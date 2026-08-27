package com.globaltrade.ejb;

import com.globaltrade.core.entity.Inventory;
import java.util.List;

public interface InventoryManagerLocal {
    List<Inventory> getAvailableInventory();
}
