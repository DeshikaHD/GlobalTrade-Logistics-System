package com.globaltrade.ejb;

import com.globaltrade.core.entity.CustomsDeclaration;
import com.globaltrade.core.enums.ShipmentStatus;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;

@Singleton
@Startup
public class CustomsMonitorTimerBean {

    private static final Logger LOGGER = Logger.getLogger(CustomsMonitorTimerBean.class.getName());

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;

    // Running every 5 minutes for demonstration purposes. In production, this might be once a day.
    @Schedule(hour = "*", minute = "*/5", persistent = false)
    public void monitorStuckShipments() {
        LOGGER.info("Running CustomsMonitorTimerBean to check for stuck shipments...");
        
        // Threshold is 48 hours ago
        LocalDateTime thresholdDate = LocalDateTime.now().minusHours(48);
        
        List<CustomsDeclaration> delayedDeclarations = entityManager.createQuery(
            "SELECT d FROM CustomsDeclaration d " +
            "WHERE d.shipment.status = :status AND d.submissionDate < :thresholdDate", CustomsDeclaration.class)
            .setParameter("status", ShipmentStatus.AT_BORDER_PENDING_CLEARANCE)
            .setParameter("thresholdDate", thresholdDate)
            .getResultList();
            
        for (CustomsDeclaration declaration : delayedDeclarations) {
            LOGGER.warning("CRITICAL ALERT: Shipment " + declaration.getShipment().getTrackingNumber() + 
                           " has been pending customs clearance for over 48 hours. Port demurrage fees may apply!");
        }
    }
}
