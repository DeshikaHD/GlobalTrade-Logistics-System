package com.globaltrade.ejb;

import com.globaltrade.core.entity.Shipment;
import com.globaltrade.core.enums.ShipmentStatus;
import com.globaltrade.core.exception.CustomsClearanceRejectedException;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RunAs;
import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
@Startup
@RunAs("CUSTOMS_OFFICIAL")
@PermitAll
public class AutomatedCustomsFilingTimerBean {

    private static final Logger LOGGER = Logger.getLogger(AutomatedCustomsFilingTimerBean.class.getName());

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;

    @EJB
    private CustomsGatewayLocal customsGateway;

    @EJB
    private ExceptionRecoveryServiceLocal recoveryService;

    // Runs every 2 minutes for demonstration
    @Schedule(hour = "*", minute = "*/2", persistent = false)
    public void processAutomatedFiling() {
        LOGGER.info("Running AutomatedCustomsFilingTimerBean to file export paperwork...");
        
        List<Shipment> shipmentsForExport = entityManager.createQuery(
            "SELECT s FROM Shipment s WHERE s.status = :status", Shipment.class)
            .setParameter("status", ShipmentStatus.READY_FOR_EXPORT)
            .getResultList();
            
        for (Shipment shipment : shipmentsForExport) {
            try {
                // In a real scenario, this data would come from the supplier's order or system.
                // We're simulating the automated generation of customs documents.
                // We purposefully create a 20% chance to simulate a rejection (e.g. unpaid taxes) 
                // to demonstrate the ExceptionRecoveryService to the examiner.
                Double simulatedTax = Math.random() > 0.8 ? 0.0 : 1500.0;
                String simulatedBroker = "AutoBroker-System";
                String simulatedHsCode = "HS-123456";
                
                customsGateway.submitDeclaration(shipment.getId(), simulatedHsCode, simulatedTax, simulatedBroker);
                LOGGER.info("Successfully filed customs declaration for shipment: " + shipment.getTrackingNumber());
                
            } catch (CustomsClearanceRejectedException e) {
                LOGGER.log(Level.SEVERE, "Customs clearance rejected for shipment: " + shipment.getTrackingNumber() + ". Triggering REQUIRES_NEW recovery...", e);
                // The transaction for submitDeclaration has been marked for rollback.
                // We use our REQUIRES_NEW recovery service to safely update the shipment status in a new transaction.
                recoveryService.recoverFromCustomsRejection(shipment.getId());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Unexpected error during automated customs filing for shipment: " + shipment.getTrackingNumber(), e);
            }
        }
    }
}
