package com.gap.api.Model.DTO;

import jakarta.validation.constraints.NotBlank;

public record AnnexUploadRequest(@NotBlank String name, @NotBlank String contentType) { }