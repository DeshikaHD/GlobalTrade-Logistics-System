package com.globaltrade.ejb;

import com.globaltrade.core.entity.Order;
import jakarta.ejb.Remote;
import java.util.List;

@Remote
public interface CarrierManagerRemote {
    void updateTransitStatus(Long orderId, String eventCode);
    List<Order> getShippedOrders();
}
