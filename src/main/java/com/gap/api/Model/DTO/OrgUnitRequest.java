package com.gap.api.Model.DTO;

import com.gap.api.Model.Entity.OrgUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrgUnitRequest(
        @NotNull OrgUnit.OrgUnitType type,
        @NotBlank @Size(max = 255) String name
) {

    public OrgUnit toEntity() {
        OrgUnit orgUnit = new OrgUnit();
        orgUnit.setType(this.type);
        orgUnit.setName(this.name);
        return orgUnit;
    }

}
