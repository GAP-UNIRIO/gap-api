package com.gap.api.Model.DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

public record CourseRequest(
        @NotBlank String name,
        @NotNull Long directorId,
        @NotNull Long coordinatorId,
        @NotNull Long secretaryId,
        @NotNull Long departmentId,
        @NotNull Long reitoriaId,
        @NotNull Long coordenacaoId,
        @NotNull Long direcaoId,
        @NotNull Long secretariaId,
        @NotNull Long escolaId
) {
    public CourseRequest(String name, Long directorId, Long coordinatorId, Long secretaryId, Long departmentId) {
        this(name, directorId, coordinatorId, secretaryId, departmentId, null, null, null, null, null);
    }
}
