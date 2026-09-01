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

public class SupplierOrderTest {

    private static Validator validator;

    @BeforeAll
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidSupplierOrder() {
        Vendor vendor = new Vendor("Test Vendor", "Contact");
        SupplierOrder order = new SupplierOrder(vendor, "SKU-123", 100, "REQUESTED");
        
        Set<ConstraintViolation<SupplierOrder>> violations = validator.validate(order);
        assertTrue(violations.isEmpty(), "A correctly populated SupplierOrder should not produce violations.");
    }

    @Test
    public void testQuantityBelowMinimum() {
        Vendor vendor = new Vendor("Test Vendor", "Contact");
        // Quantity is 0, but minimum allowed is 1
        SupplierOrder order = new SupplierOrder(vendor, "SKU-123", 0, "REQUESTED");
        
        Set<ConstraintViolation<SupplierOrder>> violations = validator.validate(order);
        assertEquals(1, violations.size(), "Should produce exactly one violation for quantity < 1");
    }

    @Test
    public void testBlankSku() {
        Vendor vendor = new Vendor("Test Vendor", "Contact");
        // Blank SKU
        SupplierOrder order = new SupplierOrder(vendor, "", 100, "REQUESTED");
        
        Set<ConstraintViolation<SupplierOrder>> violations = validator.validate(order);
        assertEquals(1, violations.size(), "Should produce exactly one violation for blank SKU");
    }
}
