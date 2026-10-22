package com.gap.api.Model.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public record CourseRequest(
        @NotBlank String name,
        Long directorId,
        Long coordinatorId,
        Long secretaryId,
        @NotNull Long departmentId
) {
}
