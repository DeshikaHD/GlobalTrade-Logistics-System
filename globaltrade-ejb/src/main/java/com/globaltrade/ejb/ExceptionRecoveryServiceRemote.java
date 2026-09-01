package com.globaltrade.ejb;

import jakarta.ejb.Remote;

@Remote
public interface ExceptionRecoveryServiceRemote {
    void recoverFromCarrierFailure(String trackingNumber);
    void recoverFromCustomsRejection(Long shipmentId);
}
