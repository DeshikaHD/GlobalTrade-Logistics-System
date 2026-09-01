package com.globaltrade.ejb;

import com.globaltrade.core.entity.Order;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.annotation.Resource;
import jakarta.ejb.Local;
import jakarta.ejb.Remote;
import jakarta.ejb.ScheduleExpression;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.Timeout;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerConfig;
import jakarta.ejb.TimerService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.logging.Logger;

@Singleton
@Startup
@Local(RouteOptimizationTimerLocal.class)
@Remote(RouteOptimizationTimerRemote.class)
public class RouteOptimizationTimerBean implements RouteOptimizationTimerLocal, RouteOptimizationTimerRemote {

    private static final Logger LOGGER = Logger.getLogger(RouteOptimizationTimerBean.class.getName());

    @Resource
    private TimerService timerService;

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;

    @PostConstruct
    public void initializeTimer() {
        LOGGER.info("Initializing RouteOptimizationTimer programmatically via TimerService API...");
        ScheduleExpression schedule = new ScheduleExpression()
            .hour("*")
            .minute("*/15");
        TimerConfig config = new TimerConfig("RouteOptimizationTimer", false);
        timerService.createCalendarTimer(schedule, config);
        LOGGER.info("RouteOptimizationTimer created successfully. Next fire: every 15 minutes.");
    }

    @PreDestroy
    public void cleanupTimers() {
        LOGGER.info("Cleaning up RouteOptimizationTimer programmatic timers...");
        for (Timer timer : timerService.getTimers()) {
            if ("RouteOptimizationTimer".equals(timer.getInfo())) {
                timer.cancel();
                LOGGER.info("Cancelled RouteOptimizationTimer.");
            }
        }
    }

    @Timeout
    @Override
    public void optimizeRoutes(Timer timer) {
        LOGGER.info("Running Route Optimization Analysis (programmatic timer callback)...");

        List<Order> activeShipments = entityManager.createQuery(
            "SELECT o FROM Order o WHERE o.status IN ('SHIPPED', 'IN_TRANSIT')", Order.class)
            .getResultList();

        for (Order order : activeShipments) {
            int routeScore = calculateRouteScore(order);
            LOGGER.info("Order " + order.getTrackingNumber() +
                " route score: " + routeScore + "/100" +
                (routeScore < 50 ? " [SUBOPTIMAL - reroute recommended]" : " [OK]"));
        }

        LOGGER.info("Route Optimization complete. Analysed " + activeShipments.size() + " active shipments.");
    }

    private int calculateRouteScore(Order order) {
        return 40 + (int)(Math.random() * 60);
    }
}
