package com.gap.api.Service;

import com.gap.api.Model.DTO.*;
import com.gap.api.Model.Entity.Annex;
import com.gap.api.Model.Entity.Order;
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

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final IStorageService storageService;
    private final WorkflowEngineService workflowEngineService;
    private final IOrderStatusService orderStatusService;

    @Override
    @Transactional
    public OrderResponse create(OrderRequest request, String authenticatedEmail) {
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
    public AnnexUploadResponse generateAnnexUploadUrl(AnnexUploadRequest request) {
        IStorageService.UploadUrlResponse response = storageService.generateUploadUrl(request.name(), request.contentType());
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

    private OrderResponse toResponse(Order order) {
        List<AnnexResponse> annexes = order.getAnnexes().stream()
                .map(annex -> new AnnexResponse(annex.getId(), annex.getName(), annex.getContentType(), annex.getUrl()))
                .toList();
        return new OrderResponse(order.getId(), order.getUser().getId(), order.getOrderType(), order.getStatus(),
                order.getCreatedAt(), annexes);
    }
}
