package com.globaltrade.ejb;

import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

@Singleton
@Startup
public class WarehouseManagementSystemSimulatorBean implements WMSSimulatorLocal, WMSSimulatorRemote {

    private static final Logger LOGGER = Logger.getLogger(WarehouseManagementSystemSimulatorBean.class.getName());
    
    private Map<String, Integer> physicalStock = new HashMap<>();

    @Override
    public Map<String, Integer> getPhysicalStockLevels() {
        LOGGER.info("WMS Simulator providing physical stock levels...");
        return new HashMap<>(physicalStock);
    }

    @Override
    public void setMockStockLevel(String sku, Integer quantity) {
        physicalStock.put(sku, quantity);
        LOGGER.info("WMS Simulator mock stock set for " + sku + " to " + quantity);
    }
}
