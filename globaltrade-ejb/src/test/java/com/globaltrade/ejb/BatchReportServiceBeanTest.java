package com.globaltrade.ejb;

import com.globaltrade.core.entity.CustomsAuditLog;
import jakarta.ejb.EJB;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.UserTransaction;
import jakarta.annotation.Resource;
import java.io.File;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(ArquillianExtension.class)
public class BatchReportServiceBeanTest {

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class)
                .addClasses(
                    BatchReportServiceBean.class, 
                    BatchReportServiceLocal.class, 
                    BatchReportServiceRemote.class
                )
                .addClasses(CustomsAuditLog.class)
                .addPackage("com.globaltrade.ejb.interceptor").addPackage("com.globaltrade.core.entity").addPackage("com.globaltrade.core.enums").addPackage("com.globaltrade.core.exception").addAsManifestResource(new File("src/test/resources/META-INF/persistence-test.xml"), "persistence.xml")
                .addPackage("com.globaltrade.ejb.interceptor").addPackage("com.globaltrade.core.entity").addPackage("com.globaltrade.core.enums").addPackage("com.globaltrade.core.exception")
                .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @EJB
    private BatchReportServiceLocal batchReportService;

    @PersistenceContext(unitName = "GlobalTradePU")
    private EntityManager em;

    @Resource
    private UserTransaction utx;

    @Test
    public void testBatchReportServiceIsDeployed() {
        assertNotNull(batchReportService, "BatchReportServiceBean should be injected successfully");
    }

    @Test
    public void testGenerateDailyReport() throws Exception {
        // Setup some dummy data
        utx.begin();
        CustomsAuditLog log1 = new CustomsAuditLog("TEST EVENT 1");
        CustomsAuditLog log2 = new CustomsAuditLog("TEST EVENT 2");
        em.persist(log1);
        em.persist(log2);
        utx.commit();

        String report = batchReportService.generateDailyReport();
        assertNotNull(report);
        assertTrue(report.contains("=== Daily Supply Chain Report ==="));
        assertTrue(report.contains("TEST EVENT 1"));
    }
    
    @AfterEach
    public void cleanup() throws Exception {
        utx.begin();
        em.createQuery("DELETE FROM CustomsAuditLog c WHERE c.action LIKE 'TEST EVENT%'").executeUpdate();
        utx.commit();
    }
}

