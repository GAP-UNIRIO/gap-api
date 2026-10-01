package com.gap.api.Model.DTO;

import com.gap.api.Model.Entity.Role;

public record RoleResponse(Long id, String authority) {

    public static RoleResponse from(Role role) {
        return new RoleResponse(role.getId(), role.getAuthority());
    }
}
