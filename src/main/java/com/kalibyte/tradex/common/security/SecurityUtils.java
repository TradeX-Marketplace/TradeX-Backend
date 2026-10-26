package com.kalibyte.tradex.common.security;

import com.kalibyte.tradex.auth.service.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SecurityUtils - Resolves party context, roles, and administrative privileges
 * dynamically from HTTP headers, JWT tokens, and request attributes.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityUtils {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Resolves the current Party ID from:
     * 1. Explicit method argument (if passed and valid)
     * 2. X-Party-Id header
     * 3. Authorization Bearer JWT token (partyId claim)
     * 4. Default fallback (1L) for developer ease
     */
    public Long resolvePartyId(HttpServletRequest request, Long explicitPartyId) {
        if (explicitPartyId != null && explicitPartyId > 0) {
            return explicitPartyId;
        }

        if (request != null) {
            // Check X-Party-Id header
            String headerVal = request.getHeader("X-Party-Id");
            if (headerVal != null && !headerVal.trim().isEmpty()) {
                try {
                    return Long.parseLong(headerVal.trim());
                } catch (NumberFormatException ignored) {
                }
            }

            // Check Bearer JWT token
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7).trim();
                try {
                    if (jwtTokenProvider.validateToken(token)) {
                        Long partyIdFromToken = jwtTokenProvider.getPartyIdFromToken(token);
                        if (partyIdFromToken != null) {
                            return partyIdFromToken;
                        }
                    }
                } catch (Exception e) {
                    log.debug("Could not extract partyId from JWT: {}", e.getMessage());
                }
            }
        }

        // Graceful fallback for backwards compatibility
        return 1L;
    }

    /**
     * Resolves party role (BUYER / SELLER / TRADER)
     */
    public String resolvePartyRole(HttpServletRequest request, String explicitRole) {
        if (explicitRole != null && !explicitRole.trim().isEmpty()) {
            return explicitRole.trim().toUpperCase();
        }

        if (request != null) {
            String roleHeader = request.getHeader("X-Party-Role");
            if (roleHeader != null && !roleHeader.trim().isEmpty()) {
                return roleHeader.trim().toUpperCase();
            }
        }

        return "BUYER";
    }

    /**
     * Checks if caller has administrative privileges
     */
    public boolean isAdmin(HttpServletRequest request) {
        if (request == null) {
            return false;
        }

        // Check X-Admin-Id or admin header
        String adminIdHeader = request.getHeader("X-Admin-Id");
        if (adminIdHeader != null && !adminIdHeader.trim().isEmpty()) {
            return true;
        }

        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader != null && userIdHeader.toLowerCase().contains("admin")) {
            return true;
        }

        // Check JWT token
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            try {
                if (jwtTokenProvider.validateToken(token)) {
                    String email = jwtTokenProvider.getEmailFromToken(token);
                    if (email != null && email.toLowerCase().contains("admin")) {
                        return true;
                    }
                }
            } catch (Exception ignored) {
            }
        }

        return false;
    }

    /**
     * Resolves admin operator identifier
     */
    public String resolveAdminName(HttpServletRequest request, String fallback) {
        if (request != null) {
            String adminIdHeader = request.getHeader("X-Admin-Id");
            if (adminIdHeader != null && !adminIdHeader.trim().isEmpty()) {
                return "ADMIN-" + adminIdHeader.trim();
            }
            String userIdHeader = request.getHeader("X-User-Id");
            if (userIdHeader != null && !userIdHeader.trim().isEmpty()) {
                return userIdHeader.trim();
            }
        }
        return fallback != null ? fallback : "ADMIN-USER";
    }
}
