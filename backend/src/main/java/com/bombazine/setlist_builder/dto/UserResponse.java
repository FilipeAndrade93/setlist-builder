package com.bombazine.setlist_builder.dto;

import com.bombazine.setlist_builder.entity.AppUser;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(UUID id, String username, String role, LocalDateTime createdAt) {

    public static UserResponse from (AppUser user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getRole().name(), user.getCreatedAt());
    }
}
