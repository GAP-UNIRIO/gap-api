package com.gap.api.Model.DTO;

import com.gap.api.Model.Entity.Order;

import java.time.ZonedDateTime;
import java.util.List;

public record OrderResponse(Long id, Long userId, Order.OrderType orderType,
                            ZonedDateTime createdAt, List<AnnexResponse> annexes) { }