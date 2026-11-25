package com.gap.api.Model.DTO;

import com.gap.api.Model.Entity.ApprovalStage;

public record ApprovalStageResponse(
        Long id,
        int stepOrder,
        ApprovalStage.ApprovalStageType type,
        ApprovalStage.ApprovalStageStatus status,
        Long responsibleUnitId,
        String rejectionJustification
) {
    public static ApprovalStageResponse from(ApprovalStage stage) {
        return new ApprovalStageResponse(stage.getId(), stage.getStepOrder(), stage.getType(), stage.getStatus(),
                stage.getResponsibleUnit() == null ? null : stage.getResponsibleUnit().getId(),
                stage.getRejectionJustification());
    }
}
