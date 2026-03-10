package com.bombazine.setlist_builder.service;

import com.bombazine.setlist_builder.dto.LoginRequest;
import com.bombazine.setlist_builder.dto.LoginResponse;
import com.bombazine.setlist_builder.entity.AppUser;
import com.bombazine.setlist_builder.repository.AppUserRepository;
import com.bombazine.setlist_builder.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        String token = jwtUtil.generateToken(request.username());

        return new LoginResponse(token, request.username());
    }

    @Transactional
    public void register(String username, String password) {
        if (userRepository.existsByUsername(username)) throw new IllegalArgumentException("Username already exists");

        userRepository.save(AppUser.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(password))
                .build());
    }
}
