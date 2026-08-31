package com.globaltrade.ejb;

import com.globaltrade.core.entity.Order;
import jakarta.ejb.Local;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
@Local(ExceptionRecoveryServiceLocal.class)
@Remote(ExceptionRecoveryServiceRemote.class)
public class ExceptionRecoveryServiceBean implements ExceptionRecoveryServiceLocal, ExceptionRecoveryServiceRemote {

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void recoverFromCarrierFailure(Long orderId) {
        Order order = em.find(Order.class, orderId);
        if (order != null) {
            order.setStatus("DELAYED_TRANSIT_ISSUE");
            em.merge(order);
        }
    }
}
