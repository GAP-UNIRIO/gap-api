package com.gap.api.Model.DTO;

import com.gap.api.Model.Entity.Order;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.Valid;

import java.util.List;

public record WorkflowTemplateRequest(
        Order.OrderType orderType,
        Long courseId,
        @NotEmpty List<@Valid WorkflowStepRequest> steps
) {}

