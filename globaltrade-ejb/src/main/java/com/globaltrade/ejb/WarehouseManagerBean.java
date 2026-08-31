package com.globaltrade.ejb;

import com.globaltrade.core.entity.Inventory;
import com.globaltrade.core.entity.Order;
import com.globaltrade.core.entity.OrderItem;
import com.globaltrade.core.exception.InsufficientStockException;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

@Stateless
@RolesAllowed("WAREHOUSE_STAFF")
public class WarehouseManagerBean implements WarehouseManagerLocal, WarehouseManagerRemote {

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Order> getPendingOrders() {
        List<Order> orders = em.createQuery(
                "SELECT DISTINCT o FROM Order o " +
                "LEFT JOIN FETCH o.orderItems " +
                "LEFT JOIN FETCH o.customer " +
                "WHERE o.status = :status", Order.class)
                .setParameter("status", "PENDING")
                .getResultList();

        for (Order order : orders) {
            order.setOrderItems(new ArrayList<>(order.getOrderItems()));
        }

        return orders;
    }

    @Override
    public void packOrder(Long orderId) {
        Order order = em.find(Order.class, orderId);
        if (order == null || !"PENDING".equals(order.getStatus())) {
            throw new IllegalArgumentException("Invalid or non-pending order ID: " + orderId);
        }

        for (OrderItem item : order.getOrderItems()) {
            Inventory inventory = item.getInventory();
            int currentQuantity = inventory.getQuantityAvailable();
            int requestedQuantity = item.getQuantity();

            if (currentQuantity < requestedQuantity) {
                throw new InsufficientStockException("Not enough stock for product: " + inventory.getProductName());
            }

            inventory.setQuantityAvailable(currentQuantity - requestedQuantity);
            em.merge(inventory);
        }

        order.setStatus("PACKED");
        em.merge(order);
    }
}
