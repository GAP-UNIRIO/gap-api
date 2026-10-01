package com.gap.api.Model.DTO;

import com.gap.api.Model.Entity.User;

public record UserResponse(
        Long id,
        String email,
        String name,
        String registrationNumber,
        Long courseId,
        boolean isCompletedCadaster
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRegistrationNumber(),
                user.getCourse() != null ? user.getCourse().getId() : null,
                user.isCompletedCadaster()
        );
    }
}
