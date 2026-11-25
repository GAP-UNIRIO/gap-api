package com.gap.api.Model.DTO;

import java.util.List;

public record UserOrgUnitsResponse(Long userId, List<Long> orgUnitIds) { }
