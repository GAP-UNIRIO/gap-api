package com.gap.api.Model.DTO;

import com.gap.api.Model.Entity.Order;

import java.time.ZonedDateTime;
import java.util.List;

public record OrderResponse(Long id, Long userId, Order.OrderType orderType, Order.OrderStatus status,
                            ZonedDateTime createdAt, List<AnnexResponse> annexes,
                            List<ApprovalStageResponse> approvalStages) {
    public OrderResponse(Long id, Long userId, Order.OrderType orderType, Order.OrderStatus status,
                         ZonedDateTime createdAt, List<AnnexResponse> annexes) {
        this(id, userId, orderType, status, createdAt, annexes, List.of());
    }
}
