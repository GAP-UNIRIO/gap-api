package com.gap.api.Model.DTO;

import com.gap.api.Model.Entity.ApprovalStage;
import com.gap.api.Model.Entity.Course;
import com.gap.api.Model.Entity.Order;
import com.gap.api.Model.Entity.OrgUnit;
import com.gap.api.Model.Entity.WorkflowStep;
import com.gap.api.Model.Entity.WorkflowTemplate;

import java.util.Comparator;
import java.util.List;

public record WorkflowTemplateResponse(
        Long id,
        Order.OrderType orderType,
        CourseSummary course,
        List<WorkflowStepSummary> steps
) {
    public static WorkflowTemplateResponse from(WorkflowTemplate workflowTemplate) {
        return new WorkflowTemplateResponse(
                workflowTemplate.getId(),
                workflowTemplate.getOrderType(),
                CourseSummary.from(workflowTemplate.getCourse()),
                workflowTemplate.getSteps().stream()
                        .sorted(Comparator.comparingInt(WorkflowStep::getStepOrder))
                        .map(WorkflowStepSummary::from)
                        .toList()
        );
    }

    /** Resumo do curso, para não expor a entidade (relações preguiçosas e referência circular com User). */
    public record CourseSummary(Long id, String name) {
        private static CourseSummary from(Course course) {
            return course == null ? null : new CourseSummary(course.getId(), course.getName());
        }
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
