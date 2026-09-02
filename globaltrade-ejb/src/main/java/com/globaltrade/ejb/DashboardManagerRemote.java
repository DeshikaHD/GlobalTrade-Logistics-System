package com.globaltrade.ejb;

import com.globaltrade.core.entity.Inventory;
import com.globaltrade.core.entity.Order;
import com.globaltrade.core.entity.SupplierOrder;
import jakarta.ejb.Remote;
import java.util.List;

@Remote
public interface DashboardManagerRemote {
    List<Order> getAllOutboundOrders();
    List<SupplierOrder> getAllInboundOrders();
    List<Inventory> getAllInventory();
}
