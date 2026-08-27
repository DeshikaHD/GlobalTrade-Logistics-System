package com.globaltrade.ejb.interceptor;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import java.util.logging.Logger;

public class AuditLoggingInterceptor {
    private static final Logger logger = Logger.getLogger(AuditLoggingInterceptor.class.getName());

    @AroundInvoke
    public Object logAudit(InvocationContext ctx) throws Exception {
        String methodName = ctx.getMethod().getName();
        logger.info("AUDIT: Executing " + methodName);
        try {
            Object result = ctx.proceed();
            logger.info("AUDIT: Successfully executed " + methodName);
            return result;
        } catch (Exception e) {
            logger.severe("AUDIT: Execution failed for " + methodName + " - " + e.getMessage());
            throw e;
        }
    }
}
