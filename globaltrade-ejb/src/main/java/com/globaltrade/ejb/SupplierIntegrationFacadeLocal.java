package com.globaltrade.ejb;

import com.globaltrade.core.entity.SupplierEvaluation;
import com.globaltrade.core.entity.SupplierOrder;
import jakarta.ejb.Local;
import java.util.List;

@Local
public interface SupplierIntegrationFacadeLocal {

    boolean ping();

    List<SupplierOrder> getActiveOrdersForVendor(Long vendorId);

    List<SupplierEvaluation> getVendorEvaluations(Long vendorId);

    SupplierOrder fulfillOrder(Long orderId, String trackingNumber, boolean hasDocs);
}
