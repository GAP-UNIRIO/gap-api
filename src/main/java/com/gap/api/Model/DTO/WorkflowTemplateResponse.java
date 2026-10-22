package com.gap.api.Model.DTO;

import com.gap.api.Model.Entity.Course;
import com.gap.api.Model.Entity.Order;
import com.gap.api.Model.Entity.WorkflowStep;
import com.gap.api.Model.Entity.WorkflowTemplate;
import com.gap.api.Model.Entity.ApprovalStage;
import com.gap.api.Model.Entity.OrgUnit;
import java.util.List;

public record WorkflowTemplateResponse(
        Long id,
        Order.OrderType orderType,
        Course course,
        List<WorkflowStepSummary> steps
) {
    public static WorkflowTemplateResponse from(WorkflowTemplate workflowTemplate) {
        return new WorkflowTemplateResponse(
                workflowTemplate.getId(),
                workflowTemplate.getOrderType(),
                workflowTemplate.getCourse(),
                workflowTemplate.getSteps().stream().map(WorkflowStepSummary::from).toList()
        );
    }

    public record WorkflowStepSummary(
            int stepOrder,
            ApprovalStage.ApprovalStageType stageType,
            OrgUnit.OrgUnitType responsibleUnitType,
            String stepName,
            String stepDescription
    ) {
        private static WorkflowStepSummary from(WorkflowStep step) {
            return new WorkflowStepSummary(step.getStepOrder(), step.getStageType(),
                    step.getResponsibleUnitType(), step.getStepName(), step.getStepDescription());
        }
    }
}
