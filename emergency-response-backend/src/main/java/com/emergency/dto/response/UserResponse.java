package com.emergency.dto.response;

import com.emergency.model.User;

public record UserResponse(
    Long id,
    String name,
    String email,
    String phone,
    String role,
    String avatarUrl,
    boolean isActive,
    String createdAt
) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getPhone(),
            user.getRole().name(),
            user.getAvatarUrl(),
            user.isActive(),
            user.getCreatedAt() != null ? user.getCreatedAt().toString() : null
        );
    }
}
