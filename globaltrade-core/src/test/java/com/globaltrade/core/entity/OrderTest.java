package com.globaltrade.core.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class OrderTest {

    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidOrder() {
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setName("Test Hospital");

        Order order = new Order(customer, LocalDateTime.now(), "PENDING");
        order.setTrackingNumber("TRK-OUT-001");

        Set<ConstraintViolation<Order>> violations = validator.validate(order);
        assertTrue(violations.isEmpty(), "Valid order should have no violations");
    }

    @Test
    public void testInvalidOrder_NullCustomer() {
        Order order = new Order(null, LocalDateTime.now(), "PENDING");

        Set<ConstraintViolation<Order>> violations = validator.validate(order);
        assertFalse(violations.isEmpty(), "Order with null customer should have violations");
    }

    @Test
    public void testInvalidOrder_BlankStatus() {
        Customer customer = new Customer();
        customer.setId(1L);

        Order order = new Order(customer, LocalDateTime.now(), "");

        Set<ConstraintViolation<Order>> violations = validator.validate(order);
        assertFalse(violations.isEmpty(), "Order with blank status should have violations");
    }
}
