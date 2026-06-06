# TestSeer IntelliJ plugin

> **Status:** Partial — local PSI test plans only  
> **Last verified:** 2026-06-05  
> **Does not call:** `testseer-backend` REST API

Provides **`PsiSdkCodeIntelligence`** — real IntelliJ PSI (`PsiClass`, `PsiMethod`, `JavaRecursiveElementVisitor`, `ReadAction`) — and **`GenerateTestPlanAction`** (editor / Project View context menu).

## Prerequisites

- JDK 17+
- **`io.testseer:testseer-v01:0.1.0-SNAPSHOT`** in Maven Local — the v0.1 engine is **not built from this repo** (no root `pom.xml` / `src/`). Install from a separate v0.1 engine checkout: `mvn install -DskipTests`.

## Build

```bash
cd intellij-plugin
./gradlew compileJava    # compile only
./gradlew buildPlugin    # packaged plugin zip → build/distributions/
```

## Run sandbox IDE

```bash
./gradlew runIde
```

## Current scope vs planned

| Feature | Status |
|---------|--------|
| PSI-based parse + test plan JSON | Shipped (dialog excerpt) |
| Backend impact analysis integration | Planned — [P15](../testseer-backend/docs/archive/plans/2026-06-05-p15-intellij-impact-consumer.md) |
| Tool window (Plan / Skeleton / Manual tabs) | Not built — see [Completion Plan](../docs/TestSeer_Completion_Execution_Plan.md) Step 9 |
| MCP / Cursor workflow | Use [testseer-mcp](../testseer-mcp/README.md) instead |

## Versions

- **IntelliJ Platform Gradle Plugin 2.16.0** with **IDEA Community 2024.2.5** (Gradle **9.0**+, `sinceBuild` **242**). `settings.gradle.kts` applies the **Foojay** toolchain resolver so Gradle can provision the JDK version required for the target platform. Java support via bundled plugin `com.intellij.java`.

## Entry points

- **`io.testseer.intellij.psi.PsiSdkCodeIntelligence`** — implements `io.testseer.engine.CodeIntelligence`.
- **`io.testseer.intellij.IntellijTestSeerFacade`** — calls **`TestSeerSurfaces.generateTestPlan`** with PSI-backed model.
- **`GenerateTestPlanAction`** — uses `IntellijTestSeerFacade.generatePlan`.
