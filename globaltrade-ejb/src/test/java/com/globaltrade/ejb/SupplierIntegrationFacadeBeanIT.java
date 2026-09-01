package com.globaltrade.ejb;

import com.globaltrade.core.entity.Shipment;
import com.globaltrade.core.entity.SupplierEvaluation;
import com.globaltrade.core.entity.SupplierOrder;
import com.globaltrade.core.entity.Vendor;
import com.globaltrade.core.exception.InvalidOrderStateException;
import com.globaltrade.core.exception.SupplierNotEligibleException;
import com.globaltrade.core.exception.VendorSystemOutageException;
import com.globaltrade.ejb.interceptor.AuditLoggingInterceptor;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.ejb.EJB;
import jakarta.ejb.EJBAccessException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.UserTransaction;
import javax.naming.InitialContext;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ArquillianExtension.class)
public class SupplierIntegrationFacadeBeanIT {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClasses(
                        SupplierIntegrationFacadeBean.class,
                        SupplierIntegrationFacadeRemote.class,
                        SupplierIntegrationFacadeTestWrapper.class,
                        Vendor.class,
                        SupplierOrder.class,
                        SupplierEvaluation.class,
                        Shipment.class,
                        com.globaltrade.core.enums.ShipmentStatus.class,
                        InvalidOrderStateException.class,
                        SupplierNotEligibleException.class,
                        VendorSystemOutageException.class,
                        AuditLoggingInterceptor.class
                )
                .addAsManifestResource("META-INF/persistence.xml", "persistence.xml")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @EJB
    private SupplierIntegrationFacadeTestWrapper wrapper;

    @EJB
    private SupplierIntegrationFacadeRemote secureFacade;

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    private UserTransaction utx;
    private Vendor testVendor;
    private SupplierOrder testOrder;

    @BeforeEach
    public void setup() throws Exception {
        utx = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
        utx.begin();
        em.joinTransaction();

        testVendor = new Vendor("Test Vendor " + UUID.randomUUID(), "Contact");
        em.persist(testVendor);

        testOrder = new SupplierOrder(testVendor, "SKU-" + UUID.randomUUID(), 100, "REQUESTED");
        em.persist(testOrder);

        utx.commit();
    }

    @AfterEach
    public void teardown() throws Exception {
        utx.begin();
        em.joinTransaction();
        if (!em.contains(testOrder)) testOrder = em.merge(testOrder);
        em.remove(testOrder);
        if (testOrder.getShipment() != null) {
            Shipment s = em.merge(testOrder.getShipment());
            em.remove(s);
        }
        if (!em.contains(testVendor)) testVendor = em.merge(testVendor);
        em.remove(testVendor);
        utx.commit();
    }

    @Test
    public void testDirectAccessFailsWithoutRole() {
        assertThrows(EJBAccessException.class, () -> {
            secureFacade.ping();
        }, "Should throw EJBAccessException when accessed directly without VENDOR role");
    }

    @Test
    public void testFulfillOrderSuccess() {
        SupplierOrder fulfilled = wrapper.fulfillOrder(testOrder.getOrderId(), "TRK-12345", true);
        
        assertNotNull(fulfilled);
        assertEquals("FULFILLED", fulfilled.getStatus());
        assertTrue(fulfilled.getTradeDocumentationProvided());
        assertNotNull(fulfilled.getShipment());
        assertEquals("TRK-12345", fulfilled.getShipment().getTrackingNumber());
        assertEquals("READY_FOR_EXPORT", fulfilled.getShipment().getStatus());
    }

    @Test
    public void testFulfillOrderInvalidState() {
        wrapper.fulfillOrder(testOrder.getOrderId(), "TRK-12345", true);
        
        assertThrows(InvalidOrderStateException.class, () -> {
            wrapper.fulfillOrder(testOrder.getOrderId(), "TRK-67890", true);
        }, "Should not fulfill an already fulfilled order");
    }
}
