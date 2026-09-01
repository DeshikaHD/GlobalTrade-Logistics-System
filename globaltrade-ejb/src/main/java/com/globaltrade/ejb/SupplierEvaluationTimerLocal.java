package com.globaltrade.ejb;

import jakarta.ejb.Local;

@Local
public interface SupplierEvaluationTimerLocal {
    void evaluateSuppliers();
}
