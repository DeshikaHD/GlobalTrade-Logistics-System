# Task Tracker

## Phase 1 — Foundation Fixes
- `[x]` 1.1 Fix `persistence.xml` — add missing entity classes
- `[x]` 1.2 Create `SupplierIntegrationFacadeLocal` interface
- `[x]` 1.3 Add missing `@Local`/`@Remote` annotations on beans
- `[x]` 1.4 Add `CUSTOMS_OFFICIAL` user to `WildFly_Users.md`

## Phase 2 — New Interceptors
- `[/]` 2.1 Create `PerformanceMonitoringInterceptor`
- `[ ]` 2.2 Create `VendorDataValidationInterceptor`
- `[ ]` 2.3 Apply interceptor chaining (multiple interceptors per bean)
- `[ ]` 2.4 Add `@PostConstruct` to `AuditLoggingInterceptor`
- `[ ]` 2.5 Update Arquillian `@Deployment` ShrinkWrap

## Phase 3 — Programmatic Timer
- `[ ]` 3.1 Create `RouteOptimizationTimerBean` with programmatic timer
- `[ ]` 3.2 Create Local and Remote interfaces

## Phase 4 — Transaction Management (BMT + Timeout)
- `[ ]` 4.1 Create `BatchReportServiceBean` with BMT
- `[ ]` 4.2 Create Local and Remote interfaces
- `[ ]` 4.3 Add `@TransactionTimeout` on critical methods

## Phase 5 — Security Enhancements
- `[ ]` 5.1 Add `@DeclareRoles` to all secured EJBs
- `[ ]` 5.2 Add `isCallerInRole()` programmatic security
- `[ ]` 5.3 Demonstrate `@DenyAll` on sensitive method

## Phase 6 — EJB Lifecycle Callbacks
- `[ ]` 6.1 Add `@PostConstruct` / `@PreDestroy` to Stateless EJBs

## Phase 7 — Deployment Descriptors
- `[ ]` 7.1 Create `ejb-jar.xml`
- `[ ]` 7.2 Create `jboss-ejb3.xml`
- `[ ]` 7.3 Create `beans.xml`

## Phase 8 — Missing Unit Tests
- `[ ]` 8.1 Create unit tests for 5 untested entities
- `[ ]` 8.2 Update `Test_Report.md`

## Phase 9 — Integration Test Enhancements
- `[ ]` 9.1 Create `BatchReportServiceBeanTest`
- `[ ]` 9.2 Update all Arquillian `@Deployment` methods
- `[ ]` 9.3 Update `Test_Report.md`

## Phase 10 — Documentation
- `[ ]` 10.1 Technical Implementation Documentation
- `[ ]` 10.2 Expand `Test_Report.md` Critical Analysis
- `[ ]` 10.3 Add Harvard references
