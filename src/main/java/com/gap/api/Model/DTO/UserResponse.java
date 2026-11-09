package com.gap.api.Model.DTO;

import com.gap.api.Model.Entity.Role;
import com.gap.api.Model.Entity.User;

import java.util.List;

public record UserResponse(
        Long id,
        String email,
        String name,
        String registrationNumber,
        Long courseId,
        boolean isCompletedCadaster,
        List<String> roles
) {
    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRegistrationNumber(),
                user.getCourse() != null ? user.getCourse().getId() : null,
                user.isCompletedCadaster(),
                user.getRoles().stream().map(Role::getAuthority).sorted().toList()
        );
    }
}
