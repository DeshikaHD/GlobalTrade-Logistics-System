package com.globaltrade.ejb;

import com.globaltrade.core.entity.Order;
import com.globaltrade.core.exception.CarrierTransitException;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.ejb.Local;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

@Stateless
@Local(CarrierManagerLocal.class)
@Remote(CarrierManagerRemote.class)
@RolesAllowed("CARRIER")
public class CarrierManagerBean implements CarrierManagerLocal, CarrierManagerRemote {

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    @EJB
    private ExceptionRecoveryServiceLocal recoveryService;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void updateTransitStatus(Long orderId, String eventCode) {
        Order order = em.find(Order.class, orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found with ID: " + orderId);
        }

        if ("DELIVERED".equalsIgnoreCase(eventCode)) {
            order.setStatus("DELIVERED");
            em.merge(order);
        } else if ("BREAKDOWN".equalsIgnoreCase(eventCode)) {
            recoveryService.recoverFromCarrierFailure(orderId);
            throw new CarrierTransitException("Carrier transit exception: Truck breakdown reported for order " + orderId);
        } else {
            throw new IllegalArgumentException("Unknown event code: " + eventCode);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<Order> getShippedOrders() {
        List<Order> orders = em.createQuery(
                "SELECT DISTINCT o FROM Order o " +
                "LEFT JOIN FETCH o.orderItems " +
                "LEFT JOIN FETCH o.customer " +
                "WHERE o.status = :status", Order.class)
                .setParameter("status", "SHIPPED")
                .getResultList();

        for (Order order : orders) {
            order.setOrderItems(new ArrayList<>(order.getOrderItems()));
        }

        return orders;
    }
}
