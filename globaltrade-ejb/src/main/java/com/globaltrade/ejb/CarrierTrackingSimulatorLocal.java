package com.globaltrade.ejb;

import jakarta.ejb.Local;

@Local
public interface CarrierTrackingSimulatorLocal {
    boolean hasArrived(Long orderId);
}
