package com.globaltrade.ejb;

import jakarta.ejb.Remote;
import jakarta.ejb.Timer;

@Remote
public interface RouteOptimizationTimerRemote {
    void optimizeRoutes(Timer timer);
}
