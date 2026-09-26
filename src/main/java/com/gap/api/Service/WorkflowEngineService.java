package com.gap.api.Service;

import com.gap.api.Model.Entity.*;
import com.gap.api.Repository.ApprovalStageRepository;
import com.gap.api.Repository.WorkflowTemplateRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WorkflowEngineService {

    private final ApprovalStageRepository approvalStageRepository;
    private final WorkflowTemplateRepository workflowTemplateRepository;
    private final OrgUnitResolutionService orgUnitResolutionService;

    @Transactional
    public void instantiateWorkflowForOrder(Order order) {
        WorkflowTemplate workflowTemplate = workflowTemplateRepository
                .findByOrderTypeAndCourse_Id(order.getOrderType(), order.getUser().getCourse().getId())
                .orElseGet(() -> workflowTemplateRepository
                        .findByOrderTypeAndCourseIsNull(order.getOrderType())
                        .orElseThrow(() -> new RuntimeException("No workflow template found for order type: " + order.getOrderType())));

        List<WorkflowStep> sortedSteps  = workflowTemplate.getSteps().stream()
                .sorted((Comparator.comparingInt(WorkflowStep::getStepOrder))).toList();

        List<ApprovalStage> activeStages = new ArrayList<>();

        for (WorkflowStep workflowStep : sortedSteps) {
            ApprovalStage approvalStage = new ApprovalStage();
            approvalStage.setOrder(order);
            approvalStage.setStepOrder(workflowStep.getStepOrder());
            approvalStage.setType(workflowStep.getStageType());
            approvalStage.setStatus(ApprovalStage.ApprovalStageStatus.PENDING);
            var responsibleUnit = orgUnitResolutionService.
                    resolveUnitForOrder(workflowStep.getResponsibleUnitType(), order);
            approvalStage.setResponsibleUnit(responsibleUnit);
            activeStages.add(approvalStage);
        }

        approvalStageRepository.saveAll(activeStages);
    }


}
