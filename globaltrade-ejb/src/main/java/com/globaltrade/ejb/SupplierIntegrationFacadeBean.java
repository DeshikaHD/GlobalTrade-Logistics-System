package com.globaltrade.ejb;

import com.globaltrade.core.entity.Shipment;
import com.globaltrade.core.entity.SupplierEvaluation;
import com.globaltrade.core.entity.SupplierOrder;
import com.globaltrade.core.enums.ShipmentStatus;
import com.globaltrade.core.exception.InvalidOrderStateException;
import com.globaltrade.ejb.interceptor.AuditLoggingInterceptor;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.interceptor.Interceptors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Stateless
@RolesAllowed("VENDOR")
public class SupplierIntegrationFacadeBean implements SupplierIntegrationFacadeRemote {

    private static final Logger LOGGER = Logger.getLogger(SupplierIntegrationFacadeBean.class.getName());

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public boolean ping() {
        return true;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<SupplierOrder> getActiveOrdersForVendor(Long vendorId) {
        List<SupplierOrder> orders = entityManager.createQuery(
            "SELECT o FROM SupplierOrder o LEFT JOIN FETCH o.vendor WHERE o.vendor.id = :vendorId AND o.status = 'REQUESTED'", SupplierOrder.class)
            .setParameter("vendorId", vendorId)
            .getResultList();
        
        return new ArrayList<>(orders);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<SupplierEvaluation> getVendorEvaluations(Long vendorId) {
        List<SupplierEvaluation> evaluations = entityManager.createQuery(
            "SELECT e FROM SupplierEvaluation e LEFT JOIN FETCH e.vendor WHERE e.vendor.id = :vendorId", SupplierEvaluation.class)
            .setParameter("vendorId", vendorId)
            .getResultList();
        
        return new ArrayList<>(evaluations);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Interceptors(AuditLoggingInterceptor.class)
    public SupplierOrder fulfillOrder(Long orderId, String trackingNumber, boolean hasDocs) {
        SupplierOrder order = entityManager.find(SupplierOrder.class, orderId);
        
        if (order == null) {
            throw new IllegalArgumentException("Order not found with ID: " + orderId);
        }
        
        if (!"REQUESTED".equals(order.getStatus())) {
            throw new InvalidOrderStateException("Order is not in REQUESTED state. Current state: " + order.getStatus());
        }

        Shipment shipment = new Shipment(trackingNumber != null ? trackingNumber : "TRK-" + System.currentTimeMillis(), ShipmentStatus.READY_FOR_EXPORT);
        entityManager.persist(shipment);

        order.setStatus("FULFILLED");
        order.setShipment(shipment);
        order.setTradeDocumentationProvided(hasDocs);
        
        // Simulating the receipt right away for the evaluation timer
        order.setExpectedDeliveryDate(java.time.LocalDate.now().plusDays(5));
        order.setReceivedDate(LocalDateTime.now());
        order.setQuantityAccepted(order.getQuantity()); 

        entityManager.merge(order);
        
        LOGGER.info("Order " + orderId + " fulfilled with tracking " + shipment.getTrackingNumber());
        return order;
    }
}
