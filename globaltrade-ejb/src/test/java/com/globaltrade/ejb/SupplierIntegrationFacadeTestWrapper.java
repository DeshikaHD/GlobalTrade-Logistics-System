package com.globaltrade.ejb;

import com.globaltrade.core.entity.SupplierEvaluation;
import com.globaltrade.core.entity.SupplierOrder;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RunAs;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import java.util.List;

@Stateless
@RunAs("VENDOR")
@PermitAll
public class SupplierIntegrationFacadeTestWrapper {

    @EJB
    private SupplierIntegrationFacadeRemote facade;

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public boolean ping() {
        return facade.ping();
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<SupplierOrder> getActiveOrdersForVendor(Long vendorId) {
        return facade.getActiveOrdersForVendor(vendorId);
    }

    @TransactionAttribute(TransactionAttributeType.SUPPORTS)
    public List<SupplierEvaluation> getVendorEvaluations(Long vendorId) {
        return facade.getVendorEvaluations(vendorId);
    }

    public SupplierOrder fulfillOrder(Long orderId, String trackingNumber, boolean hasDocs) {
        return facade.fulfillOrder(orderId, trackingNumber, hasDocs);
    }
}
