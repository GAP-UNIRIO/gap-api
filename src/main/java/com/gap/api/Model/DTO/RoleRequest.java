package com.gap.api.Model.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RoleRequest(
        @NotBlank @Size(max = 255) String authority
) {
}
