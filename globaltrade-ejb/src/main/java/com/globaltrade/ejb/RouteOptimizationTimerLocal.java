package com.globaltrade.ejb;

import jakarta.ejb.Local;
import jakarta.ejb.Timer;

@Local
public interface RouteOptimizationTimerLocal {
    void optimizeRoutes(Timer timer);
}
