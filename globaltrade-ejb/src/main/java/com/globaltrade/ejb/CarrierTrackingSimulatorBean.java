package com.globaltrade.ejb;

import jakarta.ejb.Stateless;
import java.util.Random;

@Stateless
public class CarrierTrackingSimulatorBean implements CarrierTrackingSimulatorLocal, CarrierTrackingSimulatorRemote {

    private final Random random = new Random();

    public boolean hasArrived(Long orderId) {
        // For simulation purposes, there is a 30% chance it arrives on each poll
        return random.nextInt(100) < 30;
    }
}
