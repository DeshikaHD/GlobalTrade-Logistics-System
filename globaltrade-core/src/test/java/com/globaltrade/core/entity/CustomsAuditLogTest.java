package com.globaltrade.core.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CustomsAuditLogTest {

    private static Validator validator;

    @BeforeAll
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidCustomsAuditLog() {
        CustomsAuditLog log = new CustomsAuditLog("Approved shipment");
        Set<ConstraintViolation<CustomsAuditLog>> violations = validator.validate(log);
        assertTrue(violations.isEmpty(), "Valid log should have no violations");
    }

    @Test
    public void testCustomsAuditLogBlankAction() {
        CustomsAuditLog log = new CustomsAuditLog("");
        Set<ConstraintViolation<CustomsAuditLog>> violations = validator.validate(log);
        assertEquals(1, violations.size(), "Blank action should trigger a violation");
    }

    @Test
    public void testCustomsAuditLogNullDate() {
        CustomsAuditLog log = new CustomsAuditLog("Approved shipment");
        log.setLogDate(null);
        Set<ConstraintViolation<CustomsAuditLog>> violations = validator.validate(log);
        assertEquals(1, violations.size(), "Null log date should trigger a violation");
    }
}
