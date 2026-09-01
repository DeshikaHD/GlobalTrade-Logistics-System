package com.globaltrade.ejb;

import com.globaltrade.core.entity.Order;
import com.globaltrade.core.exception.CarrierTransitException;
import jakarta.annotation.security.DeclareRoles;
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
import com.globaltrade.core.entity.Shipment;
import com.globaltrade.core.enums.ShipmentStatus;
import com.globaltrade.core.entity.SupplierOrder;
import com.globaltrade.core.entity.Inventory;

@DeclareRoles({"CARRIER", "ADMIN"})
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
    public void updateTransitStatus(String trackingNumber, String eventCode) {
        List<Shipment> shipments = em.createQuery("SELECT s FROM Shipment s WHERE s.trackingNumber = :trackingNumber", Shipment.class)
                .setParameter("trackingNumber", trackingNumber)
                .getResultList();
        
        if (!shipments.isEmpty()) {
            Shipment shipment = shipments.get(0);
            if ("DELIVERED".equalsIgnoreCase(eventCode)) {
                shipment.setStatus(ShipmentStatus.DELIVERED);
                em.merge(shipment);
                
                List<SupplierOrder> supplierOrders = em.createQuery(
                        "SELECT so FROM SupplierOrder so WHERE so.shipment = :shipment", SupplierOrder.class)
                        .setParameter("shipment", shipment)
                        .getResultList();
                
                for (SupplierOrder so : supplierOrders) {
                    so.setStatus("RECEIVED");
                    so.setReceivedDate(java.time.LocalDateTime.now());
                    em.merge(so);
                    
                    List<Inventory> inventories = em.createQuery(
                            "SELECT i FROM Inventory i WHERE i.sku = :sku", Inventory.class)
                            .setParameter("sku", so.getSku())
                            .getResultList();
                            
                    if (!inventories.isEmpty()) {
                        Inventory inventory = inventories.get(0);
                        inventory.setQuantityAvailable(inventory.getQuantityAvailable() + so.getQuantity());
                        em.merge(inventory);
                    }
                }
            } else if ("BREAKDOWN".equalsIgnoreCase(eventCode)) {
                recoveryService.recoverFromCarrierFailure(trackingNumber);
                throw new CarrierTransitException("Carrier transit exception: Truck breakdown reported for inbound tracking " + trackingNumber);
            } else if ("IN_TRANSIT".equalsIgnoreCase(eventCode)) {
                shipment.setStatus(ShipmentStatus.IN_TRANSIT);
                em.merge(shipment);
            } else {
                throw new IllegalArgumentException("Unknown event code for shipment: " + eventCode);
            }
            return;
        }

        List<Order> orders = em.createQuery("SELECT o FROM Order o WHERE o.trackingNumber = :trackingNumber", Order.class)
                .setParameter("trackingNumber", trackingNumber)
                .getResultList();
                
        if (!orders.isEmpty()) {
            Order order = orders.get(0);
            if ("DELIVERED".equalsIgnoreCase(eventCode)) {
                order.setStatus("DELIVERED");
                em.merge(order);
            } else if ("BREAKDOWN".equalsIgnoreCase(eventCode)) {
                recoveryService.recoverFromCarrierFailure(trackingNumber);
                throw new CarrierTransitException("Carrier transit exception: Truck breakdown reported for outbound tracking " + trackingNumber);
            } else if ("IN_TRANSIT".equalsIgnoreCase(eventCode)) {
                order.setStatus("IN_TRANSIT");
                em.merge(order);
            } else {
                throw new IllegalArgumentException("Unknown event code for order: " + eventCode);
            }
            return;
        }
        
        throw new IllegalArgumentException("No Shipment or Order found with tracking number: " + trackingNumber);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<String> getManifest() {
        List<String> manifest = new ArrayList<>();
        
        List<Order> orders = em.createQuery(
                "SELECT DISTINCT o FROM Order o " +
                "LEFT JOIN FETCH o.orderItems " +
                "LEFT JOIN FETCH o.customer " +
                "WHERE o.status = :status", Order.class)
                .setParameter("status", "SHIPPED")
                .getResultList();

        for (Order order : orders) {
            manifest.add(order.getTrackingNumber());
        }

        List<Shipment> shipments = em.createQuery(
                "SELECT s FROM Shipment s WHERE s.status = :status", Shipment.class)
                .setParameter("status", ShipmentStatus.CLEARED_CUSTOMS)
                .getResultList();

        for (Shipment shipment : shipments) {
            manifest.add(shipment.getTrackingNumber());
        }

        return manifest;
    }
}
