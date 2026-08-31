package com.globaltrade.ejb;

import jakarta.ejb.Local;
import java.util.Map;

@Local
public interface WMSSimulatorLocal {
    Map<String, Integer> getPhysicalStockLevels();
    void setMockStockLevel(String sku, Integer quantity);
}
