package com.globaltrade.ejb;

import jakarta.ejb.Remote;

@Remote
public interface InventoryReplenishmentPollerRemote {
    void checkAndReplenishInventory();
}
