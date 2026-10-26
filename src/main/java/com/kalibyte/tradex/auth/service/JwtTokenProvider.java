package com.kalibyte.tradex.auth.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {
    
    @Value("${app.jwt.secret:tradex-secret-key-min-32-characters-required}")
    private String jwtSecret;
    
    @Value("${app.jwt.expiration:86400000}") // 24 hours
    private long jwtExpirationMs;
    
    @Value("${app.jwt.refresh-expiration:604800000}") // 7 days
    private long refreshTokenExpirationMs;
    
    /**
     * Generate JWT token for authenticated party
     */
    public String generateToken(Long partyId, String email, String companyName, String capabilities) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("partyId", partyId);
        claims.put("email", email);
        claims.put("companyName", companyName);
        claims.put("capabilities", capabilities);
        
        return createToken(claims, email, jwtExpirationMs);
    }
    
    /**
     * Generate refresh token
     */
    public String generateRefreshToken(Long partyId, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("partyId", partyId);
        claims.put("type", "REFRESH");
        
        return createToken(claims, email, refreshTokenExpirationMs);
    }
    
    /**
     * Create token with claims
     */
    private String createToken(Map<String, Object> claims, String subject, long expirationTime) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);
        
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }
    
    /**
     * Get party ID from token
     */
    public Long getPartyIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        Object partyId = claims.get("partyId");
        if (partyId != null) {
            return Long.valueOf(partyId.toString());
        }
        return null;
    }
    
    /**
     * Get email from token
     */
    public String getEmailFromToken(String token) {
        return getAllClaimsFromToken(token).getSubject();
    }
    
    /**
     * Get company name from token
     */
    public String getCompanyNameFromToken(String token) {
        Object companyName = getAllClaimsFromToken(token).get("companyName");
        return companyName != null ? companyName.toString() : null;
    }
    
    /**
     * Get all claims from token
     */
    private Claims getAllClaimsFromToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * Get expiration from token
     */
    public Date getExpirationDateFromToken(String token) {
        return getAllClaimsFromToken(token).getExpiration();
    }
    
    /**
     * Check if token is expired
     */
    public Boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            return true;
        }
    }
    
    /**
     * Validate JWT token
     */
    public Boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.error("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.error("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.error("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }
}
