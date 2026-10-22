package com.gap.api.Model.DTO;

import com.gap.api.Model.Entity.ApprovalStage;
import com.gap.api.Model.Entity.OrgUnit;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WorkflowStepRequest(
        int stepOrder,
        ApprovalStage.ApprovalStageType stageType,
        OrgUnit.OrgUnitType responsibleUnitType,
        @NotBlank @Size(max = 255) String stepName,
        @NotBlank @Size(max = 255) String stepDescription
) {}
