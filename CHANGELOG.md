# Changelog

## 2026-06-05

- **Documentation status pass:** added [`docs/CURRENT_STATUS.md`](docs/CURRENT_STATUS.md); synced Phase 1 architecture endpoint tables with OpenAPI; marked MCP as shipped surface; documented `/v1/gaps` blocked and `crossServiceBoundary` internal-only; updated README for repo layout without v0.1 `src/`.
- **Documentation layout:** moved `TestSeer_*.md` architecture and planning docs from repo root into [`docs/`](docs/); updated cross-links in README, module READMEs, and archive index.
- **Backend docs colocation:** moved Phase 1 architecture, graph guide, and P1–P15 plans into [`testseer-backend/docs/`](testseer-backend/docs/README.md); platform index remains at [`docs/README.md`](docs/README.md).
- **Documentation reorganization:** added [`docs/README.md`](docs/README.md) index; expanded root [`README.md`](README.md) doc map; moved implementation plans to [`docs/archive/plans/`](docs/archive/plans/README.md).
- **Spike cleanup:** removed Phase 0 graph spike source and `graph-spike` Maven profile; PRD Appendix A updated.
- **Dead code:** removed orphaned legacy `Model.java` (v0.1 engine, external checkout).
- **Governance:** `contract-governance-check.sh` now includes `testseer-backend/docs/openapi.yaml`.

## 2026-05-01

- **Contract governance:** added `docs/schema-contract.md` with schema versioning, deprecation policy, compatibility rules, and required change process.
- **Migration tracking:** added `docs/schema-migration-notes.md` with baseline (`v1.0.0`) and non-breaking contract evolution notes (`v1.0.1`).
- **Domain contract refactor (non-breaking):** split `model/Model.java` into `PlanModel`, `CodeIntelModel`, and `RenderModel` packages without changing JSON wire schema.

## 2026-05-02

- **CI calibration artifact:** added `ConfidenceCalibrationReport`, `ConfidenceCalibrationArtifact`, and tests that emit `target/calibration/confidence-calibration-report.json` on successful builds for classifier/threshold/stub baseline metrics.
- **API rule pack governance:** `RulePackSchemaValidator` for YAML shape, `conflictResolution` (`collect_all` / `first_match`) with detection haystack matching, and default pack `detect` values as valid Java regex. Documented in `docs/rules-schema.md`.
- **IntelliJ build:** `intellij-plugin` migrated to **IntelliJ Platform Gradle Plugin 2.16.0** (IC **2024.2.5**), **Gradle 9.0** (required by 2.16+), `org.gradle.toolchains.foojay-resolver-convention` **1.0.0** for JDK toolchain provisioning, `bundledPlugin("com.intellij.java")`, `sinceBuild` **242**+.
- **Contract governance:** `scripts/contract-governance-check.sh` — fail if snapshot or schema contract files change without a `CHANGELOG.md` update in the same diff (optional env `BASE_REF` for the merge base).
- **Rule pack golden regression:** `RulePackGoldenRegressionTest` and `src/test/resources/rules/fixtures/golden/*` lock parsed-model → prerequisites + `StubSpec` outputs (`-Dtestseer.golden.update=true` to refresh).

## 2026-05-03

- **IntelliJ plugin module (`intellij-plugin/`):** Gradle + IntelliJ Plugin 1.x, `PsiSdkCodeIntelligence` using real PSI APIs, context-menu action, `IntellijTestSeerFacade` wired to `TestSeerSurfaces.generateTestPlan`. Core: `HeuristicLayerClassifier` uses project root from surfaces; public `generateTestPlan` API for IDE.
