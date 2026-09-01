package com.globaltrade.ejb;

import jakarta.ejb.Remote;

@Remote
public interface BatchReportServiceRemote {
    String generateDailyReport();
    int generateBatchAuditReport();
}
