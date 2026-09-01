package com.globaltrade.ejb;

import jakarta.ejb.Remote;

@Remote
public interface ExceptionRecoveryServiceRemote {
    void recoverFromCarrierFailure(Long orderId);
    void recoverFromCustomsRejection(Long shipmentId);
}
