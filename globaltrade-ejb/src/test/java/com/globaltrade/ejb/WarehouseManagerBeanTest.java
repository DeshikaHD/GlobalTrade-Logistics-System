package com.globaltrade.ejb;

import com.globaltrade.core.entity.Customer;
import com.globaltrade.core.entity.Inventory;
import com.globaltrade.core.entity.Order;
import com.globaltrade.core.entity.OrderItem;
import com.globaltrade.core.exception.InsufficientStockException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ArquillianExtension.class)
public class WarehouseManagerBeanTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClasses(WarehouseManagerBean.class, WarehouseManagerLocal.class, WarehouseManagerRemote.class, WarehouseManagerTestWrapper.class)
                .addClasses(Customer.class, Inventory.class, Order.class, OrderItem.class, InsufficientStockException.class)
                .addAsManifestResource(new File("../globaltrade-core/src/main/resources/META-INF/persistence.xml"), "persistence.xml")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @EJB
    private WarehouseManagerTestWrapper warehouseWrapper;

    @EJB
    private WarehouseManagerLocal rawWarehouseManager;

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    private Long testOrderId;
    private Long testInventoryId;
    private Long insufficientStockOrderId;

    @BeforeEach
    public void setupData() throws Exception {
        jakarta.transaction.UserTransaction utx = (jakarta.transaction.UserTransaction) new javax.naming.InitialContext().lookup("java:comp/UserTransaction");
        utx.begin();
        em.joinTransaction();

        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        Customer customer = new Customer("Test Hospital " + uniqueSuffix, "HOSPITAL", "1234567890");
        em.persist(customer);

        Inventory inv1 = new Inventory("Masks " + uniqueSuffix, "SKU1" + uniqueSuffix, 100, "LOC1");
        Inventory inv2 = new Inventory("Gloves " + uniqueSuffix, "SKU2" + uniqueSuffix, 5, "LOC2");
        em.persist(inv1);
        em.persist(inv2);
        testInventoryId = inv1.getId();

        Order order = new Order(customer, LocalDateTime.now(), "PENDING");
        OrderItem item1 = new OrderItem(order, inv1, 10);
        order.addOrderItem(item1);
        em.persist(order);
        testOrderId = order.getId();

        Order orderFail = new Order(customer, LocalDateTime.now(), "PENDING");
        OrderItem itemFail = new OrderItem(orderFail, inv2, 10);
        orderFail.addOrderItem(itemFail);
        em.persist(orderFail);
        insufficientStockOrderId = orderFail.getId();

        utx.commit();
    }

    @Test
    public void testGetPendingOrders_WithWrapper() {
        List<Order> pendingOrders = warehouseWrapper.getPendingOrders();
        assertFalse(pendingOrders.isEmpty(), "Should retrieve pending orders");
        
        boolean foundTestOrder = false;
        for (Order o : pendingOrders) {
            if (o.getId().equals(testOrderId)) {
                foundTestOrder = true;
                // Verify collections were eagerly loaded and wrapper stripped
                assertNotNull(o.getCustomer(), "Customer should be loaded");
                assertFalse(o.getOrderItems().isEmpty(), "Order items should be loaded");
                assertEquals(ArrayList.class, o.getOrderItems().getClass(), "PersistentBag should be stripped to ArrayList");
            }
        }
        assertTrue(foundTestOrder, "The test order should be in the pending list");
    }

    @Test
    public void testPackOrder_Success() {
        warehouseWrapper.packOrder(testOrderId);
        
        Order updatedOrder = em.find(Order.class, testOrderId);
        assertEquals("PACKED", updatedOrder.getStatus(), "Order status should be updated to PACKED");
        
        Inventory updatedInv = em.find(Inventory.class, testInventoryId);
        assertEquals(90, updatedInv.getQuantityAvailable(), "Inventory quantity should be deducted (100 - 10)");
    }

    @Test
    public void testPackOrder_InsufficientStock() {
        assertThrows(InsufficientStockException.class, () -> {
            warehouseWrapper.packOrder(insufficientStockOrderId);
        });
    }

    @Test
    public void testSecurity_DirectAccessBlocked() {
        assertThrows(jakarta.ejb.EJBAccessException.class, () -> {
            rawWarehouseManager.getPendingOrders();
        });
    }
}
