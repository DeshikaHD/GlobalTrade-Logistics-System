package com.globaltrade.ejb;

import com.globaltrade.core.entity.SupplierOrder;
import com.globaltrade.core.entity.Vendor;
import com.globaltrade.core.exception.VendorSystemOutageException;
import com.globaltrade.ejb.interceptor.AuditLoggingInterceptor;
import jakarta.ejb.EJB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ArquillianExtension.class)
public class SupplierOrderManagerBeanTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClasses(SupplierOrderManagerBean.class, SupplierOrderManagerLocal.class, SupplierOrderManagerRemote.class)
                .addClasses(SupplierOrder.class, Vendor.class, VendorSystemOutageException.class, com.globaltrade.core.exception.SupplierNotEligibleException.class, com.globaltrade.core.entity.Shipment.class, com.globaltrade.core.enums.ShipmentStatus.class, AuditLoggingInterceptor.class)
                .addClasses(com.globaltrade.core.entity.Customer.class, com.globaltrade.core.entity.Inventory.class, com.globaltrade.core.entity.Order.class, com.globaltrade.core.entity.OrderItem.class)
                .addAsManifestResource(new File("../globaltrade-core/src/main/resources/META-INF/persistence.xml"),
                        "persistence.xml")
                .addPackage("com.globaltrade.ejb.interceptor").addPackage("com.globaltrade.core.entity").addPackage("com.globaltrade.core.enums").addPackage("com.globaltrade.core.exception")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @EJB
    private SupplierOrderManagerLocal supplierOrderManager;

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    private Long validVendorId;
    private Long outageVendorId;
    private Long createdOrderId;

    @BeforeEach
    public void setupData() throws Exception {
        jakarta.transaction.UserTransaction utx = (jakarta.transaction.UserTransaction) new javax.naming.InitialContext()
                .lookup("java:comp/UserTransaction");
        utx.begin();
        em.joinTransaction();

        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        
        Vendor validVendor = new Vendor("Valid Vendor " + uniqueSuffix, "contact@valid.com");
        Vendor outageVendor = new Vendor("Outage Vendor " + uniqueSuffix, "contact@outage.com");
        
        em.persist(validVendor);
        em.persist(outageVendor);
        
        validVendorId = validVendor.getId();
        outageVendorId = outageVendor.getId();

        utx.commit();
    }

    @AfterEach
    public void cleanupData() throws Exception {
        jakarta.transaction.UserTransaction utx = (jakarta.transaction.UserTransaction) new javax.naming.InitialContext().lookup("java:comp/UserTransaction");
        utx.begin();
        em.joinTransaction();

        if (createdOrderId != null) {
            SupplierOrder order = em.find(SupplierOrder.class, createdOrderId);
            if (order != null) em.remove(order);
        }

        Vendor v1 = em.find(Vendor.class, validVendorId);
        if (v1 != null) em.remove(v1);

        Vendor v2 = em.find(Vendor.class, outageVendorId);
        if (v2 != null) em.remove(v2);

        utx.commit();
    }

    @Test
    public void testPlaceRestockOrder_Success() {
        Vendor vendor = em.find(Vendor.class, validVendorId);
        assertNotNull(vendor);

        SupplierOrder order = supplierOrderManager.placeRestockOrder(vendor, "SKU-TEST-123", 100);
        assertNotNull(order);
        assertNotNull(order.getOrderId());
        assertEquals("REQUESTED", order.getStatus());
        
        createdOrderId = order.getOrderId();
    }

    @Test
    public void testPlaceRestockOrder_VendorOutage() {
        Vendor vendor = em.find(Vendor.class, outageVendorId);
        assertNotNull(vendor);

        assertThrows(VendorSystemOutageException.class, () -> {
            supplierOrderManager.placeRestockOrder(vendor, "SKU-TEST-123", 100);
        });
    }
}

