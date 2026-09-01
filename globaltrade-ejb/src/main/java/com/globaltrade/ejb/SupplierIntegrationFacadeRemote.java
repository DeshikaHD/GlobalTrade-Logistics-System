package com.globaltrade.ejb;

import com.globaltrade.core.entity.SupplierEvaluation;
import com.globaltrade.core.entity.SupplierOrder;
import jakarta.ejb.Remote;
import java.util.List;

@Remote
public interface SupplierIntegrationFacadeRemote {
    
    // Auth ping method
    boolean ping();

    List<SupplierOrder> getActiveOrdersForVendor(Long vendorId);

    List<SupplierEvaluation> getVendorEvaluations(Long vendorId);

    SupplierOrder fulfillOrder(Long orderId, String trackingNumber, boolean hasDocs);
}
