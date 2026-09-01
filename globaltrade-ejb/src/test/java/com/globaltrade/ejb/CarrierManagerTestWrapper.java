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

    public void updateTransitStatus(String trackingNumber, String eventCode) {
        carrierManager.updateTransitStatus(trackingNumber, eventCode);
    }
}
