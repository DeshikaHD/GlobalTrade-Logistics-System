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
public class InventoryReplenishmentPollerBeanTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClasses(InventoryReplenishmentPollerBean.class, InventoryReplenishmentPollerLocal.class, InventoryReplenishmentPollerRemote.class)
                .addClasses(SupplierOrderManagerBean.class, SupplierOrderManagerLocal.class, SupplierOrderManagerRemote.class)
                .addClasses(SupplierOrder.class, Vendor.class, Inventory.class, VendorSystemOutageException.class, com.globaltrade.core.exception.SupplierNotEligibleException.class, com.globaltrade.core.entity.Shipment.class, com.globaltrade.core.enums.ShipmentStatus.class, AuditLoggingInterceptor.class)
                .addClasses(com.globaltrade.core.entity.Customer.class, com.globaltrade.core.entity.Order.class, com.globaltrade.core.entity.OrderItem.class)
                .addClasses(WarehouseManagementSystemSimulatorBean.class, WMSSimulatorLocal.class, WMSSimulatorRemote.class)
                .addClasses(InventoryManagerBean.class, InventoryManagerLocal.class, InventoryManagerRemote.class)
                .addAsManifestResource(new File("../globaltrade-core/src/main/resources/META-INF/persistence.xml"),
                        "persistence.xml")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @EJB
    private InventoryReplenishmentPollerLocal poller;

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    private Long validVendorId;
    private Long outageVendorId;
    private Long normalInvId;
    private Long outageInvId;
    private String normalSku;
    private String outageSku;

    @BeforeEach
    public void setupData() throws Exception {
        jakarta.transaction.UserTransaction utx = (jakarta.transaction.UserTransaction) new javax.naming.InitialContext()
                .lookup("java:comp/UserTransaction");
        utx.begin();
        em.joinTransaction();

        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        
        Vendor validVendor = new Vendor("Valid Vendor " + uniqueSuffix, "contact@valid.com");
        Vendor outageVendor = new Vendor("Outage Vendor " + uniqueSuffix, "contact@outage.com"); // Contains "Outage"
        
        em.persist(validVendor);
        em.persist(outageVendor);
        validVendorId = validVendor.getId();
        outageVendorId = outageVendor.getId();

        normalSku = "SKU-NORM-" + uniqueSuffix;
        outageSku = "SKU-OUTAGE-" + uniqueSuffix;

        // Inventory below threshold
        Inventory normalInv = new Inventory("Normal Item " + uniqueSuffix, normalSku, 10, "LOC-1", 50, 100, validVendor);
        Inventory outageInv = new Inventory("Outage Item " + uniqueSuffix, outageSku, 5, "LOC-2", 20, 100, outageVendor);

        em.persist(normalInv);
        em.persist(outageInv);
        normalInvId = normalInv.getId();
        outageInvId = outageInv.getId();

        utx.commit();
    }

    @AfterEach
    public void cleanupData() throws Exception {
        jakarta.transaction.UserTransaction utx = (jakarta.transaction.UserTransaction) new javax.naming.InitialContext().lookup("java:comp/UserTransaction");
        utx.begin();
        em.joinTransaction();

        // Delete any generated supplier orders
        TypedQuery<SupplierOrder> query = em.createQuery("SELECT o FROM SupplierOrder o WHERE o.sku IN (:s1, :s2)", SupplierOrder.class);
        query.setParameter("s1", normalSku);
        query.setParameter("s2", outageSku);
        List<SupplierOrder> orders = query.getResultList();
        for (SupplierOrder o : orders) {
            em.remove(o);
        }

        Inventory i1 = em.find(Inventory.class, normalInvId);
        if (i1 != null) em.remove(i1);

        Inventory i2 = em.find(Inventory.class, outageInvId);
        if (i2 != null) em.remove(i2);

        Vendor v1 = em.find(Vendor.class, validVendorId);
        if (v1 != null) em.remove(v1);

        Vendor v2 = em.find(Vendor.class, outageVendorId);
        if (v2 != null) em.remove(v2);

        utx.commit();
    }

    @Test
    public void testPollerExecutesReplenishmentAndResistsOutage() {
        // Manually invoke the timer method
        poller.checkAndReplenishInventory();

        // 1. Validate that exactly one SupplierOrder was created for the normal item
        TypedQuery<SupplierOrder> normalQuery = em.createQuery("SELECT o FROM SupplierOrder o WHERE o.sku = :sku", SupplierOrder.class);
        normalQuery.setParameter("sku", normalSku);
        List<SupplierOrder> normalOrders = normalQuery.getResultList();
        
        assertEquals(1, normalOrders.size(), "Exactly one order should be placed for the normal inventory item");
        assertEquals(100, normalOrders.get(0).getQuantity(), "Order quantity should match reorderQuantity");

        // 2. Validate that ZERO SupplierOrders were created for the outage item (rolled back)
        TypedQuery<SupplierOrder> outageQuery = em.createQuery("SELECT o FROM SupplierOrder o WHERE o.sku = :sku", SupplierOrder.class);
        outageQuery.setParameter("sku", outageSku);
        List<SupplierOrder> outageOrders = outageQuery.getResultList();
        
        assertTrue(outageOrders.isEmpty(), "No order should be placed for the item with an outage vendor due to transaction rollback");
    }
}
