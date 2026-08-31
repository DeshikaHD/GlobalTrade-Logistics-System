package com.globaltrade.core.exception;

import jakarta.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class CarrierTransitException extends RuntimeException {
    public CarrierTransitException(String message) {
        super(message);
    }
}
