package com.globaltrade.ejb;

import jakarta.ejb.Remote;

@Remote
public interface CarrierDispatchPollerRemote {
    void pollDeliveryStatus();
}
