package com.globaltrade.ejb;

import com.globaltrade.core.entity.CustomsDeclaration;
import com.globaltrade.core.entity.Shipment;
import com.globaltrade.core.enums.ShipmentStatus;
import com.globaltrade.core.exception.CustomsClearanceRejectedException;
import com.globaltrade.ejb.interceptor.CustomsComplianceInterceptor;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.interceptor.ExcludeClassInterceptors;
import jakarta.interceptor.Interceptors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

@Stateless
@RolesAllowed("CUSTOMS_OFFICIAL")
@Interceptors(CustomsComplianceInterceptor.class)
public class CustomsGatewayBean implements CustomsGatewayLocal, CustomsGatewayRemote {

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    @ExcludeClassInterceptors
    public boolean ping() {
        return true;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public CustomsDeclaration submitDeclaration(Long shipmentId, String hsCode, Double taxPaid, String brokerName) {
        if (taxPaid == null || taxPaid <= 0) {
            throw new CustomsClearanceRejectedException("Customs clearance rejected: Unpaid taxes.");
        }
        if (brokerName == null || brokerName.trim().isEmpty() || hsCode == null || hsCode.trim().isEmpty()) {
            throw new CustomsClearanceRejectedException("Customs clearance rejected: Missing required documents or details.");
        }

        Shipment shipment = entityManager.find(Shipment.class, shipmentId);
        if (shipment == null) {
            throw new IllegalArgumentException("Shipment not found with ID: " + shipmentId);
        }
        
        CustomsDeclaration declaration = new CustomsDeclaration(hsCode, taxPaid, brokerName, shipment);
        entityManager.persist(declaration);
        
        shipment.setStatus(ShipmentStatus.AT_BORDER_PENDING_CLEARANCE);
        entityManager.merge(shipment);
        
        return declaration;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void approveShipment(Long shipmentId) {
        Shipment shipment = entityManager.find(Shipment.class, shipmentId);
        if (shipment != null) {
            shipment.setStatus(ShipmentStatus.CLEARED);
            entityManager.merge(shipment);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void rejectShipment(Long shipmentId) {
        Shipment shipment = entityManager.find(Shipment.class, shipmentId);
        if (shipment != null) {
            shipment.setStatus(ShipmentStatus.CUSTOMS_PAPERWORK_REJECTED);
            entityManager.merge(shipment);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    @ExcludeClassInterceptors
    public List<Shipment> getPendingClearanceShipments() {
        List<Shipment> shipments = entityManager.createQuery(
            "SELECT s FROM Shipment s WHERE s.status = :status", Shipment.class)
            .setParameter("status", ShipmentStatus.AT_BORDER_PENDING_CLEARANCE)
            .getResultList();
        
        return new ArrayList<>(shipments);
    }
}
