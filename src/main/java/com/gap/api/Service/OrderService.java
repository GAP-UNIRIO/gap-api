package com.gap.api.Service;

import com.gap.api.Model.DTO.*;
import com.gap.api.Model.Entity.ApprovalStage;
import com.gap.api.Model.Entity.Annex;
import com.gap.api.Model.Entity.Order;
import com.gap.api.Model.Entity.OrgUnit;
import com.gap.api.Model.Entity.User;
import com.gap.api.Repository.OrderRepository;
import com.gap.api.Repository.UserRepository;
import com.gap.api.Service.Interface.IOrderService;
import com.gap.api.Service.Interface.IOrderStatusService;
import com.gap.api.Service.Interface.IStorageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {
    private static final HttpStatusCode UNPROCESSABLE_ENTITY = HttpStatusCode.valueOf(422);
    private static final int MAX_ANNEX_COUNT = 10;

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final IStorageService storageService;
    private final WorkflowEngineService workflowEngineService;
    private final ApprovalDecisionService approvalDecisionService;
    private final IOrderStatusService orderStatusService;

    @Override
    @Transactional
    public OrderResponse create(OrderRequest request, String authenticatedEmail) {
        if (request.annexes() != null && request.annexes().size() > MAX_ANNEX_COUNT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A request can contain at most 10 annexes");
        }
        User user = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if (!user.getId().equals(request.userId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Cannot create an order for another user");
        }
        List<AnnexRequest> requestedAnnexes = request.annexes() == null ? List.of() : request.annexes();
        for (AnnexRequest annex : requestedAnnexes) {
            if (!storageService.isTemporaryKeyIssued(annex.temporaryKey()))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid temporary file key");
            if (!storageService.isTemporaryKeyIssuedForUser(annex.temporaryKey(), user.getId()))
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot use another user's temporary file key");
            if (storageService.getTemporaryObjectSize(annex.temporaryKey()) > IStorageService.MAX_ANNEX_SIZE_BYTES)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attachment exceeds the 5 MiB size limit");
        }

        Order order = new Order();
        order.setUser(user);
        order.setOrderType(request.orderType());
        order.setStatus(Order.OrderStatus.ABERTA);
        order.setExtraData(request.extraData());
        List<String> finalKeys = new ArrayList<>();
        try {
            order = orderRepository.save(order);
            for (AnnexRequest requested : requestedAnnexes) {
                String finalKey = storageService.moveToOrder(requested.temporaryKey(), order.getId(), requested.name());
                finalKeys.add(finalKey);
                Annex annex = new Annex();
                annex.setOrder(order);
                annex.setName(requested.name());
                annex.setContentType(requested.contentType());
                annex.setUrl(finalKey);
                order.getAnnexes().add(annex);
            }
            if (!requestedAnnexes.isEmpty()) order = orderRepository.save(order);
            workflowEngineService.instantiateWorkflowForOrder(order);
            return toResponse(order);
        } catch (RuntimeException failure) {
            for (String key : finalKeys) {
                try { storageService.deleteObject(key); }
                catch (RuntimeException cleanupFailure) { failure.addSuppressed(cleanupFailure); }
            }
            throw failure;
        }
    }

    @Override
    public AnnexUploadResponse generateAnnexUploadUrl(AnnexUploadRequest request, String authenticatedEmail) {
        User user = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        IStorageService.UploadUrlResponse response = storageService.generateUploadUrl(
                user.getId(), request.name(), request.contentType());
        return new AnnexUploadResponse(response.uploadUrl(), response.fileKey(), response.fields());
    }

    @Override
    @Transactional
    public OrderResponse cancel(Long orderId, String authenticatedEmail) {
        User user = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot cancel an order of another user");
        }
        if (order.getStatus() != Order.OrderStatus.ABERTA) {
            throw new ResponseStatusException(UNPROCESSABLE_ENTITY,
                    "Only orders with status ABERTA can be cancelled; current status is " + order.getStatus());
        }
        return toResponse(orderStatusService.transition(order, Order.OrderStatus.CANCELADA, user));
    }

    @Override
    @Transactional
    public OrderResponse decideStage(Long orderId, Long stageId, ApprovalDecisionRequest request,
                                     String authenticatedEmail) {
        ApprovalStage decidedStage = approvalDecisionService.decide(orderId, stageId, authenticatedEmail, request);
        return toResponse(decidedStage.getOrder());
    }

    @Override
    @Transactional
    public OrderResponse complete(Long orderId, String authenticatedEmail) {
        User actor = userRepository.findByEmail(authenticatedEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));

        if (order.getStatus() != Order.OrderStatus.EM_ANALISE) {
            throw new ResponseStatusException(UNPROCESSABLE_ENTITY,
                    "Only orders under analysis can be completed after delivery.");
        }
        if (order.getApprovalStages().isEmpty() || order.getApprovalStages().stream()
                .anyMatch(stage -> stage.getStatus() != ApprovalStage.ApprovalStageStatus.APPROVED)) {
            throw new ResponseStatusException(UNPROCESSABLE_ENTITY,
                    "All approval stages must be approved before completing the order.");
        }

        OrgUnit secretaryUnit = order.getUser().getCourse() == null
                ? null : order.getUser().getCourse().getSecretaria();
        if (secretaryUnit == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "The order owner's course has no secretary unit assigned.");
        }
        if (actor.getOrgUnits().stream().noneMatch(unit -> unit.getId().equals(secretaryUnit.getId()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only a member of the secretary unit can complete the order.");
        }

        return toResponse(orderStatusService.transition(order, Order.OrderStatus.CONCLUIDA, actor));
    }

    private OrderResponse toResponse(Order order) {
        List<AnnexResponse> annexes = order.getAnnexes().stream()
                .map(annex -> new AnnexResponse(annex.getId(), annex.getName(), annex.getContentType(), annex.getUrl()))
                .toList();
        return new OrderResponse(order.getId(), order.getUser().getId(), order.getOrderType(), order.getStatus(),
                order.getCreatedAt(), annexes, order.getApprovalStages().stream()
                .map(ApprovalStageResponse::from).toList());
    }
}
