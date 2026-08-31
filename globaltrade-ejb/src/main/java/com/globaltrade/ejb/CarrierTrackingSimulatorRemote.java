package com.globaltrade.ejb;

import jakarta.ejb.Remote;

@Remote
public interface CarrierTrackingSimulatorRemote {
    boolean hasArrived(Long orderId);
}
