package com.gap.api.Model.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDate;
import java.util.Map;

/**
 * "active" é opcional: na criação o padrão é false; na atualização, se omitido, mantém o valor atual.
 */
public record AcademicCalendarRequest(
        @NotBlank @Pattern(regexp = "^[0-9]{4}\\.[12]$") String semester, // Ex: "2026.1"
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotEmpty Map<@NotBlank String, @NotNull LocalDate> deadlines,
        @NotEmpty Map<@NotBlank String, @NotNull @PositiveOrZero Integer> academicLimits,
        Boolean active
) {
}
