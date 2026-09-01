package com.globaltrade.ejb.interceptor;

import com.globaltrade.core.entity.CustomsAuditLog;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.InvocationContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.logging.Logger;

public class CustomsComplianceInterceptor {

    private static final Logger LOGGER = Logger.getLogger(CustomsComplianceInterceptor.class.getName());

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;

    @AroundInvoke
    public Object auditGovernmentInteraction(InvocationContext ctx) throws Exception {
        String methodName = ctx.getMethod().getName();
        Object[] parameters = ctx.getParameters();
        
        StringBuilder actionBuilder = new StringBuilder();
        actionBuilder.append("Executing Customs Interaction: ").append(methodName);
        
        if (parameters != null && parameters.length > 0) {
            actionBuilder.append(" | Target Params: ");
            for (Object param : parameters) {
                if (param != null) {
                    actionBuilder.append(param.toString()).append(" ");
                }
            }
        }
        
        LOGGER.info("CustomsComplianceInterceptor intercepting: " + actionBuilder.toString());
        
        CustomsAuditLog auditLog = new CustomsAuditLog(actionBuilder.toString());
        entityManager.persist(auditLog);

        return ctx.proceed();
    }
}
