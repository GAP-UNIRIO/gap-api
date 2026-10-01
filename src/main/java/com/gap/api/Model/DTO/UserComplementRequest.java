package com.gap.api.Model.DTO;

public record UserComplementRequest(
        String registrarionNumber,
        Long courseId
) {
}
