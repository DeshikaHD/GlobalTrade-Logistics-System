package com.globaltrade.ejb;

import com.globaltrade.core.entity.SupplierOrder;
import com.globaltrade.core.entity.Vendor;
import com.globaltrade.core.exception.VendorSystemOutageException;
import com.globaltrade.ejb.interceptor.AuditLoggingInterceptor;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.interceptor.Interceptors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.logging.Logger;

@Stateless
public class SupplierOrderManagerBean implements SupplierOrderManagerLocal, SupplierOrderManagerRemote {

    private static final Logger LOGGER = Logger.getLogger(SupplierOrderManagerBean.class.getName());

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    @Interceptors(AuditLoggingInterceptor.class)
    public SupplierOrder placeRestockOrder(Vendor vendor, String sku, int quantity) {
        if (vendor == null || sku == null || quantity <= 0) {
            throw new IllegalArgumentException("Invalid restock order parameters.");
        }
        
        Vendor managedVendor = entityManager.find(Vendor.class, vendor.getId());
        if (managedVendor == null) {
            throw new IllegalArgumentException("Vendor not found with ID: " + vendor.getId());
        }

        if (managedVendor.getIsEligible() == null || !managedVendor.getIsEligible()) {
            throw new com.globaltrade.core.exception.SupplierNotEligibleException("Vendor is not eligible to receive orders.");
        }

        simulateVendorApiCall(managedVendor);

        com.globaltrade.core.entity.Inventory inventory = entityManager.createQuery("SELECT i FROM Inventory i WHERE i.sku = :sku", com.globaltrade.core.entity.Inventory.class)
            .setParameter("sku", sku)
            .getResultStream().findFirst().orElse(null);
        String productName = inventory != null ? inventory.getProductName() : "Unknown Product";

        SupplierOrder order = new SupplierOrder(managedVendor, sku, quantity, "REQUESTED");
        order.setProductName(productName);
        entityManager.persist(order);
        
        LOGGER.info("Successfully placed restock order for SKU: " + sku + " with Vendor: " + managedVendor.getName());
        return order;
    }

    private void simulateVendorApiCall(Vendor vendor) {
        if (vendor.getName() != null && vendor.getName().contains("Outage")) {
            throw new VendorSystemOutageException("Connection refused: Vendor API is currently down.");
        }
    }
}
