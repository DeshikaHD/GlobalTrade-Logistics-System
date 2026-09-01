package com.globaltrade.ejb;

import jakarta.ejb.Local;

@Local
public interface ExceptionRecoveryServiceLocal {
    void recoverFromCarrierFailure(Long orderId);
    void recoverFromCustomsRejection(Long shipmentId);
}
