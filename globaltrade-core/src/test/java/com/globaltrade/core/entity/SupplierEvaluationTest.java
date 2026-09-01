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

public class SupplierEvaluationTest {

    private static Validator validator;

    @BeforeAll
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidSupplierEvaluation() {
        Vendor vendor = new Vendor("Acme", "contact");
        SupplierEvaluation eval = new SupplierEvaluation(vendor, 85, "Good performance");
        Set<ConstraintViolation<SupplierEvaluation>> violations = validator.validate(eval);
        assertTrue(violations.isEmpty(), "Valid evaluation should have no violations");
    }

    @Test
    public void testSupplierEvaluationNullVendor() {
        SupplierEvaluation eval = new SupplierEvaluation(null, 85, "Good performance");
        Set<ConstraintViolation<SupplierEvaluation>> violations = validator.validate(eval);
        assertEquals(1, violations.size(), "Null vendor should trigger a violation");
    }

    @Test
    public void testSupplierEvaluationNullScore() {
        Vendor vendor = new Vendor("Acme", "contact");
        SupplierEvaluation eval = new SupplierEvaluation(vendor, null, "Good performance");
        Set<ConstraintViolation<SupplierEvaluation>> violations = validator.validate(eval);
        assertEquals(1, violations.size(), "Null score should trigger a violation");
    }
}
