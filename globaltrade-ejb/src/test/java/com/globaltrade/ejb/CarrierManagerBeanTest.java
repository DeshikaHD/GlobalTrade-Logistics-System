package com.globaltrade.ejb;

import com.globaltrade.core.entity.Customer;
import com.globaltrade.core.entity.Inventory;
import com.globaltrade.core.entity.Order;
import com.globaltrade.core.entity.OrderItem;
import com.globaltrade.core.exception.CarrierTransitException;
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
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ArquillianExtension.class)
public class CarrierManagerBeanTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClasses(CarrierManagerBean.class, CarrierManagerLocal.class, CarrierManagerRemote.class,
                        CarrierManagerTestWrapper.class)
                .addClasses(ExceptionRecoveryServiceBean.class, ExceptionRecoveryServiceLocal.class,
                        ExceptionRecoveryServiceRemote.class)
                .addClasses(Customer.class, Inventory.class, Order.class, OrderItem.class,
                        CarrierTransitException.class, com.globaltrade.core.entity.Vendor.class,
                        com.globaltrade.core.entity.Shipment.class, com.globaltrade.core.enums.ShipmentStatus.class,
                        com.globaltrade.core.entity.SupplierOrder.class)
                .addAsManifestResource(new File("../globaltrade-core/src/main/resources/META-INF/persistence.xml"),
                        "persistence.xml")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @EJB
    private CarrierManagerTestWrapper wrapper;

    @EJB
    private CarrierManagerLocal rawManager;

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    private Long testOrderIdDelivered;
    private Long testOrderIdBreakdown;
    private String tracking1;
    private String tracking2;
    private Long customerId;

    @BeforeEach
    public void setupData() throws Exception {
        jakarta.transaction.UserTransaction utx = (jakarta.transaction.UserTransaction) new javax.naming.InitialContext()
                .lookup("java:comp/UserTransaction");
        utx.begin();
        em.joinTransaction();

        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        Customer customer = new Customer("Test Hospital " + uniqueSuffix, "HOSPITAL", "1234567890");
        em.persist(customer);

        Order order1 = new Order(customer, LocalDateTime.now(), "SHIPPED");
        tracking1 = "TRK-OUT-" + uniqueSuffix + "-1";
        order1.setTrackingNumber(tracking1);
        em.persist(order1);
        testOrderIdDelivered = order1.getId();

        Order order2 = new Order(customer, LocalDateTime.now(), "SHIPPED");
        tracking2 = "TRK-OUT-" + uniqueSuffix + "-2";
        order2.setTrackingNumber(tracking2);
        em.persist(order2);
        testOrderIdBreakdown = order2.getId();
        
        customerId = customer.getId();

        utx.commit();
    }

    @AfterEach
    public void cleanupData() throws Exception {
        jakarta.transaction.UserTransaction utx = (jakarta.transaction.UserTransaction) new javax.naming.InitialContext().lookup("java:comp/UserTransaction");
        utx.begin();
        em.joinTransaction();

        Order o1 = em.find(Order.class, testOrderIdDelivered);
        if (o1 != null) em.remove(o1);

        Order o2 = em.find(Order.class, testOrderIdBreakdown);
        if (o2 != null) em.remove(o2);

        Customer c = em.find(Customer.class, customerId);
        if (c != null) em.remove(c);

        utx.commit();
    }

    @Test
    public void testUpdateTransitStatus_Delivered() {
        wrapper.updateTransitStatus(tracking1, "DELIVERED");
        Order updatedOrder = em.find(Order.class, testOrderIdDelivered);
        assertEquals("DELIVERED", updatedOrder.getStatus(), "Order status should be updated to DELIVERED");
    }

    @Test
    public void testUpdateTransitStatus_Breakdown_RollbackAndRecover() {
        assertThrows(CarrierTransitException.class, () -> {
            wrapper.updateTransitStatus(tracking2, "BREAKDOWN");
        });

        Order recoveredOrder = em.find(Order.class, testOrderIdBreakdown);
        assertEquals("DELAYED_TRANSIT_ISSUE", recoveredOrder.getStatus(),
                "Recovery service should set status to DELAYED_TRANSIT_ISSUE in a separate transaction");
    }

    @Test
    public void testSecurity_DirectAccessBlocked() {
        assertThrows(jakarta.ejb.EJBAccessException.class, () -> {
            rawManager.updateTransitStatus(tracking1, "DELIVERED");
        });
    }
}
