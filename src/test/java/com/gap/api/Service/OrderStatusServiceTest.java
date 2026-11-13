package com.gap.api.Service;

import com.gap.api.Model.Entity.Order;
import com.gap.api.Model.Entity.OrderHistory;
import com.gap.api.Model.Entity.User;
import com.gap.api.Repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderStatusServiceTest {

    @Mock private OrderRepository orderRepository;
    @InjectMocks private OrderStatusService orderStatusService;

    private User actor;

    @BeforeEach
    void setUp() {
        actor = new User();
        actor.setId(9L);
        actor.setEmail("secretaria@example.test");
    }

    @Test
    @DisplayName("@spec:AC-133 aplica transição permitida e registra no histórico")
    void transition_allowedTransition_changesStatusAndRecordsHistory() {
        Order order = order(Order.OrderStatus.ABERTA);
        when(orderRepository.save(order)).thenReturn(order);

        Order result = orderStatusService.transition(order, Order.OrderStatus.EM_ANALISE, actor);

        assertEquals(Order.OrderStatus.EM_ANALISE, result.getStatus());
        assertEquals(1, order.getOrderHistories().size());
        OrderHistory history = order.getOrderHistories().get(0);
        assertSame(order, history.getOrder());
        assertSame(actor, history.getActor());
        assertEquals("STATUS_CHANGED", history.getEventType());
        assertEquals(Map.of("from", "ABERTA", "to", "EM_ANALISE"), history.getPayload());
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("@spec:AC-134 recusa transição não permitida com HTTP 422")
    void transition_forbiddenTransition_throwsUnprocessableAndKeepsStatus() {
        Order order = order(Order.OrderStatus.DEFERIDA);

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> orderStatusService.transition(order, Order.OrderStatus.ABERTA, actor));

        assertEquals(422, error.getStatusCode().value());
        assertEquals(Order.OrderStatus.DEFERIDA, order.getStatus());
        assertTrue(order.getOrderHistories().isEmpty());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    @DisplayName("@spec:AC-135 bloqueia qualquer transição a partir de status final")
    void transition_fromFinalStatus_throwsUnprocessableForEveryTarget() {
        for (Order.OrderStatus finalStatus : new Order.OrderStatus[]{
                Order.OrderStatus.INDEFERIDA, Order.OrderStatus.CANCELADA, Order.OrderStatus.CONCLUIDA}) {
            for (Order.OrderStatus target : Order.OrderStatus.values()) {
                Order order = order(finalStatus);

                ResponseStatusException error = assertThrows(ResponseStatusException.class,
                        () -> orderStatusService.transition(order, target, actor));

                assertEquals(422, error.getStatusCode().value());
                assertEquals(finalStatus, order.getStatus());
                assertTrue(order.getOrderHistories().isEmpty());
            }
        }
        verify(orderRepository, never()).save(any(Order.class));
    }

    private Order order(Order.OrderStatus status) {
        Order order = new Order();
        order.setId(23L);
        order.setStatus(status);
        return order;
    }
}
