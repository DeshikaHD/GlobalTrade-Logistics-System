package com.globaltrade.ejb;

import com.globaltrade.core.entity.Order;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface CarrierManagerLocal {
    void updateTransitStatus(Long orderId, String eventCode);
    List<Order> getShippedOrders();
}
