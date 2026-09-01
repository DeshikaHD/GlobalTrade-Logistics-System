package com.globaltrade.ejb;

import com.globaltrade.core.entity.Order;
import com.globaltrade.core.entity.Shipment;
import com.globaltrade.core.enums.ShipmentStatus;
import jakarta.ejb.Local;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

import jakarta.annotation.PostConstruct;
import java.util.logging.Logger;

@Stateless
@Local(ExceptionRecoveryServiceLocal.class)
@Remote(ExceptionRecoveryServiceRemote.class)
public class ExceptionRecoveryServiceBean implements ExceptionRecoveryServiceLocal, ExceptionRecoveryServiceRemote {

    private static final Logger LOGGER = Logger.getLogger(ExceptionRecoveryServiceBean.class.getName());

    @PostConstruct
    public void init() {
        LOGGER.info("ExceptionRecoveryServiceBean ready - REQUIRES_NEW recovery operations available.");
    }

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void recoverFromCarrierFailure(String trackingNumber) {
        List<Order> orders = em.createQuery("SELECT o FROM Order o WHERE o.trackingNumber = :trackingNumber", Order.class)
                .setParameter("trackingNumber", trackingNumber).getResultList();
        if (!orders.isEmpty()) {
            Order order = orders.get(0);
            order.setStatus("DELAYED_TRANSIT_ISSUE");
            em.merge(order);
            return;
        }

        List<Shipment> shipments = em.createQuery("SELECT s FROM Shipment s WHERE s.trackingNumber = :trackingNumber", Shipment.class)
                .setParameter("trackingNumber", trackingNumber).getResultList();
        if (!shipments.isEmpty()) {
            Shipment shipment = shipments.get(0);
            shipment.setStatus(ShipmentStatus.BREAKDOWN);
            em.merge(shipment);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void recoverFromCustomsRejection(Long shipmentId) {
        Shipment shipment = em.find(Shipment.class, shipmentId);
        if (shipment != null) {
            shipment.setStatus(ShipmentStatus.CUSTOMS_PAPERWORK_REJECTED);
            em.merge(shipment);
        }
    }
}
