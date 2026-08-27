package com.globaltrade.core.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

public class CustomerTest {

    private static Validator validator;

    @BeforeAll
    public static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    public void testValidCustomer() {
        Customer customer = new Customer("General Hospital", "HOSPITAL", "contact@genhosp.com");
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        assertTrue(violations.isEmpty(), "Valid customer should have no violations");
    }

    @Test
    public void testCustomerNameTooShort() {
        Customer customer = new Customer("A", "HOSPITAL", "contact@genhosp.com");
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        assertFalse(violations.isEmpty(), "Customer name must be at least 2 characters");
    }

    @Test
    public void testCustomerNameBlank() {
        Customer customer = new Customer("", "HOSPITAL", "contact@genhosp.com");
        Set<ConstraintViolation<Customer>> violations = validator.validate(customer);
        assertFalse(violations.isEmpty(), "Customer name cannot be blank");
    }
}
