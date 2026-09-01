package com.globaltrade.ejb;

import jakarta.ejb.Local;

@Local
public interface BatchReportServiceLocal {
    String generateDailyReport();
    int generateBatchAuditReport();
}
