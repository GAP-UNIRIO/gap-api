package com.gap.api.Model.DTO;

import com.gap.api.Model.Entity.Order;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public record OrderRequest(@NotNull Long userId, @NotNull Order.OrderType orderType,
                           Map<String, Object> extraData, @Valid List<AnnexRequest> annexes) { }