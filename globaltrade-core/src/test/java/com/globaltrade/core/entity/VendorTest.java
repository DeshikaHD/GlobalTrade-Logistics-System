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

public class VendorTest {

    private static Validator validator;

    @BeforeAll
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidVendor() {
        Vendor vendor = new Vendor("Acme Corp", "contact@acme.com");
        Set<ConstraintViolation<Vendor>> violations = validator.validate(vendor);
        assertTrue(violations.isEmpty(), "Valid vendor should have no violations");
    }

    @Test
    public void testVendorBlankName() {
        Vendor vendor = new Vendor("", "contact@acme.com");
        Set<ConstraintViolation<Vendor>> violations = validator.validate(vendor);
        assertEquals(1, violations.size(), "Blank name should trigger a violation");
    }

    @Test
    public void testVendorBlankContactInfo() {
        Vendor vendor = new Vendor("Acme Corp", "");
        Set<ConstraintViolation<Vendor>> violations = validator.validate(vendor);
        assertEquals(1, violations.size(), "Blank contact info should trigger a violation");
    }
}
