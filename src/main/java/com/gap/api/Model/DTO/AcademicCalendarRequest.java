package com.gap.api.Model.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Map;

/**
 * "active" é opcional: na criação o padrão é false; na atualização, se omitido, mantém o valor atual.
 */
public record AcademicCalendarRequest(
        @NotBlank @Size(max = 10) String semester, // Ex: "2026.1"
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        Map<String, LocalDate> deadlines,
        Map<String, Integer> academicLimits,
        Boolean active
) {
}
