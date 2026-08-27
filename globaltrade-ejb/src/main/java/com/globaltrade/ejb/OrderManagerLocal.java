package com.globaltrade.ejb;

import com.globaltrade.core.entity.Order;
import com.globaltrade.core.entity.OrderItem;
import java.util.List;

public interface OrderManagerLocal {
    Order placeOrder(Long customerId, List<OrderItem> items);
    List<Order> getOrdersForCustomer(Long customerId);
}
