package com.gap.api.Model.DTO;

public record CourseRequest(
        String name,
        Long directorId,
        Long coordinatorId,
        Long secretaryId
) {
}
