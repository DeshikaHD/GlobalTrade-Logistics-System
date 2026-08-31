package com.globaltrade.ejb;

import jakarta.ejb.Local;

@Local
public interface InventoryReplenishmentPollerLocal {
    void checkAndReplenishInventory();
}
