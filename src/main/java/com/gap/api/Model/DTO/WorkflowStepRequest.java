package com.gap.api.Model.DTO;

import com.gap.api.Model.Entity.ApprovalStage;
import com.gap.api.Model.Entity.OrgUnit;

public record WorkflowStepRequest(
        int stepOrder,
        ApprovalStage.ApprovalStageType stageType,
        OrgUnit.OrgUnitType responsibleUnitType
) {}
