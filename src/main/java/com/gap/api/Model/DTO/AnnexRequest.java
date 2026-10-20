package com.gap.api.Model.DTO;

import jakarta.validation.constraints.NotBlank;

public record AnnexRequest(@NotBlank String name, @NotBlank String contentType, @NotBlank String temporaryKey) { }