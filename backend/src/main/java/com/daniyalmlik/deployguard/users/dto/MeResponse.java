package com.daniyalmlik.deployguard.users.dto;

import com.daniyalmlik.deployguard.users.User;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MeResponse(UUID id, String email, String role, OffsetDateTime createdAt) {
    public static MeResponse from(User user) {
        return new MeResponse(user.getId(), user.getEmail(), user.getRole(), user.getCreatedAt());
    }
}
