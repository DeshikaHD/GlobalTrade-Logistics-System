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
