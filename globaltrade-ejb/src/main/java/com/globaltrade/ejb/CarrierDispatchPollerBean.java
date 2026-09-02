package com.globaltrade.ejb;

import com.globaltrade.core.entity.Order;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.ejb.Local;
import jakarta.ejb.Lock;
import jakarta.ejb.LockType;
import jakarta.ejb.Remote;
import jakarta.ejb.ScheduleExpression;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.Timeout;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerConfig;
import jakarta.ejb.TimerService;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

@Singleton
@Startup
@Lock(LockType.READ)
@Local(CarrierDispatchPollerLocal.class)
@Remote(CarrierDispatchPollerRemote.class)
public class CarrierDispatchPollerBean implements CarrierDispatchPollerLocal, CarrierDispatchPollerRemote {

    private static final Logger logger = Logger.getLogger(CarrierDispatchPollerBean.class.getName());

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    @EJB
    private CarrierTrackingSimulatorLocal simulatorBean;

    @Resource
    private TimerService timerService;

    @PostConstruct
    public void init() {
        logger.info("Initializing CarrierDispatchPoller timer...");
        for (Timer t : timerService.getTimers()) {
            if ("CarrierDispatchPollerTimer".equals(t.getInfo())) {
                t.cancel();
            }
        }
        ScheduleExpression schedule = new ScheduleExpression()
            .hour("*")
            .minute("*")
            .second("*/15");
        TimerConfig config = new TimerConfig("CarrierDispatchPollerTimer", false);
        timerService.createCalendarTimer(schedule, config);
        logger.info("CarrierDispatchPoller timer created. Fires every 15 seconds.");
    }

    @PreDestroy
    public void cleanup() {
        for (Timer t : timerService.getTimers()) {
            if ("CarrierDispatchPollerTimer".equals(t.getInfo())) {
                t.cancel();
            }
        }
    }

    @Timeout
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void onTimeout(Timer timer) {
        pollDeliveryStatus();
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void pollDeliveryStatus() {
        List<Order> packedOrders = em.createQuery(
            "SELECT o FROM Order o WHERE o.status = 'PACKED'", Order.class).getResultList();

        for (Order order : packedOrders) {
            String trackingNumber = "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            order.setStatus("SHIPPED");
            order.setTrackingNumber(trackingNumber);
            logger.info("Order #" + order.getId() + " -> SHIPPED. Tracking: " + trackingNumber);
            em.merge(order);
        }

        List<Order> shippedOrders = em.createQuery(
            "SELECT o FROM Order o WHERE o.status = 'SHIPPED'", Order.class).getResultList();

        for (Order order : shippedOrders) {
            if (simulatorBean.hasArrived(order.getId())) {
                order.setStatus("DELIVERED");
                logger.info("Order #" + order.getId() + " -> DELIVERED.");
                em.merge(order);
            } else {
                logger.info("Order #" + order.getId() + " still in transit.");
            }
        }
    }
}
