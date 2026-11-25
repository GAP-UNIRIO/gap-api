package com.gap.api.Model.DTO;

import com.gap.api.Model.Entity.Order;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.Map;

public record OrderRequest(@NotNull Long userId, @NotNull Order.OrderType orderType,
                           Map<String, Object> extraData, @Valid @Size(max = 10, message = "A request can contain at most 10 annexes") List<AnnexRequest> annexes) { }
