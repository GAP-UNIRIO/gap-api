package com.gap.api.Model.DTO;

public record BaseResponseDTO(
        String code,
        String message,
        Object data
) {

}
