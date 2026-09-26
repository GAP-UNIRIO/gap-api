package com.gap.api.Model.DTO;

import com.gap.api.Model.Entity.ApprovalStage;
import com.gap.api.Model.Entity.OrgUnit;

public record WorkflowStepRequestDTO(
        int stepOrder,
        ApprovalStage.ApprovalStageType stageType,
        OrgUnit.OrgUnitType responsibleUnitType
) {}
