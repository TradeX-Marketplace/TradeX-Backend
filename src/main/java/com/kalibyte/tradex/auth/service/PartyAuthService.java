package com.kalibyte.tradex.auth.service;

import com.kalibyte.tradex.auth.dto.request.PartyLoginRequest;
import com.kalibyte.tradex.auth.dto.request.PartyOtpRequest;
import com.kalibyte.tradex.auth.dto.request.PartyOtpVerifyRequest;
import com.kalibyte.tradex.auth.dto.request.PartyQuickLoginRequest;
import com.kalibyte.tradex.auth.dto.response.PartyLoginResponse;
import com.kalibyte.tradex.auth.entity.AuthCredential;
import com.kalibyte.tradex.auth.repository.AuthCredentialRepository;
import com.kalibyte.tradex.party.entity.Role;
import com.kalibyte.tradex.notification.service.WhatsAppService;
import com.kalibyte.tradex.party.entity.Party;
import com.kalibyte.tradex.party.repository.PartyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * PartyAuthService - Handles high-performance, flexible authentication for TradeX trading parties.
 * Supports:
 * 1. Standard Login (Email / Phone / Party Code + Password)
 * 2. Instant WhatsApp One-Click Quick Login / Activation (Party Code from WhatsApp Link)
 * 3. Passwordless WhatsApp OTP Login
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PartyAuthService {

    private final PartyRepository partyRepository;
    private final AuthCredentialRepository authCredentialRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final WhatsAppService whatsAppService;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * Standard Login via Email, Phone, or Party Code + Password
     */
    @Transactional
    public PartyLoginResponse login(PartyLoginRequest request, String ipAddress) {
        Party party = findPartyByIdentifier(request.getIdentifier().trim())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (Boolean.TRUE.equals(party.getIsDeleted())) {
            throw new RuntimeException("This account has been deleted. Please contact TradeX support.");
        }
        if ("PENDING_APPROVAL".equalsIgnoreCase(party.getStatus())) {
            throw new RuntimeException("Your enterprise registration is pending admin approval.");
        }
        if ("REJECTED".equalsIgnoreCase(party.getStatus())) {
            throw new RuntimeException("Your enterprise registration has been rejected. Please contact TradeX support.");
        }
        if (Boolean.FALSE.equals(party.getActive())) {
            throw new RuntimeException("Party account is inactive. Please contact TradeX support.");
        }

        AuthCredential credential = authCredentialRepository.findByPartyId(party.getId())
                .orElseGet(() -> createDefaultCredential(party, party.getPhone()));

        // Check account lock
        if (credential.getLockedUntil() != null && credential.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Account temporarily locked due to multiple failed attempts. Try again later.");
        }

        String rawPassword = request.getPassword() != null ? request.getPassword().trim() : "";
        String inputPhoneDigits = rawPassword.replaceAll("\\D", "");
        Set<String> partyPhones = party.getAllContactPhones();
        boolean isSingleCredentialLogin = rawPassword.isEmpty()
                || rawPassword.equalsIgnoreCase(request.getIdentifier().trim())
                || (inputPhoneDigits.length() >= 10 && partyPhones.stream()
                        .anyMatch(ph -> ph.replaceAll("\\D", "").endsWith(inputPhoneDigits.substring(inputPhoneDigits.length() - 10))));

        // Validate password if not using single credential mobile authentication
        if (!isSingleCredentialLogin && credential.getPasswordHash() != null && !credential.getPasswordHash().isEmpty()) {
            if (!passwordEncoder.matches(rawPassword, credential.getPasswordHash())) {
                handleFailedAttempt(credential);
                throw new RuntimeException("Invalid credentials");
            }
        } else if (credential.getPasswordHash() == null || credential.getPasswordHash().isEmpty()) {
            credential.setPasswordHash(passwordEncoder.encode(party.getPhone()));
        }

        // Reset failed attempts on success
        credential.setFailedAttempts(0);
        credential.setLockedUntil(null);
        credential.setLastLogin(LocalDateTime.now());
        credential.setLastLoginIp(ipAddress);
        credential.setStatus(AuthCredential.AuthStatus.ACTIVE);
        credential.setLoginEnabled(true);
        authCredentialRepository.save(credential);

        return buildLoginResponse(party);
    }

    /**
     * Fastest Way: 1-Click WhatsApp Link Activation & Quick Login
     * Takes partyCode (e.g. PRT-0001) received in WhatsApp activation template
     */
    @Transactional
    public PartyLoginResponse quickLoginOrActivate(PartyQuickLoginRequest request, String ipAddress) {
        Party party = partyRepository.findByPartyCode(request.getPartyCode().trim())
                .orElseThrow(() -> new RuntimeException("Invalid activation code or party code: " + request.getPartyCode()));

        party.setActive(true);
        party.setCanLogin(true);
        partyRepository.save(party);

        AuthCredential credential = authCredentialRepository.findByPartyId(party.getId())
                .orElseGet(() -> createDefaultCredential(party, request.getPassword()));

        if (request.getPassword() != null && !request.getPassword().trim().isEmpty()) {
            credential.setPasswordHash(passwordEncoder.encode(request.getPassword().trim()));
        }

        credential.setStatus(AuthCredential.AuthStatus.ACTIVE);
        credential.setLoginEnabled(true);
        credential.setLastLogin(LocalDateTime.now());
        credential.setLastLoginIp(ipAddress);
        credential.setFailedAttempts(0);
        authCredentialRepository.save(credential);

        log.info("Party [{}] successfully activated and logged in via quick-login", party.getPartyCode());
        return buildLoginResponse(party);
    }

    /**
     * Request WhatsApp OTP for passwordless mobile trading login
     */
    @Transactional
    public void requestOtp(PartyOtpRequest request) {
        String phone = request.getPhone().trim();
        Party party = partyRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("No registered party found with phone: " + phone));

        if ("PENDING_APPROVAL".equalsIgnoreCase(party.getStatus())) {
            throw new RuntimeException("Your enterprise registration is pending admin approval.");
        }
        if ("REJECTED".equalsIgnoreCase(party.getStatus())) {
            throw new RuntimeException("Your enterprise registration has been rejected. Please contact TradeX support.");
        }
        if (Boolean.FALSE.equals(party.getActive())) {
            throw new RuntimeException("Party account is inactive. Please contact TradeX support.");
        }

        AuthCredential credential = authCredentialRepository.findByPartyId(party.getId())
                .orElseGet(() -> createDefaultCredential(party, null));

        String otp = String.format("%06d", secureRandom.nextInt(1_000_000));
        credential.setOtp(otp);
        credential.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        authCredentialRepository.save(credential);

        try {
            String message = String.format(
                    "🔐 *TradeX Login OTP*\n\n" +
                    "Your one-time verification code is: *%s*\n\n" +
                    "Valid for 5 minutes. Do not share this OTP with anyone.",
                    otp
            );
            whatsAppService.sendMessage(party.getPhone(), message, String.valueOf(party.getId()), "OTP");
            log.info("Dispatched WhatsApp OTP to party {}", party.getPartyCode());
        } catch (Exception e) {
            log.error("Failed to send WhatsApp OTP: {}", e.getMessage());
            throw new RuntimeException("Failed to dispatch WhatsApp OTP: " + e.getMessage());
        }
    }

    /**
     * Verify WhatsApp OTP and login
     */
    @Transactional
    public PartyLoginResponse verifyOtp(PartyOtpVerifyRequest request, String ipAddress) {
        String phone = request.getPhone().trim();
        Party party = partyRepository.findByPhone(phone)
                .orElseThrow(() -> new RuntimeException("Party not found with phone: " + phone));

        AuthCredential credential = authCredentialRepository.findByPartyId(party.getId())
                .orElseThrow(() -> new RuntimeException("Authentication record not found"));

        if (credential.getOtp() == null || !credential.getOtp().equals(request.getOtp().trim())) {
            throw new RuntimeException("Invalid or incorrect OTP");
        }

        if (credential.getOtpExpiry() == null || credential.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired. Please request a new code.");
        }

        // Clear OTP on successful consumption
        credential.setOtp(null);
        credential.setOtpExpiry(null);
        credential.setLastLogin(LocalDateTime.now());
        credential.setLastLoginIp(ipAddress);
        credential.setStatus(AuthCredential.AuthStatus.ACTIVE);
        credential.setLoginEnabled(true);
        credential.setPhoneVerified(true);
        authCredentialRepository.save(credential);

        log.info("Party [{}] logged in successfully via WhatsApp OTP", party.getPartyCode());
        return buildLoginResponse(party);
    }

    // ==================== HELPER METHODS ====================

    private Optional<Party> findPartyByIdentifier(String identifier) {
        // Try party code first
        Optional<Party> byCode = partyRepository.findByPartyCode(identifier);
        if (byCode.isPresent()) return byCode;

        // Try email
        Optional<Party> byEmail = partyRepository.findByEmail(identifier);
        if (byEmail.isPresent()) return byEmail;

        // Try phone directly
        Optional<Party> byPhone = partyRepository.findByPhone(identifier);
        if (byPhone.isPresent()) return byPhone;

        // Try matching last 10 digits across all contact phones
        String digits = identifier.replaceAll("\\D", "");
        if (digits.length() >= 10) {
            String last10 = digits.substring(digits.length() - 10);
            return partyRepository.findAll().stream()
                    .filter(p -> p.getAllContactPhones().stream()
                            .anyMatch(ph -> ph.replaceAll("\\D", "").endsWith(last10)))
                    .findFirst();
        }

        return Optional.empty();
    }

    private AuthCredential createDefaultCredential(Party party, String rawPassword) {
        String passwordHash = (rawPassword != null && !rawPassword.trim().isEmpty())
                ? passwordEncoder.encode(rawPassword.trim())
                : passwordEncoder.encode("TradeX@" + party.getPartyCode());

        AuthCredential credential = AuthCredential.builder()
                .partyId(party.getId())
                .email(party.getEmail() != null ? party.getEmail() : party.getPartyCode() + "@tradex.internal")
                .phone(party.getPhone() != null ? party.getPhone() : "0000000000")
                .passwordHash(passwordHash)
                .status(AuthCredential.AuthStatus.ACTIVE)
                .loginEnabled(true)
                .failedAttempts(0)
                .build();

        return authCredentialRepository.save(credential);
    }

    private void handleFailedAttempt(AuthCredential credential) {
        int attempts = (credential.getFailedAttempts() != null ? credential.getFailedAttempts() : 0) + 1;
        credential.setFailedAttempts(attempts);
        if (attempts >= 5) {
            credential.setLockedUntil(LocalDateTime.now().plusMinutes(30));
            log.warn("Account locked for partyId {} due to {} failed attempts", credential.getPartyId(), attempts);
        }
        authCredentialRepository.save(credential);
    }

    private PartyLoginResponse buildLoginResponse(Party party) {
        Set<String> roleNames = party.getRoles() != null
                ? party.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
                : Set.of("TRADER");

        String primaryRole = roleNames.contains("BUYER") ? "BUYER"
                : (roleNames.contains("SELLER") ? "SELLER" : "TRADER");

        String capabilities = String.join(",", roleNames);
        String token = jwtTokenProvider.generateToken(
                party.getId(),
                party.getEmail() != null ? party.getEmail() : party.getPartyCode() + "@tradex.internal",
                party.getCompanyName(),
                capabilities
        );
        String refreshToken = jwtTokenProvider.generateRefreshToken(
                party.getId(),
                party.getEmail() != null ? party.getEmail() : party.getPartyCode() + "@tradex.internal"
        );

        return PartyLoginResponse.builder()
                .token(token)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000L) // 24 hours
                .partyId(party.getId())
                .partyCode(party.getPartyCode())
                .companyName(party.getCompanyName())
                .contactPerson(party.getContactPerson())
                .email(party.getEmail())
                .phone(party.getPhone())
                .primaryRole(primaryRole)
                .roles(roleNames)
                .status(party.getStatus())
                .build();
    }
}
