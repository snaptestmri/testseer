# TestSeer Full Functionality Build Plan

> **Status:** Historical — v0.1 engine roadmap; source not in this repo  
> **Last verified:** 2026-06-05

## Goal
Build TestSeer from the current v0.1 scaffold into a production-ready test case builder with:
- real code intelligence extraction,
- reliable API-layer inference,
- robust scenario and prerequisite generation,
- high-quality test skeleton/manual output,
- complete IntelliJ + CLI + Maven experiences,
- calibration-backed trust metrics.

## Current Baseline (Done)
- End-to-end pipeline exists: classify -> prereqs -> scenarios -> render.
- Rule-pack and stub inference are scaffolded and functional.
- Mock provider SPI is present with default + template provider.
- Renderers output TestNG, manual Markdown, and JSON.
- Compatibility test with Spring Petclinic passes.

## Step-by-Step Implementation Plan

### 1) Stabilize Domain Contracts (Week 1)
**Objective:** Freeze core data contracts so downstream modules remain stable.

**Tasks**
- Finalize semantics for `TestPlan`, `Scenario`, `Prerequisite`, `StubSpec`.
- Split large model container into focused classes/packages.
- Add explicit schema/version in plan outputs.
- Add JSON snapshot tests for backward compatibility.

**Acceptance criteria**
- Output schema is versioned and documented.
- No breaking contract changes without test updates.

---

### 2) Implement Real Code Intelligence (Weeks 1-3)
**Objective:** Replace scaffolding with true source analysis.

**Tasks**
- Implement full PSI traversal for IntelliJ path.
- Implement JavaParser + SymbolSolver path for CLI/Maven.
- Extract:
  - annotations and mappings,
  - constructor/field dependencies,
  - outbound calls and signatures,
  - sibling test patterns,
  - build/test config hints.
- Add parity tests between PSI and JavaParser normalized output.

**Acceptance criteria**
- Same target class yields equivalent normalized model in both adapters.
- Parsing works on real-world Spring projects.

---

### 3) Production Layer Classifier (Week 3)
**Objective:** Improve classification reliability and explainability.

**Tasks**
- Extend classifier signals:
  - annotation/type/package/source-set/naming/sibling-test hints.
- Implement score composition and confidence thresholds.
- Add rationale trace (which rules fired).
- Add low-confidence handling contract for surfaces.

**Acceptance criteria**
- Classifier emits layer + confidence + explanation trace.
- Low-confidence behavior is deterministic and test-covered.

---

### 4) Rule Engine + YAML Governance (Weeks 3-4)
**Objective:** Make rule packs robust, extensible, and safe to evolve.

**Tasks**
- Add YAML schema validation.
- Add priority and conflict resolution.
- Add hot-reload for rule packs.
- Add rule-pack versioning and compatibility checks.
- Build fixture-based regression suite for rule outputs.

**Acceptance criteria**
- Invalid rule packs fail fast with actionable diagnostics.
- Rule changes are regression-tested.

---

### 5) Complete Outbound Dependency Inference (Weeks 4-5)
**Objective:** Infer realistic external dependencies from real code.

**Tasks**
- Expand extraction:
  - `RestClient`,
  - `WebClient`,
  - `RestTemplate.exchange`,
  - Feign mappings/interfaces.
- Resolve path/method from constants and common wrappers where feasible.
- Infer request matchers (headers/body/query) from call arguments.
- Add dedupe + confidence scoring per inferred stub.

**Acceptance criteria**
- Stub inference precision/recall is measured on labeled samples.
- Inference handles mixed call styles in one controller flow.

---

### 6) Build Full Test Case Builder Logic (Weeks 5-6)
**Objective:** Generate meaningful, code-grounded test scenarios.

**Tasks**
- Generate scenarios from code semantics:
  - happy path,
  - validation failures,
  - auth/permission failures,
  - dependency failures/timeouts,
  - not found/conflict,
  - boundary conditions.
- Build fixture synthesis from DTO constraints + domain hints.
- Add scenario prioritization (`smoke`, `regression`, `risk`).
- Preserve rationale traceability from source -> scenario/prereq.

**Acceptance criteria**
- Generated scenarios align with controller/service logic.
- Reduced manual rewriting of generated plans.

---

### 7) Harden Mock Provider Ecosystem (Week 6)
**Objective:** Ensure provider abstraction remains consistent and extensible.

**Tasks**
- Finalize provider lifecycle contract (setup/stub/verify/teardown).
- Improve `WireMockProvider` output robustness.
- Harden templated provider variable substitution and validation.
- Add provider contract tests for semantic equivalence.

**Acceptance criteria**
- Same `StubSpec` renders equivalent semantics across providers.
- Provider misconfiguration yields clear errors.

---

### 8) Production-Grade Renderers (Weeks 6-7)
**Objective:** Emit high-quality artifacts with minimal manual fixes.

**Tasks**
- Improve TestNG renderer:
  - deterministic naming,
  - group assignments,
  - data providers,
  - setup/teardown conventions.
- Improve manual Markdown renderer:
  - Confluence-friendly structure,
  - clear steps, expected results, rationale sections.
- Add compile checks for generated TestNG code.
- Add golden-file tests for renderer outputs.

**Acceptance criteria**
- Generated Java compiles in validation harness.
- Markdown output is directly usable in Confluence.

---

### 9) IntelliJ Plugin UX Completion (Weeks 7-8)
**Objective:** Deliver a usable day-to-day IDE workflow.

**Tasks**
- Implement action entrypoint and target selection.
- Implement tool window tabs:
  - Plan,
  - Skeleton,
  - Manual Cases.
- Add insert/copy/export flows.
- Add confidence warnings and unsupported-pattern messages.

**Acceptance criteria**
- End users can generate and apply plans without CLI.
- Low-confidence decisions are visible and actionable.

---

### 10) CLI + Maven Surface Completion (Week 8)
**Objective:** Provide complete non-IDE workflows and CI usage.

**Tasks**
- Finalize command options:
  - target class/method,
  - output formats,
  - output paths,
  - provider override,
  - strict mode.
- Implement structured errors + non-zero exits on failures.
- Add e2e tests for CLI and Maven goal behavior.

**Acceptance criteria**
- CLI/Maven outputs are deterministic and CI-friendly.

---

### 11) Calibration, Trust, and Quality Gates (Weeks 8-9)
**Objective:** Quantify usefulness and reliability before release.

**Tasks**
- Build labeled corpus runner across multiple repos.
- Track:
  - classifier accuracy,
  - stub inference quality,
  - scenario usefulness,
  - skeleton edit distance.
- Define v0.1 release gates and block release if unmet.

**Acceptance criteria**
- Metrics dashboard demonstrates release readiness.

---

### 12) Safety, Observability, and Policy Controls (Week 9)
**Objective:** Ensure predictable behavior and pilot-safe telemetry.

**Tasks**
- Add optional telemetry with explicit opt-in.
- Add deterministic mode vs LLM-enhanced mode controls.
- Add safe-bail behavior for unsupported constructs.
- Add audit trail notes for LLM-enriched scenarios.

**Acceptance criteria**
- No silent failure modes.
- Policy-sensitive environments can run local-first.

---

### 13) Pilot Rollout and Hardening (Weeks 10-11)
**Objective:** Validate in real workflows and remove top friction.

**Tasks**
- Run pilot against selected repos and user workflows.
- Triage defects by impact (classifier, stubs, scenarios, output quality).
- Fix top-priority issues and re-run calibration.

**Acceptance criteria**
- Pilot exit criteria met and documented.

## Done Criteria (Full Functionality)
- Real PSI/JavaParser extraction (no scaffolded placeholders).
- Reliable layer classification with confidence traceability.
- High-fidelity scenario/prereq/stub generation from real code.
- Stable, high-quality TestNG/manual/JSON outputs.
- Complete IntelliJ, CLI, and Maven workflows.
- Calibration metrics meet release thresholds.

## Risks and Mitigations
- **Classifier drift:** keep labeled corpus and threshold tuning in CI.
- **Rule-pack fragility:** strict schema + fixture regressions.
- **Ecosystem edge cases:** detect-and-bail with explicit reasons.
- **Provider inconsistency:** contract tests across providers.
- **Output trust issues:** rationale trace and confidence surfaced to users.

## Suggested Next Action
Start with **Step 1 + Step 2** in the next sprint and gate merges on adapter parity tests early, so the rest of the pipeline builds on trustworthy input.
