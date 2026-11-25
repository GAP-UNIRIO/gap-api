package com.gap.api.Model.DTO;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UserOrgUnitsRequest(@NotNull List<@NotNull Long> orgUnitIds) { }
