package com.litethinking.platform.auth.service;

import com.litethinking.platform.auth.domain.UserAccount;
import com.litethinking.platform.auth.domain.UserRole;
import com.litethinking.platform.auth.dto.LoginRequest;
import com.litethinking.platform.auth.dto.RegisterUserRequest;
import com.litethinking.platform.auth.dto.TokenResponse;
import com.litethinking.platform.common.exception.InvalidCredentialsException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserAccountService userAccountService;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    private UserAccount admin;

    @BeforeEach
    void setUp() {
        admin = new UserAccount("admin@litethinking.com", "secret", UserRole.ADMIN);
    }

    @Test
    void registerShouldPersistUserAndGenerateToken() {
        RegisterUserRequest request = new RegisterUserRequest("admin@litethinking.com", "Secret123*", UserRole.ADMIN);
        when(userAccountService.register(request)).thenReturn(admin);
        when(jwtService.generateToken(admin)).thenReturn("token");
        when(jwtService.getExpirationMinutes()).thenReturn(60L);

        TokenResponse response = authService.register(request);

        assertThat(response.token()).isEqualTo("token");
        assertThat(response.expiresInMinutes()).isEqualTo(60L);
        verify(userAccountService).register(request);
        verify(jwtService).generateToken(admin);
    }

    @Test
    void loginShouldAuthenticateGenerateTokenAndReturnResponse() {
        LoginRequest request = new LoginRequest("admin@litethinking.com", "Secret123*");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(userAccountService.findByEmail("admin@litethinking.com")).thenReturn(admin);
        when(jwtService.generateToken(admin)).thenReturn("jwt-token");
        when(jwtService.getExpirationMinutes()).thenReturn(30L);

        TokenResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.expiresInMinutes()).isEqualTo(30L);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userAccountService).findByEmail("admin@litethinking.com");
        verify(jwtService).generateToken(admin);
    }

    @Test
    void loginShouldFailWhenCredentialsAreInvalid() {
        LoginRequest request = new LoginRequest("admin@litethinking.com", "Wrong");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(InvalidCredentialsException.class)
                .hasMessageContaining("Credenciales invalidas");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userAccountService, org.mockito.Mockito.never()).findByEmail(eq("admin@litethinking.com"));
    }
}
