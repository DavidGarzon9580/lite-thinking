package com.litethinking.platform.auth.service;

import com.litethinking.platform.auth.dto.LoginRequest;
import com.litethinking.platform.auth.dto.RegisterUserRequest;
import com.litethinking.platform.auth.dto.TokenResponse;
import com.litethinking.platform.common.exception.InvalidCredentialsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserAccountService userAccountService;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager,
                       UserAccountService userAccountService,
                       JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userAccountService = userAccountService;
        this.jwtService = jwtService;
    }

    @Transactional
    public TokenResponse register(RegisterUserRequest request) {
        var user = userAccountService.register(request);
        String token = jwtService.generateToken(user);
        return new TokenResponse(token, jwtService.getExpirationMinutes());
    }

    public TokenResponse login(LoginRequest request) {
        Authentication authentication = new UsernamePasswordAuthenticationToken(request.email(), request.password());
        try {
            authenticationManager.authenticate(authentication);
            var user = userAccountService.findByEmail(request.email());
            String token = jwtService.generateToken(user);
            return new TokenResponse(token, jwtService.getExpirationMinutes());
        } catch (Exception e) {
            throw new InvalidCredentialsException("Credenciales invalidas");
        }
    }
}
