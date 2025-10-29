package com.litethinking.platform.auth.service;

import com.litethinking.platform.auth.domain.UserAccount;
import com.litethinking.platform.auth.domain.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;
    private UserAccount user;

    @BeforeEach
    void setUp() {
        // Secret with at least 32 bytes
        String secret = "super-secret-key-that-is-long-enough-123";
        jwtService = new JwtService(secret, 60);
        user = new UserAccount("user@litethinking.com", "encoded", UserRole.VIEWER);
    }

    @Test
    void generateTokenShouldEmbedSubjectAndBeValid() {
        String token = jwtService.generateToken(user);

        assertThat(token).isNotBlank();
        assertThat(jwtService.isValid(token)).isTrue();
        assertThat(jwtService.getSubject(token)).isEqualTo("user@litethinking.com");
        assertThat(jwtService.getExpirationMinutes()).isEqualTo(60);
    }

    @Test
    void invalidTokenShouldBeRejected() {
        String token = jwtService.generateToken(user);
        String tampered = token.replaceFirst("\\.", ".invalid.");

        assertThat(jwtService.isValid(tampered)).isFalse();
    }
}
