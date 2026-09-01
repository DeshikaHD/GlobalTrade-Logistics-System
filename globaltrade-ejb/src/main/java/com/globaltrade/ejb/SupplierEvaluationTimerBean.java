package com.globaltrade.ejb;

import com.globaltrade.core.entity.SupplierEvaluation;
import com.globaltrade.core.entity.SupplierOrder;
import com.globaltrade.core.entity.Vendor;
import jakarta.ejb.Schedule;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.logging.Logger;

@Singleton
@Startup
public class SupplierEvaluationTimerBean implements SupplierEvaluationTimerLocal, SupplierEvaluationTimerRemote {

    private static final Logger LOGGER = Logger.getLogger(SupplierEvaluationTimerBean.class.getName());

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;

    @Schedule(hour = "*", minute = "*/30", persistent = true)
    @Override
    public void evaluateSuppliers() {
        LOGGER.info("Starting Supplier Evaluation Timer...");
        
        List<Vendor> vendors = entityManager.createQuery("SELECT v FROM Vendor v", Vendor.class).getResultList();
        for (Vendor vendor : vendors) {
            evaluateVendor(vendor);
        }
    }

    private void evaluateVendor(Vendor vendor) {
        List<SupplierOrder> orders = entityManager.createQuery(
            "SELECT o FROM SupplierOrder o WHERE o.vendor.id = :vendorId AND o.status = 'FULFILLED'", SupplierOrder.class)
            .setParameter("vendorId", vendor.getId())
            .getResultList();

        if (orders.isEmpty()) {
            return;
        }

        int totalScore = 100;
        StringBuilder remarks = new StringBuilder();

        for (SupplierOrder order : orders) {
            if (order.getExpectedDeliveryDate() != null && order.getReceivedDate() != null) {
                if (order.getReceivedDate().toLocalDate().isAfter(order.getExpectedDeliveryDate())) {
                    totalScore -= 10;
                    remarks.append("Late delivery for order ").append(order.getOrderId()).append(". ");
                }
            }

            if (order.getQuantityAccepted() != null && order.getQuantityAccepted() < order.getQuantity()) {
                totalScore -= 15;
                remarks.append("Defective items in order ").append(order.getOrderId()).append(". ");
            }

            if (order.getTradeDocumentationProvided() == null || !order.getTradeDocumentationProvided()) {
                totalScore -= 20;
                remarks.append("Missing customs documentation for order ").append(order.getOrderId()).append(". ");
            }
        }

        if (totalScore < 0) totalScore = 0;

        vendor.setRating(totalScore);
        
        if (totalScore < 60) {
            vendor.setIsEligible(false);
            remarks.append("Vendor suspended due to low score.");
        } else {
            vendor.setIsEligible(true);
        }

        SupplierEvaluation eval = new SupplierEvaluation(vendor, totalScore, remarks.toString());
        entityManager.persist(eval);
        entityManager.merge(vendor);

        LOGGER.info("Evaluated vendor " + vendor.getName() + " with score: " + totalScore);
    }
}
