package com.gap.api.Model.DTO;

import com.gap.api.Model.Entity.Order;

import java.util.List;

public record WorkflowTemplateRequestDTO(
        Order.OrderType orderType,
        Long courseId,
        List<WorkflowStepRequestDTO> steps
) {}

