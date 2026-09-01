package com.globaltrade.ejb;

import com.globaltrade.core.entity.CustomsAuditLog;
import jakarta.annotation.Resource;
import jakarta.annotation.security.PermitAll;
import jakarta.ejb.Local;
import jakarta.ejb.Remote;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionManagement;
import jakarta.ejb.TransactionManagementType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.UserTransaction;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
@Local(BatchReportServiceLocal.class)
@Remote(BatchReportServiceRemote.class)
@TransactionManagement(TransactionManagementType.BEAN)
@PermitAll
public class BatchReportServiceBean implements BatchReportServiceLocal, BatchReportServiceRemote {

    private static final Logger LOGGER = Logger.getLogger(BatchReportServiceBean.class.getName());

    @Resource
    private UserTransaction utx;

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager entityManager;

    @Override
    public String generateDailyReport() {
        try {
            utx.begin();

            List<CustomsAuditLog> logs = entityManager.createQuery(
                "SELECT c FROM CustomsAuditLog c WHERE c.logDate >= :since ORDER BY c.logDate DESC", CustomsAuditLog.class)
                .setParameter("since", LocalDateTime.now().minusDays(1))
                .getResultList();

            StringBuilder report = new StringBuilder("=== Daily Supply Chain Report ===\n");
            report.append("Generated: ").append(LocalDateTime.now()).append("\n");
            report.append("Customs audit entries (last 24h): ").append(logs.size()).append("\n");

            for (CustomsAuditLog log : logs) {
                report.append("  - ").append(log.getAction()).append(" at ").append(log.getLogDate()).append("\n");
            }

            utx.commit();
            LOGGER.info("BMT: Daily report generated successfully. Entries: " + logs.size());
            return report.toString();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "BMT: Daily report generation failed, rolling back.", e);
            try {
                utx.rollback();
            } catch (Exception rbEx) {
                LOGGER.severe("BMT: Rollback also failed: " + rbEx.getMessage());
            }
            throw new RuntimeException("Report generation failed: " + e.getMessage(), e);
        }
    }

    @Override
    public int generateBatchAuditReport() {
        int totalProcessed = 0;
        try {
            List<CustomsAuditLog> allLogs = fetchAllLogsInSeparateTransaction();

            int batchSize = 10;
            for (int i = 0; i < allLogs.size(); i += batchSize) {
                utx.begin();
                int end = Math.min(i + batchSize, allLogs.size());
                List<CustomsAuditLog> batch = allLogs.subList(i, end);
                for (CustomsAuditLog log : batch) {
                    LOGGER.fine("BMT Batch: Processing audit log ID=" + log.getId() + " action=" + log.getAction());
                    totalProcessed++;
                }
                utx.commit();
                LOGGER.info("BMT Batch: Committed batch. Total processed so far: " + totalProcessed);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "BMT Batch: Failed at record " + totalProcessed + ", rolling back current batch.", e);
            try {
                utx.rollback();
            } catch (Exception rbEx) {
                LOGGER.severe("BMT Batch: Rollback failed: " + rbEx.getMessage());
            }
        }
        LOGGER.info("BMT Batch: Audit report complete. Total records processed: " + totalProcessed);
        return totalProcessed;
    }

    private List<CustomsAuditLog> fetchAllLogsInSeparateTransaction() {
        try {
            utx.begin();
            List<CustomsAuditLog> logs = entityManager.createQuery(
                "SELECT c FROM CustomsAuditLog c ORDER BY c.logDate DESC", CustomsAuditLog.class)
                .getResultList();
            utx.commit();
            return logs;
        } catch (Exception e) {
            try {
                utx.rollback();
            } catch (Exception rbEx) {
                LOGGER.severe("BMT: Fetch rollback failed: " + rbEx.getMessage());
            }
            throw new RuntimeException("Failed to fetch audit logs", e);
        }
    }
}
