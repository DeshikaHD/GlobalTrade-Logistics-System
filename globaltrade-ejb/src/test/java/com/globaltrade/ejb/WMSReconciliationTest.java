package com.globaltrade.ejb;

import com.globaltrade.core.entity.Inventory;
import com.globaltrade.core.entity.SupplierOrder;
import com.globaltrade.core.entity.Vendor;
import com.globaltrade.core.exception.VendorSystemOutageException;
import com.globaltrade.ejb.interceptor.AuditLoggingInterceptor;
import jakarta.ejb.EJB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ArquillianExtension.class)
public class WMSReconciliationTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClasses(InventoryReplenishmentPollerBean.class, InventoryReplenishmentPollerLocal.class, InventoryReplenishmentPollerRemote.class)
                .addClasses(SupplierOrderManagerBean.class, SupplierOrderManagerLocal.class, SupplierOrderManagerRemote.class)
                .addClasses(WarehouseManagementSystemSimulatorBean.class, WMSSimulatorLocal.class, WMSSimulatorRemote.class)
                .addClasses(InventoryManagerBean.class, InventoryManagerLocal.class, InventoryManagerRemote.class)
                .addClasses(SupplierOrder.class, Vendor.class, Inventory.class, VendorSystemOutageException.class, com.globaltrade.core.exception.SupplierNotEligibleException.class, com.globaltrade.core.entity.Shipment.class, com.globaltrade.core.enums.ShipmentStatus.class, AuditLoggingInterceptor.class)
                .addClasses(com.globaltrade.core.entity.Customer.class, com.globaltrade.core.entity.Order.class, com.globaltrade.core.entity.OrderItem.class)
                .addAsManifestResource(new File("../globaltrade-core/src/main/resources/META-INF/persistence.xml"),
                        "persistence.xml")
                .addPackage("com.globaltrade.ejb.interceptor").addPackage("com.globaltrade.core.entity").addPackage("com.globaltrade.core.enums").addPackage("com.globaltrade.core.exception")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @EJB
    private InventoryReplenishmentPollerLocal poller;

    @EJB
    private WMSSimulatorLocal wmsSimulator;

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    private Long vendorId;
    private Long invId;
    private String sku;

    @BeforeEach
    public void setupData() throws Exception {
        jakarta.transaction.UserTransaction utx = (jakarta.transaction.UserTransaction) new javax.naming.InitialContext()
                .lookup("java:comp/UserTransaction");
        utx.begin();
        em.joinTransaction();

        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        Vendor vendor = new Vendor("WMS Vendor " + uniqueSuffix, "wms@vendor.com");
        em.persist(vendor);
        vendorId = vendor.getId();

        sku = "SKU-WMS-" + uniqueSuffix;

        // DB says we have 100 in stock (threshold is 50), so no restock should happen initially.
        Inventory inv = new Inventory("WMS Item " + uniqueSuffix, sku, 100, "LOC-WMS", 50, 100, vendor);
        em.persist(inv);
        invId = inv.getId();

        utx.commit();
    }

    @AfterEach
    public void cleanupData() throws Exception {
        jakarta.transaction.UserTransaction utx = (jakarta.transaction.UserTransaction) new javax.naming.InitialContext().lookup("java:comp/UserTransaction");
        utx.begin();
        em.joinTransaction();

        TypedQuery<SupplierOrder> query = em.createQuery("SELECT o FROM SupplierOrder o WHERE o.sku = :sku", SupplierOrder.class);
        query.setParameter("sku", sku);
        List<SupplierOrder> orders = query.getResultList();
        for (SupplierOrder o : orders) {
            em.remove(o);
        }

        Inventory i = em.find(Inventory.class, invId);
        if (i != null) em.remove(i);

        Vendor v = em.find(Vendor.class, vendorId);
        if (v != null) em.remove(v);

        utx.commit();
    }

    @Test
    public void testReconciliationTriggersRestock() {
        // 1. Initially, DB says quantity is 100. Threshold is 50.
        // We set WMS to say quantity is actually 10!
        wmsSimulator.setMockStockLevel(sku, 10);

        // 2. Invoke Poller. 
        // It should first reconcile DB to 10.
        // Then it should see 10 < 50, and place a restock order.
        poller.checkAndReplenishInventory();

        // 3. Verify DB quantity is updated
        Inventory updatedInv = em.find(Inventory.class, invId);
        assertEquals(10, updatedInv.getQuantityAvailable(), "Inventory should be reconciled to WMS stock level");

        // 4. Verify SupplierOrder was created
        TypedQuery<SupplierOrder> orderQuery = em.createQuery("SELECT o FROM SupplierOrder o WHERE o.sku = :sku", SupplierOrder.class);
        orderQuery.setParameter("sku", sku);
        List<SupplierOrder> orders = orderQuery.getResultList();
        
        assertEquals(1, orders.size(), "One restock order should be placed after reconciliation");
    }
}

