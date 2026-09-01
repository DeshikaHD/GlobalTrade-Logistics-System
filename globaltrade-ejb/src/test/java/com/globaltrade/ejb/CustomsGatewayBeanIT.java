package com.globaltrade.ejb;

import com.globaltrade.core.entity.CustomsAuditLog;
import com.globaltrade.core.entity.CustomsDeclaration;
import com.globaltrade.core.entity.Shipment;
import com.globaltrade.core.entity.SupplierOrder;
import com.globaltrade.core.entity.Vendor;
import com.globaltrade.core.enums.ShipmentStatus;
import com.globaltrade.core.exception.CustomsClearanceRejectedException;
import com.globaltrade.ejb.interceptor.CustomsComplianceInterceptor;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.ejb.EJB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.UserTransaction;
import javax.naming.InitialContext;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ArquillianExtension.class)
public class CustomsGatewayBeanIT {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
            .addClasses(CustomsGatewayBean.class, CustomsGatewayLocal.class, CustomsGatewayRemote.class,
                        CustomsGatewayTestWrapper.class, CustomsComplianceInterceptor.class,
                        CustomsClearanceRejectedException.class, 
                        Shipment.class, ShipmentStatus.class, CustomsDeclaration.class, CustomsAuditLog.class,
                        SupplierOrder.class, Vendor.class)
            .addAsResource("META-INF/persistence.xml");
    }

    @EJB
    private CustomsGatewayTestWrapper gatewayWrapper;

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;
    
    private UserTransaction utx;

    private Shipment testShipment;

    @BeforeEach
    public void setup() throws Exception {
        utx = (UserTransaction) new InitialContext().lookup("java:comp/UserTransaction");
        utx.begin();
        em.joinTransaction();

        testShipment = new Shipment("TRK-" + UUID.randomUUID().toString(), ShipmentStatus.READY_FOR_EXPORT);
        em.persist(testShipment);

        utx.commit();
    }

    @AfterEach
    public void cleanup() throws Exception {
        utx.begin();
        em.joinTransaction();
        
        // Clean up entities in correct order to avoid constraint violations
        em.createQuery("DELETE FROM CustomsAuditLog").executeUpdate();
        em.createQuery("DELETE FROM CustomsDeclaration").executeUpdate();
        em.createQuery("DELETE FROM Shipment s WHERE s.id = :id")
            .setParameter("id", testShipment.getId())
            .executeUpdate();
            
        utx.commit();
    }

    @Test
    public void testSubmitDeclarationSuccess() {
        CustomsDeclaration declaration = gatewayWrapper.submitDeclaration(testShipment.getId(), "HS-123", 1000.0, "TestBroker");
        assertNotNull(declaration);
        assertEquals("HS-123", declaration.getHsCode());
        
        // Use a new transaction to verify DB state
        Shipment s = getShipmentFromDb(testShipment.getId());
        assertEquals(ShipmentStatus.AT_BORDER_PENDING_CLEARANCE, s.getStatus());
    }

    @Test
    public void testSubmitDeclarationRejection_MissingTaxes() {
        assertThrows(CustomsClearanceRejectedException.class, () -> {
            gatewayWrapper.submitDeclaration(testShipment.getId(), "HS-123", 0.0, "TestBroker");
        });
        
        Shipment s = getShipmentFromDb(testShipment.getId());
        // Verify transaction rolled back, state didn't change
        assertEquals(ShipmentStatus.READY_FOR_EXPORT, s.getStatus());
    }

    @Test
    public void testApproveShipment() throws Exception {
        // Setup initial state for AT_BORDER_PENDING_CLEARANCE
        utx.begin();
        em.joinTransaction();
        Shipment s = em.find(Shipment.class, testShipment.getId());
        s.setStatus(ShipmentStatus.AT_BORDER_PENDING_CLEARANCE);
        em.merge(s);
        utx.commit();

        gatewayWrapper.approveShipment(testShipment.getId());

        Shipment updated = getShipmentFromDb(testShipment.getId());
        assertEquals(ShipmentStatus.CLEARED, updated.getStatus());
    }

    @Test
    public void testRejectShipment() throws Exception {
        // Setup initial state for AT_BORDER_PENDING_CLEARANCE
        utx.begin();
        em.joinTransaction();
        Shipment s = em.find(Shipment.class, testShipment.getId());
        s.setStatus(ShipmentStatus.AT_BORDER_PENDING_CLEARANCE);
        em.merge(s);
        utx.commit();

        gatewayWrapper.rejectShipment(testShipment.getId());

        Shipment updated = getShipmentFromDb(testShipment.getId());
        assertEquals(ShipmentStatus.CUSTOMS_PAPERWORK_REJECTED, updated.getStatus());
    }

    @Test
    public void testGetPendingClearanceShipments() throws Exception {
        // Setup a pending shipment
        utx.begin();
        em.joinTransaction();
        Shipment s = em.find(Shipment.class, testShipment.getId());
        s.setStatus(ShipmentStatus.AT_BORDER_PENDING_CLEARANCE);
        em.merge(s);
        utx.commit();

        List<Shipment> pending = gatewayWrapper.getPendingClearanceShipments();
        assertFalse(pending.isEmpty());
        assertTrue(pending.stream().anyMatch(shipment -> shipment.getId().equals(testShipment.getId())));
    }
    
    private Shipment getShipmentFromDb(Long id) {
        try {
            utx.begin();
            em.joinTransaction();
            Shipment s = em.find(Shipment.class, id);
            utx.commit();
            return s;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
