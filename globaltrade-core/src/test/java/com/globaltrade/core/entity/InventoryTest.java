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
        
        ConstraintViolation<Inventory> violation = violations.iterator().next();
        assertEquals("quantityAvailable", violation.getPropertyPath().toString());
    }

    @Test
    public void testBlankProductNameValidation() {
        Inventory inventory = new Inventory("", "SYR-300", 1000, "Warehouse C");
        Set<ConstraintViolation<Inventory>> violations = validator.validate(inventory);
        
        assertEquals(1, violations.size(), "Should have exactly one violation for blank product name");
        
        ConstraintViolation<Inventory> violation = violations.iterator().next();
        assertEquals("productName", violation.getPropertyPath().toString());
    }
}
