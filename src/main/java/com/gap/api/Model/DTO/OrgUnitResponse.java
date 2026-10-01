package com.gap.api.Model.DTO;

import com.gap.api.Model.Entity.OrgUnit;
import com.gap.api.Model.Entity.OrgUnit.OrgUnitType;

public record OrgUnitResponse(Long id, OrgUnitType type, String name) {

    public static OrgUnitResponse from(OrgUnit unit) {
        return new OrgUnitResponse(unit.getId(), unit.getType(), unit.getName());
    }
}
