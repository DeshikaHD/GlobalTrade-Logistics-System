package com.globaltrade.ejb;

import com.globaltrade.core.entity.Inventory;
import com.globaltrade.ejb.interceptor.AuditLoggingInterceptor;
import com.globaltrade.ejb.interceptor.PerformanceMonitoringInterceptor;
import jakarta.annotation.security.DeclareRoles;
import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Local;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.interceptor.Interceptors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

@DeclareRoles({"CUSTOMER", "WAREHOUSE_STAFF"})
@Stateless
@Local(InventoryManagerLocal.class)
@Remote(InventoryManagerRemote.class)
@Interceptors({AuditLoggingInterceptor.class, PerformanceMonitoringInterceptor.class})
@RolesAllowed("CUSTOMER")
public class InventoryManagerBean implements InventoryManagerLocal, InventoryManagerRemote {

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    @Override
    public List<Inventory> getAvailableInventory() {
        List<Inventory> results = em.createQuery("SELECT i FROM Inventory i WHERE i.quantityAvailable > 0", Inventory.class)
                                    .getResultList();
        return new ArrayList<>(results);
    }

    @Override
    @PermitAll
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void updateInventoryQuantity(String sku, int newQuantity) {
        Inventory inv = em.createQuery("SELECT i FROM Inventory i WHERE i.sku = :sku", Inventory.class)
                          .setParameter("sku", sku)
                          .getSingleResult();
        inv.setQuantityAvailable(newQuantity);
        em.merge(inv);
    }

    @DenyAll
    public void purgeAllInventory() {
        throw new IllegalStateException("This operation is permanently disabled for safety.");
    }
}
