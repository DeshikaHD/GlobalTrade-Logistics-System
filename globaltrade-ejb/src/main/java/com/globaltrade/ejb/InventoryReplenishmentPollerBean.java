package com.globaltrade.ejb;

import com.globaltrade.core.entity.Inventory;
import com.globaltrade.core.exception.VendorSystemOutageException;
import jakarta.ejb.EJB;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Singleton
@Startup
public class InventoryReplenishmentPollerBean implements InventoryReplenishmentPollerLocal, InventoryReplenishmentPollerRemote {

    private static final Logger LOGGER = Logger.getLogger(InventoryReplenishmentPollerBean.class.getName());

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;

    @EJB
    private SupplierOrderManagerLocal supplierOrderManager;

    @EJB
    private WMSSimulatorLocal wmsSimulator;

    @EJB
    private InventoryManagerLocal inventoryManager;

    @Override
    @Schedule(hour = "*", minute = "*/30", persistent = true)
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void checkAndReplenishInventory() {
        LOGGER.info("Starting scheduled inventory replenishment check...");

        try {
            // WMS Reconciliation Pre-Phase
            Map<String, Integer> physicalStock = wmsSimulator.getPhysicalStockLevels();
            for (Map.Entry<String, Integer> entry : physicalStock.entrySet()) {
                String sku = entry.getKey();
                Integer physicalQuantity = entry.getValue();
                
                try {
                    TypedQuery<Inventory> invQuery = entityManager.createQuery("SELECT i FROM Inventory i WHERE i.sku = :sku", Inventory.class);
                    invQuery.setParameter("sku", sku);
                    List<Inventory> results = invQuery.getResultList();
                    
                    if (!results.isEmpty()) {
                        Inventory currentInv = results.get(0);
                        if (!currentInv.getQuantityAvailable().equals(physicalQuantity)) {
                            LOGGER.info("Stock discrepancy detected for SKU " + sku + ". DB: " + currentInv.getQuantityAvailable() + ", WMS: " + physicalQuantity + ". Reconciling...");
                            inventoryManager.updateInventoryQuantity(sku, physicalQuantity);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to reconcile stock for SKU " + sku + ": " + e.getMessage());
                }
            }

            // 1. Query all Inventory where quantity < reorderThreshold
            TypedQuery<Inventory> query = entityManager.createQuery(
                    "SELECT i FROM Inventory i WHERE i.quantityAvailable < i.reorderThreshold", Inventory.class);
            List<Inventory> lowStockItems = query.getResultList();

            for (Inventory item : lowStockItems) {
                // Check if primary vendor exists
                if (item.getPrimaryVendor() == null) {
                    LOGGER.warning("SKU " + item.getSku() + " is low on stock, but has no primary vendor assigned. Skipping.");
                    continue;
                }

                // 2. Check if there's already a 'REQUESTED' SupplierOrder for this SKU
                TypedQuery<Long> orderQuery = entityManager.createQuery(
                        "SELECT COUNT(o) FROM SupplierOrder o WHERE o.sku = :sku AND o.status = 'REQUESTED'", Long.class);
                orderQuery.setParameter("sku", item.getSku());
                Long pendingOrders = orderQuery.getSingleResult();

                if (pendingOrders == 0) {
                    // 3. Invoke SupplierOrderManagerBean.placeRestockOrder
                    try {
                        LOGGER.info("Stock for SKU " + item.getSku() + " is below threshold (" + item.getQuantityAvailable() + " < " + item.getReorderThreshold() + "). Placing restock order...");
                        supplierOrderManager.placeRestockOrder(item.getPrimaryVendor(), item.getSku(), item.getReorderQuantity());
                    } catch (VendorSystemOutageException e) {
                        // 4. Resilience: Catch outage exception to prevent breaking the loop for other items
                        LOGGER.log(Level.SEVERE, "Failed to place restock order for SKU " + item.getSku() + " due to Vendor System Outage: " + e.getMessage());
                    } catch (Exception e) {
                        LOGGER.log(Level.SEVERE, "Unexpected error during replenishment for SKU " + item.getSku() + ": " + e.getMessage(), e);
                    }
                } else {
                    LOGGER.info("Restock order already pending for SKU " + item.getSku() + ". Skipping.");
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to execute inventory replenishment check: " + e.getMessage(), e);
        }
        
        LOGGER.info("Finished inventory replenishment check.");
    }
}
