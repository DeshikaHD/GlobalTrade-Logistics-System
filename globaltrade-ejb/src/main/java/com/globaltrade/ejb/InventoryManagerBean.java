package com.globaltrade.ejb;

import com.globaltrade.core.entity.Inventory;
import com.globaltrade.ejb.interceptor.AuditLoggingInterceptor;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Local;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.interceptor.Interceptors;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

@Stateless
@Local(InventoryManagerLocal.class)
@Remote(InventoryManagerRemote.class)
@Interceptors(AuditLoggingInterceptor.class)
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
}
