package com.globaltrade.ejb;

import com.globaltrade.core.entity.Customer;
import com.globaltrade.core.entity.Inventory;
import com.globaltrade.core.entity.Order;
import com.globaltrade.core.entity.OrderItem;
import jakarta.ejb.EJB;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ArquillianExtension.class)
public class CarrierDispatchPollerBeanTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClasses(CarrierDispatchPollerBean.class, CarrierDispatchPollerLocal.class, CarrierDispatchPollerRemote.class)
                .addClasses(CarrierTrackingSimulatorBean.class, CarrierTrackingSimulatorLocal.class, CarrierTrackingSimulatorRemote.class)
                .addClasses(Customer.class, Inventory.class, Order.class, OrderItem.class)
                .addAsManifestResource(new File("../globaltrade-core/src/main/resources/META-INF/persistence.xml"), "persistence.xml")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @EJB
    private CarrierDispatchPollerLocal poller;

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    private Long packedOrderId;

    @BeforeEach
    public void setupData() throws Exception {
        jakarta.transaction.UserTransaction utx = (jakarta.transaction.UserTransaction) new javax.naming.InitialContext().lookup("java:comp/UserTransaction");
        utx.begin();
        em.joinTransaction();

        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        Customer customer = new Customer("Test Hospital " + uniqueSuffix, "HOSPITAL", "1234567890");
        em.persist(customer);

        Inventory inv = new Inventory("Bandages " + uniqueSuffix, "SKU" + uniqueSuffix, 100, "LOC1");
        em.persist(inv);

        Order order = new Order(customer, LocalDateTime.now(), "PACKED");
        OrderItem item = new OrderItem(order, inv, 10);
        order.addOrderItem(item);
        em.persist(order);
        packedOrderId = order.getId();

        utx.commit();
    }

    @Test
    public void testPollDeliveryStatus_Lifecycle() {
        // First poll: Should pick up the PACKED order and mark it as SHIPPED
        poller.pollDeliveryStatus();
        Order shippedOrder = em.find(Order.class, packedOrderId);
        assertEquals("SHIPPED", shippedOrder.getStatus(), "Order status should be updated to SHIPPED");

        // Subsequent polls: Should eventually mark the SHIPPED order as DELIVERED
        boolean delivered = false;
        for (int i = 0; i < 20; i++) {
            poller.pollDeliveryStatus();
            Order checkOrder = em.find(Order.class, packedOrderId);
            if ("DELIVERED".equals(checkOrder.getStatus())) {
                delivered = true;
                break;
            }
        }
        
        assertTrue(delivered, "Order should eventually be marked as DELIVERED by the simulator");
    }
}
