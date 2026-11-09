package com.gap.api.Service.Interface;

import com.gap.api.Model.Entity.Order;
import com.gap.api.Model.Entity.User;

public interface IOrderStatusService {

    /**
     * Aplica uma transição de status e registra a mudança no histórico da solicitação.
     * Lança HTTP 422 quando a transição não é permitida a partir do status atual.
     */
    Order transition(Order order, Order.OrderStatus target, User actor);
}
