package com.globaltrade.ejb;

import com.globaltrade.core.entity.Customer;
import com.globaltrade.core.entity.Inventory;
import com.globaltrade.core.entity.Order;
import com.globaltrade.core.entity.OrderItem;
import jakarta.ejb.EJB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ArquillianExtension.class)
public class OrderManagerBeanTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClasses(OrderManagerBean.class, OrderManagerLocal.class, OrderManagerRemote.class)
                // Add the entity classes from the core module
                .addClasses(Customer.class, Inventory.class, Order.class, OrderItem.class, com.globaltrade.core.entity.Vendor.class)
                // Add the interceptor if it exists in the EJB module, since OrderManagerBean uses AuditLoggingInterceptor
                // We'll add the package to be safe.
                .addPackage("com.globaltrade.ejb.interceptor")
                // Add persistence.xml from the core module
                .addPackage("com.globaltrade.ejb.interceptor").addPackage("com.globaltrade.core.entity").addPackage("com.globaltrade.core.enums").addPackage("com.globaltrade.core.exception").addAsManifestResource(new File("src/test/resources/META-INF/persistence-test.xml"), "persistence.xml")
                .addPackage("com.globaltrade.ejb.interceptor").addPackage("com.globaltrade.core.entity").addPackage("com.globaltrade.core.enums").addPackage("com.globaltrade.core.exception")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @EJB
    private OrderManagerLocal orderManager;

    @Test
    public void testOrderManagerIsDeployed() {
        assertNotNull(orderManager, "OrderManagerBean should be injected successfully by Arquillian");
    }

    @Test
    public void testPlaceOrder_UnauthorizedUser() {
        // In an in-container Arquillian test without a custom JAAS login context, 
        // the caller principal is typically "anonymous".
        // Therefore, calling placeOrder for Customer ID 1 should trigger the security validation.
        Long requestedCustomerId = 1L;
        List<OrderItem> requestedItems = new ArrayList<>();
        
        jakarta.ejb.EJBAccessException exception = assertThrows(jakarta.ejb.EJBAccessException.class, () -> {
            orderManager.placeOrder(requestedCustomerId, requestedItems);
        });

        assertTrue(exception.getMessage().contains("is not allowed"), "Exception should indicate invocation is not allowed");
    }
}

