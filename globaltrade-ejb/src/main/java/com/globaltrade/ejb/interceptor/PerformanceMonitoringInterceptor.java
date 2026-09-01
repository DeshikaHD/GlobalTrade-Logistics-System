package com.globaltrade.ejb.interceptor;

import jakarta.annotation.PostConstruct;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PerformanceMonitoringInterceptor {

    private static final Logger LOGGER = Logger.getLogger(PerformanceMonitoringInterceptor.class.getName());
    private static final long SLOW_THRESHOLD_MS = 500;

    @PostConstruct
    public void init(InvocationContext ctx) throws Exception {
        LOGGER.info("PerformanceMonitoringInterceptor initialized for: " + ctx.getTarget().getClass().getSimpleName());
        ctx.proceed();
    }

    @AroundInvoke
    public Object measurePerformance(InvocationContext ctx) throws Exception {
        long startNano = System.nanoTime();
        try {
            return ctx.proceed();
        } finally {
            long durationMs = (System.nanoTime() - startNano) / 1_000_000;
            String methodName = ctx.getMethod().getName();
            String targetClass = ctx.getTarget().getClass().getSimpleName();
            if (durationMs > SLOW_THRESHOLD_MS) {
                LOGGER.log(Level.WARNING, "SLOW METHOD: {0}.{1} took {2}ms (threshold: {3}ms)",
                    new Object[]{targetClass, methodName, durationMs, SLOW_THRESHOLD_MS});
            } else {
                LOGGER.log(Level.INFO, "PERF: {0}.{1} completed in {2}ms",
                    new Object[]{targetClass, methodName, durationMs});
            }
        }
    }
}
