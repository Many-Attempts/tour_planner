package org.example.tourplanner.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(
                "mySecretKeyForDevelopmentOnlyDoNotUseInProduction2024!ExtraLongForHS512",
                86400000L
        );
    }

    @Test
    void generateToken_createsValidToken() {
        String token = tokenProvider.generateToken("test@test.com");

        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    void getEmailFromToken_returnsCorrectEmail() {
        String token = tokenProvider.generateToken("test@test.com");
        String email = tokenProvider.getEmailFromToken(token);

        assertEquals("test@test.com", email);
    }

    @Test
    void validateToken_returnsFalseForInvalidToken() {
        assertFalse(tokenProvider.validateToken("invalid-token"));
    }

    @Test
    void validateToken_returnsFalseForNull() {
        assertFalse(tokenProvider.validateToken(null));
    }
}
