package com.gap.api.Service;

import com.gap.api.Model.DTO.ApprovalDecisionRequest;
import com.gap.api.Model.Entity.ApprovalStage;
import com.gap.api.Model.Entity.Order;
import com.gap.api.Model.Entity.User;
import com.gap.api.Repository.ApprovalStageRepository;
import com.gap.api.Repository.UserRepository;
import com.gap.api.Service.Interface.IOrderStatusService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ApprovalDecisionService {

    private final ApprovalStageRepository approvalStageRepository;
    private final UserRepository userRepository;
    private final IOrderStatusService orderStatusService;

    @Transactional
    public ApprovalStage decide(Long orderId, Long stageId, String authenticatedEmail,
                                ApprovalDecisionRequest request) {
        if (request == null || request.decision() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Approval decision is required.");
        }
        if (request.decision() == ApprovalDecisionRequest.Decision.REJECTED
                && (request.justification() == null || request.justification().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "A justification is required to reject an approval stage.");
        }

        User actor = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found."));
        ApprovalStage stage = approvalStageRepository.findByIdAndOrder_Id(stageId, orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Approval stage not found."));

        boolean belongsToResponsibleUnit = actor.getOrgUnits().stream()
                .anyMatch(unit -> unit.getId().equals(stage.getResponsibleUnit().getId()));
        if (!belongsToResponsibleUnit) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only a member of the responsible unit can decide this approval stage.");
        }

        ApprovalStage currentStage = approvalStageRepository
                .findAllByOrder_IdOrderByStepOrderAsc(orderId).stream()
                .filter(candidate -> candidate.getStatus() == ApprovalStage.ApprovalStageStatus.PENDING)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "There are no pending approval stages for this order."));
        if (!currentStage.getId().equals(stage.getId())) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Approval stages must be decided in workflow order.");
        }

        Order order = stage.getOrder();
        if (order.getStatus() != Order.OrderStatus.ABERTA && order.getStatus() != Order.OrderStatus.EM_ANALISE) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Only open orders can have approval stages decided.");
        }

        stage.setDecidedBy(actor);
        if (request.decision() == ApprovalDecisionRequest.Decision.REJECTED) {
            stage.setStatus(ApprovalStage.ApprovalStageStatus.REJECTED);
            stage.setRejectionJustification(request.justification().trim());
            orderStatusService.transition(order, Order.OrderStatus.INDEFERIDA, actor);
        } else {
            stage.setStatus(ApprovalStage.ApprovalStageStatus.APPROVED);
            stage.setRejectionJustification(null);
            if (order.getStatus() == Order.OrderStatus.ABERTA) {
                orderStatusService.transition(order, Order.OrderStatus.EM_ANALISE, actor);
            }
        }

        return approvalStageRepository.save(stage);
    }
}
