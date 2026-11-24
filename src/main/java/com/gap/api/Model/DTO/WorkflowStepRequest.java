package com.gap.api.Model.DTO;

import com.gap.api.Model.Entity.ApprovalStage;
import com.gap.api.Model.Entity.OrgUnit;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WorkflowStepRequest(
        @Min(value = 1, message = "stepOrder deve ser maior ou igual a 1") int stepOrder,
        @NotNull ApprovalStage.ApprovalStageType stageType,
        @NotNull OrgUnit.OrgUnitType responsibleUnitType,
        @NotBlank @Size(max = 255) String stepName,
        @NotBlank @Size(max = 255) String stepDescription
) {}
