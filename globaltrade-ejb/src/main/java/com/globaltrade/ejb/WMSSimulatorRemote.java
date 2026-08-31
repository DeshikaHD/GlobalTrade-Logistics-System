package com.globaltrade.ejb;

import jakarta.ejb.Remote;
import java.util.Map;

@Remote
public interface WMSSimulatorRemote {
    Map<String, Integer> getPhysicalStockLevels();
}
