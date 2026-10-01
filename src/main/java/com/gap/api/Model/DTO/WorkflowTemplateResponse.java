package com.gap.api.Model.DTO;

import com.gap.api.Model.Entity.Course;
import com.gap.api.Model.Entity.Order;
import com.gap.api.Model.Entity.WorkflowStep;
import com.gap.api.Model.Entity.WorkflowTemplate;

public record WorkflowTemplateResponse(
        Long id,
        Order.OrderType orderType,
        Course course
) {
    public static WorkflowTemplateResponse from(WorkflowTemplate workflowTemplate) {
        return new WorkflowTemplateResponse(
                workflowTemplate.getId(),
                workflowTemplate.getOrderType(),
                workflowTemplate.getCourse()
        );
    }
}
