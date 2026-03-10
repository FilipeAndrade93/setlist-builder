package com.bombazine.setlist_builder.service;

import com.bombazine.setlist_builder.dto.*;
import com.bombazine.setlist_builder.entity.AppUser;
import com.bombazine.setlist_builder.entity.UserRole;
import com.bombazine.setlist_builder.exception.Exceptions;
import com.bombazine.setlist_builder.repository.AppUserRepository;
import com.bombazine.setlist_builder.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

        AppUser user = userRepository.findByUsernameAndDeletedAtIsNull(request.username()).orElseThrow();

        String token = jwtUtil.generateToken(request.username(), user.getRole().name());

        return new LoginResponse(token, request.username(), user.getRole().name());
    }

    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsernameAndDeletedAtIsNull(request.username())) throw new IllegalArgumentException("Username already exists");

        userRepository.save(AppUser.builder()
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(UserRole.MEMBER)
                .build());
    }

    @Transactional
    public void resetPassword(UUID userId, ResetPasswordRequest request) {
        AppUser user = userRepository.findByIdAndDeletedAtIsNull(userId).orElseThrow(() -> new Exceptions.UserNotFoundException(userId));

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    }

    @Transactional
    public void deleteUser(UUID userId) {
        AppUser user = userRepository.findByIdAndDeletedAtIsNull(userId).orElseThrow(() -> new Exceptions.UserNotFoundException(userId));

        if (user.getRole() == UserRole.ADMIN) throw new IllegalStateException("Admin cannot be deleted");

        user.setDeletedAt(LocalDateTime.now());
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findByDeletedAtIsNull()
                .stream()
                .map(UserResponse::from)
                .toList();
    }
}
