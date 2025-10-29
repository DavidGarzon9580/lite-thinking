package com.litethinking.platform.auth.service;

import com.litethinking.platform.auth.domain.UserAccount;
import com.litethinking.platform.auth.domain.UserRole;
import com.litethinking.platform.auth.dto.RegisterUserRequest;
import com.litethinking.platform.auth.repository.UserAccountRepository;
import com.litethinking.platform.common.exception.ResourceAlreadyExistsException;
import com.litethinking.platform.common.exception.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserAccountService {

    private final UserAccountRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(UserAccountRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserAccount register(RegisterUserRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new ResourceAlreadyExistsException("El usuario ya existe con correo " + request.email());
        }

        UserAccount user = new UserAccount(
                request.email(),
                passwordEncoder.encode(request.password()),
                request.role()
        );

        return repository.save(user);
    }

    @Transactional
    public void updateRole(UUID userId, UserRole role) {
        UserAccount user = repository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        user.setRole(role);
        repository.save(user);
    }

    @Transactional(readOnly = true)
    public UserAccount findByEmail(String email) {
        return repository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }
}
