package com.kalibyte.tradex.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "tradex-super-secure-secret-key-that-is-at-least-256-bits-long!");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 86400000L); // 24 hours
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpirationMs", 604800000L); // 7 days
    }

    @Test
    @DisplayName("Generate JWT token and validate claims")
    void testGenerateTokenAndValidate() {
        Long partyId = 42L;
        String email = "trader@kalibyte.com";
        String companyName = "KaliByte Traders";
        String capabilities = "BUYER,SELLER";

        String token = jwtTokenProvider.generateToken(partyId, email, companyName, capabilities);

        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(partyId, jwtTokenProvider.getPartyIdFromToken(token));
        assertEquals(email, jwtTokenProvider.getEmailFromToken(token));
        assertEquals(companyName, jwtTokenProvider.getCompanyNameFromToken(token));
        assertFalse(jwtTokenProvider.isTokenExpired(token));
    }

    @Test
    @DisplayName("Generate Refresh Token and validate")
    void testGenerateRefreshToken() {
        Long partyId = 42L;
        String email = "trader@kalibyte.com";

        String refreshToken = jwtTokenProvider.generateRefreshToken(partyId, email);

        assertNotNull(refreshToken);
        assertTrue(jwtTokenProvider.validateToken(refreshToken));
        assertEquals(partyId, jwtTokenProvider.getPartyIdFromToken(refreshToken));
        assertEquals(email, jwtTokenProvider.getEmailFromToken(refreshToken));
    }

    @Test
    @DisplayName("Validate invalid token returns false")
    void testValidateInvalidToken() {
        assertFalse(jwtTokenProvider.validateToken("invalid.jwt.token"));
    }
}
