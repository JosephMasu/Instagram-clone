package com.masu.auth_service.service;

import com.masu.auth_service.Model.User;
import com.masu.auth_service.dto.AuthResponse;
import com.masu.auth_service.dto.LoginRequest;
import com.masu.auth_service.dto.RegisterRequest;
import com.masu.auth_service.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public User register(RegisterRequest request) {

        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException(
                    "Username already exists"
            );
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException(
                    "Email already exists"
            );
        }

        Instant now = Instant.now();

        User user = new User();

        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );

        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        return userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        User user = userRepository
                .findByEmail(request.email())
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found"
                        )
                );

        String token = jwtService.generateToken(
                user.getId().toHexString()
        );

        return new AuthResponse(
                token,
                "Bearer",
                3600000
        );
    }
}
