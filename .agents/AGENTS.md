# Agent Rules

* **No Comments:** Absolutely no code comments or docstrings allowed; enforce self-documenting code.
* **File Granularity:** Keep classes under 150 lines.
* **Explicit Interfaces:** Always define both `@Local` and `@Remote` interfaces for EJBs; avoid no-interface views.
* **Test-As-We-Go:** Agents must write and run tests at the end of each implementation phase.
* **No CLI Maven:** Agents must not run `mvn` in the terminal (rely on user's IDE).
