package com.globaltrade.ejb.interceptor;

import com.globaltrade.core.entity.Vendor;
import com.globaltrade.core.exception.SupplierNotEligibleException;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.logging.Logger;

public class VendorDataValidationInterceptor {

    private static final Logger LOGGER = Logger.getLogger(VendorDataValidationInterceptor.class.getName());

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;

    @AroundInvoke
    public Object validateVendorData(InvocationContext ctx) throws Exception {
        String methodName = ctx.getMethod().getName();

        if (methodName.contains("Vendor") || methodName.contains("Order") || methodName.contains("fulfill")) {
            Object[] params = ctx.getParameters();
            for (Object param : params) {
                if (param instanceof Long potentialVendorId) {
                    Vendor vendor = entityManager.find(Vendor.class, potentialVendorId);
                    if (vendor != null && !vendor.getIsEligible()) {
                        LOGGER.warning("VENDOR VALIDATION FAILED: Vendor " + vendor.getName() + " is suspended (rating=" + vendor.getRating() + ").");
                        throw new SupplierNotEligibleException("Vendor " + vendor.getName() + " is currently suspended due to low performance score.");
                    }
                }
            }
        }

        LOGGER.info("VendorDataValidationInterceptor passed for: " + methodName);
        return ctx.proceed();
    }
}
