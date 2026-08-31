package com.globaltrade.ejb;

import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RunAs;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

@Stateless
@RunAs("CARRIER")
@PermitAll
public class CarrierManagerTestWrapper {

    @EJB
    private CarrierManagerLocal carrierManager;

    public void updateTransitStatus(Long orderId, String eventCode) {
        carrierManager.updateTransitStatus(orderId, eventCode);
    }
}
