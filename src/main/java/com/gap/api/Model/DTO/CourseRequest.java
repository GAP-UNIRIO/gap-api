package com.gap.api.Model.DTO;

import jakarta.validation.constraints.NotNull;

public record CourseRequest(
        String name,
        Long directorId,
        Long coordinatorId,
        Long secretaryId,
        @NotNull Long departmentId
) {
}
