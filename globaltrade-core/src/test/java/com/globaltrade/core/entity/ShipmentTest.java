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

public class ShipmentTest {

    private static Validator validator;

    @BeforeAll
    public static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    public void testValidShipment() {
        Shipment shipment = new Shipment("TRK12345", ShipmentStatus.READY_FOR_EXPORT);
        Set<ConstraintViolation<Shipment>> violations = validator.validate(shipment);
        assertTrue(violations.isEmpty(), "Valid shipment should have no violations");
    }

    @Test
    public void testShipmentBlankTrackingNumber() {
        Shipment shipment = new Shipment("", ShipmentStatus.READY_FOR_EXPORT);
        Set<ConstraintViolation<Shipment>> violations = validator.validate(shipment);
        assertEquals(1, violations.size(), "Blank tracking number should trigger a violation");
    }

    @Test
    public void testShipmentNullStatus() {
        Shipment shipment = new Shipment("TRK12345", null);
        Set<ConstraintViolation<Shipment>> violations = validator.validate(shipment);
        assertEquals(1, violations.size(), "Null status should trigger a violation");
    }
}
