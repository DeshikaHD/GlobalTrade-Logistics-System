package com.globaltrade.core.entity;

import com.globaltrade.core.enums.ShipmentStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CustomsDeclarationTest {

    private static Validator validator;

    @BeforeAll
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidCustomsDeclaration() {
        Shipment shipment = new Shipment("TRK123", ShipmentStatus.READY_FOR_EXPORT);
        CustomsDeclaration declaration = new CustomsDeclaration("HS123", 500.0, "BrokerBob", shipment);
        Set<ConstraintViolation<CustomsDeclaration>> violations = validator.validate(declaration);
        assertTrue(violations.isEmpty(), "Valid customs declaration should have no violations");
    }

    @Test
    public void testCustomsDeclarationBlankHsCode() {
        Shipment shipment = new Shipment("TRK123", ShipmentStatus.READY_FOR_EXPORT);
        CustomsDeclaration declaration = new CustomsDeclaration("", 500.0, "BrokerBob", shipment);
        Set<ConstraintViolation<CustomsDeclaration>> violations = validator.validate(declaration);
        assertEquals(1, violations.size(), "Blank HS Code should trigger a violation");
    }

    @Test
    public void testCustomsDeclarationNullTaxPaid() {
        Shipment shipment = new Shipment("TRK123", ShipmentStatus.READY_FOR_EXPORT);
        CustomsDeclaration declaration = new CustomsDeclaration("HS123", null, "BrokerBob", shipment);
        Set<ConstraintViolation<CustomsDeclaration>> violations = validator.validate(declaration);
        assertEquals(1, violations.size(), "Null tax paid should trigger a violation");
    }

    @Test
    public void testCustomsDeclarationBlankBrokerName() {
        Shipment shipment = new Shipment("TRK123", ShipmentStatus.READY_FOR_EXPORT);
        CustomsDeclaration declaration = new CustomsDeclaration("HS123", 500.0, "", shipment);
        Set<ConstraintViolation<CustomsDeclaration>> violations = validator.validate(declaration);
        assertEquals(1, violations.size(), "Blank broker name should trigger a violation");
    }
}
