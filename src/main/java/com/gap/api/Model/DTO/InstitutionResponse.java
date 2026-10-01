package com.gap.api.Model.DTO;

import com.gap.api.Model.Entity.Institution;
import com.gap.api.Model.Entity.Institution.AuthProviderType;
import com.gap.api.Model.Entity.Institution.SignatureProviderType;

import java.time.ZonedDateTime;
import java.util.Map;

public record InstitutionResponse(
        Long id,
        String name,
        String code,
        AuthProviderType authProviderType,
        SignatureProviderType signatureProvider,
        Map<String, Object> settings,
        boolean active,
        ZonedDateTime createdAt
) {

    public static InstitutionResponse from(Institution i) {
        return new InstitutionResponse(
                i.getId(),
                i.getName(),
                i.getCode(),
                i.getAuthProviderType(),
                i.getSignatureProvider(),
                i.getSettings(),
                i.isActive(),
                i.getCreatedAt());
    }
}
