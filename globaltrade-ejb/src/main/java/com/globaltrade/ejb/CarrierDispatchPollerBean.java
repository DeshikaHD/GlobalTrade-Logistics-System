package com.globaltrade.ejb;

import com.globaltrade.core.entity.Order;
import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Singleton
@Startup
public class CarrierDispatchPollerBean implements CarrierDispatchPollerLocal, CarrierDispatchPollerRemote {

    private static final Logger logger = Logger.getLogger(CarrierDispatchPollerBean.class.getName());

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    @EJB
    private CarrierTrackingSimulatorLocal simulatorBean;

    @Schedule(hour = "*", minute = "*", second = "*/15", persistent = false)
    public void pollDeliveryStatus() {
        // Find PACKED orders
        List<Order> packedOrders = em.createQuery("SELECT o FROM Order o WHERE o.status = 'PACKED'", Order.class).getResultList();
        for (Order order : packedOrders) {
            String trackingNumber = "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            order.setStatus("SHIPPED");
            logger.info("Order #" + order.getId() + " is now SHIPPED! Carrier tracking number: " + trackingNumber);
            em.merge(order);
        }

        // Find SHIPPED orders
        List<Order> shippedOrders = em.createQuery("SELECT o FROM Order o WHERE o.status = 'SHIPPED'", Order.class).getResultList();
        for (Order order : shippedOrders) {
            if (simulatorBean.hasArrived(order.getId())) {
                order.setStatus("DELIVERED");
                logger.info("Carrier update: Order #" + order.getId() + " has been DELIVERED to the hospital.");
                em.merge(order);
            } else {
                logger.info("Carrier update: Order #" + order.getId() + " is still in transit.");
            }
        }
    }
}
