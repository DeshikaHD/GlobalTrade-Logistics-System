package com.globaltrade.ejb;

import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Local;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import java.util.Random;

@Stateless
@PermitAll
@Local(CarrierTrackingSimulatorLocal.class)
@Remote(CarrierTrackingSimulatorRemote.class)
public class CarrierTrackingSimulatorBean implements CarrierTrackingSimulatorLocal, CarrierTrackingSimulatorRemote {

    private final Random random = new Random();

    @Override
    public boolean hasArrived(Long orderId) {
        // For simulation purposes, there is a 30% chance it arrives on each poll
        return random.nextInt(100) < 30;
    }
}
