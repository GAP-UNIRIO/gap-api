package com.gap.api.Service;

import com.gap.api.Model.Entity.Order;
import com.gap.api.Model.Entity.OrderHistory;
import com.gap.api.Model.Entity.User;
import com.gap.api.Repository.OrderRepository;
import com.gap.api.Service.Interface.IOrderStatusService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderStatusService implements IOrderStatusService {

    static final String STATUS_CHANGED_EVENT = "STATUS_CHANGED";
    private static final HttpStatusCode UNPROCESSABLE_ENTITY = HttpStatusCode.valueOf(422);
    private static final Logger log = LoggerFactory.getLogger(OrderStatusService.class);

    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public Order transition(Order order, Order.OrderStatus target, User actor) {
        Order.OrderStatus current = order.getStatus();
        if (current.isFinal()) {
            throw new ResponseStatusException(UNPROCESSABLE_ENTITY,
                    "Order is in final status " + current + " and cannot change status");
        }
        if (!current.canTransitionTo(target)) {
            throw new ResponseStatusException(UNPROCESSABLE_ENTITY,
                    "Cannot change order status from " + current + " to " + target);
        }

        OrderHistory history = new OrderHistory();
        history.setOrder(order);
        history.setActor(actor);
        history.setEventType(STATUS_CHANGED_EVENT);
        history.setDescription("Status changed from " + current + " to " + target);
        history.setPayload(Map.of("from", current.name(), "to", target.name()));

        order.setStatus(target);
        order.getOrderHistories().add(history);
        Order saved = orderRepository.save(order);
        log.info("Order {} status changed from {} to {}", order.getId(), current, target);
        return saved;
    }
}
