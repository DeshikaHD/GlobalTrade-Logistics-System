package com.globaltrade.ejb;

import jakarta.ejb.Local;

@Local
public interface CarrierDispatchPollerLocal {
    void pollDeliveryStatus();
}
