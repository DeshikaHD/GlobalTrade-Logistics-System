package com.globaltrade.ejb;

import jakarta.ejb.Local;

@Local
public interface ExceptionRecoveryServiceLocal {
    void recoverFromCarrierFailure(String trackingNumber);
    void recoverFromCustomsRejection(Long shipmentId);
}
