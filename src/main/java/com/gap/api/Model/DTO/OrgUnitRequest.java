package com.gap.api.Model.DTO;

import com.gap.api.Model.Entity.OrgUnit;

public record OrgUnitRequest(
        OrgUnit.OrgUnitType type,
        String name
) {

    public OrgUnit toEntity() {
        OrgUnit orgUnit = new OrgUnit();
        orgUnit.setType(this.type);
        orgUnit.setName(this.name);
        return orgUnit;
    }

}
