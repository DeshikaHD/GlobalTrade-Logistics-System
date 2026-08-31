package com.globaltrade.ejb;

import com.globaltrade.core.entity.Inventory;
import java.util.List;
import jakarta.ejb.Local;

@Local
public interface InventoryManagerLocal {
    List<Inventory> getAvailableInventory();
    void updateInventoryQuantity(String sku, int newQuantity);
}
