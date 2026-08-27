package com.globaltrade.ejb;

import com.globaltrade.core.entity.Inventory;
import java.util.List;

public interface InventoryManagerRemote {
    List<Inventory> getAvailableInventory();
}
