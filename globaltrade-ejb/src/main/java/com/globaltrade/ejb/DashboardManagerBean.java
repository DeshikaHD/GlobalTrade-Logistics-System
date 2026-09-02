package com.globaltrade.ejb;

import com.globaltrade.core.entity.Inventory;
import com.globaltrade.core.entity.Order;
import com.globaltrade.core.entity.SupplierOrder;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Local;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.ArrayList;

@Stateless
@DeclareRoles({"LOGISTICS_COORDINATOR", "WAREHOUSE_STAFF"})
@RolesAllowed({"LOGISTICS_COORDINATOR", "WAREHOUSE_STAFF"})
@Local(DashboardManagerLocal.class)
@Remote(DashboardManagerRemote.class)
public class DashboardManagerBean implements DashboardManagerLocal, DashboardManagerRemote {

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    @Override
    public List<Order> getAllOutboundOrders() {
        List<Order> orders = em.createQuery(
            "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.orderItems JOIN FETCH o.customer", Order.class)
            .getResultList();
        
        List<Order> safeOrders = new ArrayList<>();
        for (Order o : orders) {
            em.detach(o);
            // Initialize collection fully if needed, though fetch join does it
            o.setOrderItems(new ArrayList<>(o.getOrderItems()));
            safeOrders.add(o);
        }
        return safeOrders;
    }

    @Override
    public List<SupplierOrder> getAllInboundOrders() {
        return em.createQuery("SELECT s FROM SupplierOrder s JOIN FETCH s.vendor", SupplierOrder.class).getResultList();
    }

    @Override
    public List<Inventory> getAllInventory() {
        return em.createQuery("SELECT i FROM Inventory i", Inventory.class).getResultList();
    }
}
