package com.globaltrade.ejb;

import com.globaltrade.core.entity.*;
import jakarta.ejb.EJB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.UserTransaction;
import jakarta.annotation.Resource;
import java.io.File;
import java.util.List;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ArquillianExtension.class)
public class DashboardManagerBeanTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClasses(DashboardManagerBean.class, DashboardManagerLocal.class, DashboardManagerRemote.class)
                .addClasses(Customer.class, Inventory.class, Order.class, OrderItem.class, SupplierOrder.class, Vendor.class, Shipment.class, com.globaltrade.core.enums.ShipmentStatus.class)
                .addAsManifestResource(new File("src/test/resources/META-INF/persistence-test.xml"), "persistence.xml")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @EJB
    private DashboardManagerLocal dashboardManager;

    @PersistenceContext
    private EntityManager em;

    @Resource
    private UserTransaction utx;

    @AfterEach
    public void cleanup() throws Exception {
        utx.begin();
        em.createQuery("DELETE FROM SupplierOrder").executeUpdate();
        em.createQuery("DELETE FROM OrderItem").executeUpdate();
        em.createQuery("DELETE FROM Order").executeUpdate();
        em.createQuery("DELETE FROM Inventory").executeUpdate();
        em.createQuery("DELETE FROM Vendor").executeUpdate();
        em.createQuery("DELETE FROM Customer").executeUpdate();
        utx.commit();
    }

    @Test
    public void testDashboardManagerIsDeployed() {
        assertNotNull(dashboardManager, "DashboardManagerBean should be injected successfully by Arquillian");
    }

    @Test
    public void testSecurity_DirectAccessBlocked() {
        jakarta.ejb.EJBAccessException exception = assertThrows(jakarta.ejb.EJBAccessException.class, () -> {
            dashboardManager.getAllOutboundOrders();
        });
        assertTrue(exception.getMessage().contains("is not allowed"), "Exception should indicate invocation is not allowed for anonymous users");
    }
}
