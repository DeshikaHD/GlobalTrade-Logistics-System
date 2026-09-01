# Agent Rules

* **No Comments:** Absolutely no code comments or docstrings allowed; enforce self-documenting code.
* **File Granularity:** Keep classes under 150 lines.
* **Explicit Interfaces:** Always define both `@Local` and `@Remote` interfaces for EJBs; avoid no-interface views.
* **Test-As-We-Go:** Agents must write and run tests at the end of each implementation phase.
* **No CLI Maven:** Agents must not run `mvn` in the terminal (rely on user's IDE).
* **Testing Framework Constraint:** Do NOT use Mockito for testing. Use pure JUnit 5 for Unit Tests and Arquillian for Integration Tests.
* **Arquillian Test Data Cleanup:** Always include an `@AfterEach` method in Arquillian Integration Tests to delete any dummy entities persisted during the test setup, preventing live database pollution.
* **Test Reporting:** Every time a test is created or modified, the `Test_Report.md` file MUST be updated. The report MUST maintain two separate tables: one for "Unit Tests" and one for "Integration Tests", and all tests must be logged into their respective tables.
* **Database Context:** The project uses PostgreSQL. When writing SQL queries, database instructions, or troubleshooting persistence issues, always assume PostgreSQL syntax and behaviors.
* **Arquillian ShrinkWrap Completeness:** When creating or modifying Entity classes, EJBs, or Interceptors, you MUST proactively search for and update the `@Deployment` (`ShrinkWrap.create()`) methods in ALL existing Arquillian Integration tests to include the new classes via `.addClasses()`. Missing transitive JPA dependencies (like related Entities) will cause `DeploymentException` and `ClassNotFoundException`.
* **Arquillian Remote Workflow (Chicken & Egg):** The project uses `wildfly-arquillian-container-remote`. Tests require an active WildFly server, but starting the server requires building the EAR, which fails if tests aren't passing. **Agents must explicitly advise the user to turn OFF Maven tests (skipTests) during standard builds to break this cycle.** Tests should only be run manually via the IDE *after* the server has successfully started with the deployed EAR.
