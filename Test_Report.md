# Critical Analysis and Test Report

## 1. Introduction
This document maintains the test cases and execution logs for the various modules developed as part of the GlobalTrade Logistics System. The testing strategy utilizes **JUnit 5** for Unit Testing and **Arquillian** for Integration Testing, conforming to the assignment requirements.

## 2. Unit Testing (Core Module)

Unit tests focus on validating business logic and entity constraints without requiring a running application server.

### 2.1 Inventory Bean Validation Tests
Below is the unit test code for validating the constraints (e.g., `@NotBlank`, `@Min`) of the `Inventory` entity using JUnit 5 and Hibernate Validator.

```java
package com.globaltrade.core.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InventoryTest {

    private static Validator validator;

    @BeforeAll
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidInventory() {
        Inventory inventory = new Inventory("Surgical Masks", "MASK-100", 5000, "Warehouse A");
        Set<ConstraintViolation<Inventory>> violations = validator.validate(inventory);
        
        assertTrue(violations.isEmpty(), "Valid inventory should not produce constraint violations");
    }

    @Test
    public void testNegativeQuantityValidation() {
        Inventory inventory = new Inventory("IV Fluids", "IV-200", -10, "Warehouse B");
        Set<ConstraintViolation<Inventory>> violations = validator.validate(inventory);
        
        assertEquals(1, violations.size(), "Should have exactly one violation for negative quantity");
    }

    @Test
    public void testBlankProductNameValidation() {
        Inventory inventory = new Inventory("", "SYR-300", 1000, "Warehouse C");
        Set<ConstraintViolation<Inventory>> violations = validator.validate(inventory);
        
        assertEquals(1, violations.size(), "Should have exactly one violation for blank product name");
    }
}
```

### 2.2 Unit Test Results

| Test Class | Test Method | Purpose | Status |
| :--- | :--- | :--- | :--- |
| `InventoryTest` | `testValidInventory` | Verifies that a properly populated entity passes validation | PASSED |
| `InventoryTest` | `testNegativeQuantityValidation` | Verifies that negative quantity triggers a `@Min` constraint violation | PASSED |
| `InventoryTest` | `testBlankProductNameValidation` | Verifies that a missing product name triggers a `@NotBlank` violation | PASSED |
| `CustomerTest` | `testValidCustomer` | Verifies that a correctly populated Customer passes validation | PASSED |
| `CustomerTest` | `testCustomerNameTooShort` | Verifies that a name with length < 2 triggers a violation | PASSED |
| `CustomerTest` | `testCustomerNameBlank` | Verifies that a blank name triggers a `@NotBlank` violation | PASSED |

---

## 3. Integration Testing (EJB Module)

Integration tests validate the interaction between components, lifecycle management, and security configurations inside an actual container (WildFly).

### 3.1 OrderManagerBean Arquillian Tests
Below is the Integration Test code developed for the `OrderManagerBean.java` class using **JUnit 5 + Arquillian**.

```java
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
                .addClasses(Customer.class, Inventory.class, Order.class, OrderItem.class)
                .addPackage("com.globaltrade.ejb.interceptor")
                .addAsManifestResource(new File("../globaltrade-core/src/main/resources/META-INF/persistence.xml"), "persistence.xml")
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
        Long requestedCustomerId = 1L;
        List<OrderItem> requestedItems = new ArrayList<>();
        
        jakarta.ejb.EJBAccessException exception = assertThrows(jakarta.ejb.EJBAccessException.class, () -> {
            orderManager.placeOrder(requestedCustomerId, requestedItems);
        });

        assertTrue(exception.getMessage().contains("is not allowed"), "Exception should indicate invocation is not allowed");
    }
}
```

### 3.2 Integration Test Results

| Test Class | Test Method | Purpose | Status |
| :--- | :--- | :--- | :--- |
| `OrderManagerBeanTest` | `testOrderManagerIsDeployed` | Validates Arquillian deployment and EJB injection | PASSED |
| `OrderManagerBeanTest` | `testPlaceOrder_UnauthorizedUser` | Validates `@RolesAllowed` security interceptor blocks unauthenticated access | PASSED |
| `WarehouseManagerBeanTest` | `testGetPendingOrders_WithWrapper` | Validates retrieving pending orders, eager fetching, and stripping `PersistentBag` | PASSED |
| `WarehouseManagerBeanTest` | `testPackOrder_Success` | Validates physical stock deduction and status update | PASSED |
| `WarehouseManagerBeanTest` | `testPackOrder_InsufficientStock` | Validates `InsufficientStockException` is thrown when stock is low | PASSED |
| `WarehouseManagerBeanTest` | `testSecurity_DirectAccessBlocked` | Validates `@RolesAllowed` blocks anonymous callers | PASSED |
| `CarrierDispatchPollerBeanTest` | `testPollDeliveryStatus_Lifecycle` | Validates timer logic shifting orders from PACKED to SHIPPED to DELIVERED | PASSED |
| `CarrierManagerBeanTest` | `testUpdateTransitStatus_Delivered` | Validates that DELIVERED event sets status to DELIVERED | PASSED |
| `CarrierManagerBeanTest` | `testUpdateTransitStatus_Breakdown_RollbackAndRecover` | Validates rollback and REQUIRES_NEW recovery logic for BREAKDOWN | PASSED |
| `CarrierManagerBeanTest` | `testSecurity_DirectAccessBlocked` | Validates `@RolesAllowed("CARRIER")` blocks anonymous access | PASSED |
| `SupplierOrderManagerBeanTest` | `testPlaceRestockOrder_Success` | Validates that a valid restock order is persisted correctly | PASSED |
| `SupplierOrderManagerBeanTest` | `testPlaceRestockOrder_VendorOutage` | Validates that `VendorSystemOutageException` is thrown during a simulated outage | PASSED |
| `InventoryReplenishmentPollerBeanTest` | `testPollerExecutesReplenishmentAndResistsOutage` | Validates timer creates SupplierOrders for low stock and resists vendor outages without crashing | PASSED |
| `WMSReconciliationTest` | `testReconciliationTriggersRestock` | Validates that discrepancies between WMS and DB are reconciled before the timer places restock orders | PASSED |

**Critical Analysis:**
By utilizing **Arquillian** for Integration Testing, we validate the EJB lifecycle and security context within an actual Application Server (WildFly) instead of relying on mocked behavior. 
The security test proves that the container's declarative authorization effectively identifies and rejects unauthorized caller principals, satisfying the strict supply chain security requirements defined in the architecture.

*Note on Database Integrity:* All Arquillian tests (`WarehouseManagerBeanTest`, `CarrierManagerBeanTest`) have been updated with `@AfterEach` cleanup methods to explicitly remove any dummy entities (`Customer`, `Order`, `Inventory`) persisted during test initialization, ensuring the live PostgreSQL database is not polluted across test runs.
