package com.gap.api.Model.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record UserRolesRequest(
        @NotEmpty List<@NotBlank String> roles
) {
}
