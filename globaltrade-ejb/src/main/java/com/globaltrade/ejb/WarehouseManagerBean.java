package com.globaltrade.ejb;

import com.globaltrade.core.entity.Inventory;
import com.globaltrade.core.entity.Order;
import com.globaltrade.core.entity.OrderItem;
import com.globaltrade.core.exception.InsufficientStockException;
import com.globaltrade.ejb.interceptor.AuditLoggingInterceptor;
import com.globaltrade.ejb.interceptor.PerformanceMonitoringInterceptor;
import jakarta.annotation.Resource;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Local;
import jakarta.ejb.Remote;
import jakarta.ejb.SessionContext;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.interceptor.Interceptors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@DeclareRoles({"WAREHOUSE_STAFF", "ADMIN"})
@Stateless
@Local(WarehouseManagerLocal.class)
@Remote(WarehouseManagerRemote.class)
@Interceptors({AuditLoggingInterceptor.class, PerformanceMonitoringInterceptor.class})
@RolesAllowed("WAREHOUSE_STAFF")
public class WarehouseManagerBean implements WarehouseManagerLocal, WarehouseManagerRemote {

    private static final Logger LOGGER = Logger.getLogger(WarehouseManagerBean.class.getName());

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    @Resource
    private SessionContext sessionContext;

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
        String callerName = sessionContext.getCallerPrincipal().getName();
        boolean isAdmin = sessionContext.isCallerInRole("ADMIN");
        LOGGER.info("Pack order requested by: " + callerName + " (isAdmin=" + isAdmin + ")");

        Order order = em.find(Order.class, orderId);
        if (order == null || !"PENDING".equals(order.getStatus())) {
            throw new IllegalArgumentException("Invalid or non-pending order ID: " + orderId);
        }

        if (!isAdmin) {
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
        } else {
            LOGGER.info("ADMIN override: Bypassing inventory check for order " + orderId);
        }

        order.setStatus("PACKED");
        em.merge(order);
    }
}
