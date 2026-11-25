package com.gap.api.Model.DTO;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

public record ApprovalDecisionRequest(
        @NotNull Decision decision,
        String justification
) {
    @AssertTrue(message = "Justification is required when rejecting an approval stage.")
    public boolean isJustificationValid() {
        return decision != Decision.REJECTED || (justification != null && !justification.isBlank());
    }

    public enum Decision {
        APPROVED,
        REJECTED
    }
}
