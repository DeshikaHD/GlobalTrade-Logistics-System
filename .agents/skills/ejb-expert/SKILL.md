---
name: ejb-expert
description: Jakarta EE and Enterprise JavaBeans context and best practices.
---
# EJB Expert Skill

When working with Jakarta EE and EJB:
- Use standard Jakarta EE annotations.
- Provide appropriate EJB lifecycle and transaction management.

### Testing with Arquillian and EJB Security
- **Remote Containers**: When using `wildfly-arquillian-container-remote`, remember that the target server (e.g., WildFly) MUST be running before test execution. If building the project (e.g., `mvn install`) without the server running, ensure tests are explicitly skipped to avoid connection refusal errors.
- **Declarative Security**: When testing EJB methods protected by `@RolesAllowed`, unauthorized access (e.g., from an unauthenticated Arquillian test client) will be intercepted by the container *before* method execution. Tests should expect a `jakarta.ejb.EJBAccessException` rather than custom exceptions thrown within the method body.

### WildFly JNDI Lazy Authentication
- **Lazy Evaluation**: Standard WildFly JNDI `InitialContext` performs lazy authentication. Incorrect credentials provided in the `Properties` map will NOT throw a `NamingException` during `ctx.lookup()`.
- **Symptom**: Authentication failures only surface during the *first actual remote EJB method invocation*, typically manifesting as a `RequestSendFailedException` caused by `SaslException` or generic `EJBCLIENT000409: No more destinations are available` errors.
- **Solution**: To validate credentials upfront (e.g., in standalone CLI clients), ALWAYS force an immediate, lightweight invocation on a secured EJB method immediately after the JNDI lookup. Surround it with a `try-catch` to handle the exception and prompt the user gracefully before proceeding to the application loop.

### Arquillian In-Container Testing & Lazy Loading
- **Transaction Context**: When running Arquillian tests, test methods (`@Test`) often execute outside an active JTA transaction unless explicitly configured.
- **Symptom**: Calling `em.find()` in the assert phase and subsequently traversing a `FetchType.LAZY` collection will throw a `LazyInitializationException` inside the server. This often propagates back to the IDE test runner as an un-deserializable `ClassNotFoundException: org.hibernate.LazyInitializationException`.
- **Solution**: Do not traverse lazy collections on entities loaded in the test method. Instead:
  1. Persist the related entities during the `@BeforeEach` setup and store their Primary Keys in class fields (e.g., `testInventoryId`).
  2. Use `em.find(Entity.class, savedId)` directly on the related entity in the assert phase to verify updates.
