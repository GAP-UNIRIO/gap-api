package com.gap.api.Model.DTO;

import com.gap.api.Model.Entity.Institution.AuthProviderType;
import com.gap.api.Model.Entity.Institution.SignatureProviderType;
import com.gap.api.Validation.ValidCnpj;
import com.gap.api.Validation.ValidSettings;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Map;

/**
 * "active" é opcional: na criação o padrão é true; na atualização, se omitido, mantém o valor atual.
 */
public record InstitutionRequest(
        @NotBlank @Size(max = 255) String name,
        @NotBlank @Size(max = 50) @ValidCnpj String code,
        @NotNull AuthProviderType authProviderType,
        @NotNull SignatureProviderType signatureProvider,
        @ValidSettings Map<String, Object> settings,
        Boolean active
) {
}
