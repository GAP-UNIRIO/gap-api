package com.gap.api.Model.DTO;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UserComplementRequest(
        @JsonAlias("registrarionNumber")
        @NotBlank(message = "A matrícula é obrigatória.")
        @Pattern(regexp = "^[0-9]+$", message = "A matrícula deve conter apenas dígitos.")
        String registrationNumber,
        Long courseId
) {
}
