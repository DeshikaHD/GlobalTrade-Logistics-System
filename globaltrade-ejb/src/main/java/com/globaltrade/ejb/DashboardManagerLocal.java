package com.globaltrade.ejb;

import com.globaltrade.core.entity.Inventory;
import com.globaltrade.core.entity.Order;
import com.globaltrade.core.entity.SupplierOrder;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface DashboardManagerLocal {
    List<Order> getAllOutboundOrders();
    List<SupplierOrder> getAllInboundOrders();
    List<Inventory> getAllInventory();
}
