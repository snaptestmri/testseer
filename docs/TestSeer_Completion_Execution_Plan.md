# TestSeer Completion Execution Plan

> **Status:** Historical — v0.1 engine checklist; **v0.1 source is not in this repo**  
> **Last verified:** 2026-06-05  
> **Platform status:** [CURRENT_STATUS.md](CURRENT_STATUS.md)

## Objective
Ship TestSeer v0.1 engine from scaffold to full functionality. **Backend/MCP delivery is tracked separately** in Phase 1 docs and [CURRENT_STATUS.md](CURRENT_STATUS.md).

## Current Status Summary
- Step 1: **Done in v0.1 engine** (not verifiable in this repo — no `src/`)
- Step 2–13: v0.1 engine work — **N/A in this workspace**; see historical plans

## Delivery Strategy
- Phase A (Stabilize + Inference): Steps 4-6
- Phase B (Providers + Renderers + Surfaces): Steps 7-10
- Phase C (Trust + Safety + Rollout): Steps 11-13
- Release gate: all acceptance criteria satisfied and CI quality gates enforced

---

## Step 1: Stabilize Domain Contracts (Close Remaining Gaps)

> **Repo note:** Step 1 was completed in the v0.1 engine checkout. This workspace has no `src/` — items below are historical record only.

### Already Done (v0.1 engine — external checkout)
- Core contracts exist for test plan entities.
- JSON output includes explicit schema version envelope.
- Snapshot test exists for JSON output shape.
- Split monolithic `Model.java` into focused packages:
  - `model/plan/PlanModel.java`
  - `model/codeintel/CodeIntelModel.java`
  - `model/render/RenderModel.java`
  - Legacy `Model.java` removed (2026-06-05)
- Schema contract doc: [`schema-contract.md`](schema-contract.md) (versioning, deprecation, compatibility)
- Migration notes: [`schema-migration-notes.md`](schema-migration-notes.md)
- Contract governance script: `scripts/contract-governance-check.sh`

### Remaining Work
- _(none for Step 1 — closed)_

### Acceptance Criteria
- Domain model is package-organized and easier to evolve.
- Schema/version policy is documented and linked from README.
- Any contract change requires snapshot update + explicit changelog entry.

### Validation
- Snapshot tests for JSON outputs remain green.
- CI check fails if schema changes without snapshot updates.

---

## Step 2: Real Code Intelligence (Complete PSI Side)
### Already Done
- JavaParser + SymbolSolver pipeline implemented.
- Parity tests and Petclinic compatibility test exist.

### Remaining Work
- Implement full PSI traversal path for IntelliJ runtime:
  - class/method annotations
  - constructor/field dependencies
  - outbound call graph hints
  - sibling test and source-set hints
- Expand parity suite:
  - real controllers with mixed styles
  - edge cases (inner classes, inheritance, abstract controllers)
- Add adapter drift dashboard in test output.

### Acceptance Criteria
- PSI and JavaParser outputs are semantically equivalent for target corpus.
- IntelliJ path no longer depends on synthetic fallback for standard cases.

### Validation
- Parity test suite across fixture corpus.
- Golden fingerprints per adapter for selected classes.

---

## Step 3: Production Layer Classifier (Finalize)
### Already Done
- Multi-signal scoring model with confidence thresholds.
- Structured trace (`rulesFired`, `scoreBreakdown`, `lowConfidence`).
- Deterministic low-confidence fallback and tests.

### Remaining Work
- Externalize classifier weights and threshold config:
  - YAML/JSON config under `.testseer/classifier.yml`
- Add calibration-driven threshold tuning command.
- Add confidence calibration report artifact in CI.

### Acceptance Criteria
- Classifier behavior can be tuned without code changes.
- Confidence thresholds are validated on labeled corpus.

### Validation
- Unit tests for config loading + deterministic scoring.
- Regression test on benchmark corpus.

---

## Step 4: Rule Engine + YAML Governance
### Already Done
- Basic YAML validation.
- Priority ordering and duplicate ID checks.

### Remaining Work
- Introduce formal rule-pack schema and validator:
  - required fields/types
  - enum constraints
  - compatibility version range
- Add conflict resolution strategy:
  - precedence rules
  - tie-break semantics
- Add hot-reload for local rule pack development.
- Create fixture-based regression suite:
  - input parsed models -> expected prerequisites/stubs.

### Acceptance Criteria
- Invalid packs fail fast with actionable errors.
- Rule behavior changes are visible and regression-tested.

### Validation
- Schema validation tests (positive/negative).
- Fixture regression snapshots in CI.

---

## Step 5: Outbound Dependency Inference
### Already Done
- Supports simple client calls, `RestTemplate.exchange`, and Feign mapping patterns.
- Basic dedupe implemented.

### Remaining Work
- Add constant/wrapper resolution for URL and method extraction.
- Expand call style support:
  - `WebClient` chained calls
  - `RestClient` fluent APIs
  - interface-based Feign signatures
- Infer request matcher details from args:
  - headers/body/query matching hints
- Add per-stub confidence score and rationale.
- Build labeled evaluation harness for precision/recall.

### Acceptance Criteria
- Mixed call styles are correctly inferred in one flow.
- Precision/recall tracked and above agreed threshold.

### Validation
- Labeled inference benchmark suite.
- False-positive/false-negative reports generated in CI.

---

## Step 6: Full Test Case Builder Logic
### Already Done
- Happy path, validation failure, and dependency failure scenarios exist.

### Remaining Work
- Add scenario families:
  - auth/permission failures
  - not found/conflict
  - timeout/circuit-breaker behavior
  - boundary and nullability edges
- Add fixture synthesis from DTO constraints and domain hints.
- Add scenario priority tags (`smoke`, `regression`, `risk`).
- Preserve traceability links:
  - source signal -> prerequisite/stub/scenario.

### Acceptance Criteria
- Scenarios align with real controller/service behavior.
- Manual rewrite effort measurably reduced.

### Validation
- Scenario quality review rubric with sample scoring.
- Golden scenario snapshots on reference controllers.

---

## Step 7: Mock Provider Ecosystem
### Already Done
- Provider lifecycle contract exists.
- WireMock + generic template provider implemented.

### Remaining Work
- Harden WireMock verb/path rendering correctness.
- Strengthen template provider:
  - strict variable substitution
  - missing variable diagnostics
  - schema for provider templates
- Add provider equivalence contract tests:
  - same `StubSpec` across providers yields equivalent semantics.

### Acceptance Criteria
- Provider misconfiguration errors are explicit and actionable.
- Semantic parity maintained across provider implementations.

### Validation
- Contract test suite for provider outputs.
- Negative tests for malformed templates.

---

## Step 8: Production-Grade Renderers
### Already Done
- TestNG/manual/JSON outputs implemented.
- JSON snapshot test exists.
- Confidence diagnostics surfaced in manual and TestNG output.

### Remaining Work
- Improve TestNG generation:
  - deterministic method naming
  - consistent grouping strategy
  - optional data provider scaffolds
  - setup/teardown normalization
- Improve manual markdown for Confluence readability.
- Add generated TestNG compile validation harness.
- Add golden-file tests for all renderers.

### Acceptance Criteria
- Generated Java compiles in validation harness.
- Markdown is directly usable by QA/Confluence workflows.

### Validation
- `javac`/Maven compile checks on generated artifacts.
- Golden output comparisons for each renderer.

---

## Step 9: IntelliJ Plugin UX Completion
### Already Done
- Basic IntelliJ facade exists.

### Remaining Work
- Build plugin action entrypoint and target picker.
- Implement tool window tabs:
  - Plan
  - Skeleton
  - Manual Cases
- Add insert/copy/export interactions.
- Surface low-confidence and unsupported-pattern warnings in UI.

### Acceptance Criteria
- End users can run end-to-end plan generation from IDE workflow.
- Confidence warnings are visible and understandable.

### Validation
- IntelliJ integration tests + manual usability test checklist.

---

## Step 10: CLI + Maven Surface Completion
### Already Done
- CLI and Maven goal run pipeline with key flags.
- Low-confidence warning printed in CLI.

### Remaining Work
- Add complete options:
  - target class/method
  - output formats selection
  - output path overrides
  - strict/fail-fast controls
- Add structured error payloads and exit codes.
- Add e2e tests for CLI and Maven behavior.

### Acceptance Criteria
- Deterministic, CI-friendly CLI/Maven outputs and exits.

### Validation
- End-to-end command tests in CI.
- Snapshot tests for CLI error and success outputs.

---

## Step 11: Calibration, Trust, Quality Gates
### Already Done
- Calibration primitives + baseline tests exist.

### Remaining Work
- Create multi-repo labeled corpus runner.
- Track metrics:
  - classifier accuracy
  - stub inference precision/recall
  - scenario usefulness score
  - skeleton edit distance
- Define release gates and enforce them in CI.

### Acceptance Criteria
- Metrics indicate release readiness and block unsafe releases.

### Validation
- CI gate job fails on threshold violations.
- Metrics report artifact published per run.

---

## Step 12: Safety, Observability, Policy Controls
### Already Done
- Strict mode and confidence surfacing are present.

### Remaining Work
- Add optional telemetry with explicit opt-in and redaction policy.
- Add deterministic-only mode vs LLM-enhanced mode policy controls.
- Add safe-bail behavior for unsupported constructs with explicit reason codes.
- Add audit notes for LLM-enriched scenario contributions.

### Acceptance Criteria
- No silent failures.
- Policy-sensitive environments can run local-first with deterministic mode.

### Validation
- Safety behavior tests for unsupported constructs.
- Telemetry opt-in/off integration tests.

---

## Step 13: Pilot Rollout and Hardening
### Already Done
- No formal pilot execution artifacts yet.

### Remaining Work
- Run pilot on selected repositories and workflows.
- Collect and triage defects by category:
  - classifier
  - inference
  - scenarios
  - renderer quality
  - UX
- Execute top-priority fixes and rerun calibration.
- Document pilot exit decision and residual risks.

### Acceptance Criteria
- Pilot exit criteria met and documented.

### Validation
- Pilot report with issue burndown and pre/post metrics.

---

## Milestone Plan
### Milestone 1 (Weeks 1-2)
- Finish Step 4 and Step 5 foundations.
- Deliver rule schema validator + expanded inference coverage.

### Milestone 2 (Weeks 3-4)
- Complete Step 6 and Step 7.
- Deliver stronger scenario quality and provider contract tests.

### Milestone 3 (Weeks 5-6)
- Complete Step 8, Step 9, Step 10.
- Deliver production renderers + complete IDE/CLI/Maven surface behaviors.

### Milestone 4 (Weeks 7-8)
- Complete Step 11 and Step 12.
- Enforce CI quality gates + safety controls.

### Milestone 5 (Weeks 9-10)
- Execute Step 13 pilot and final hardening.
- Ship readiness review and release candidate.

---

## CI/Quality Gates to Add
- Contract snapshots for JSON/renderer outputs.
- Adapter parity suite (PSI vs JavaParser).
- Classifier calibration gate.
- Stub inference precision/recall gate.
- Generated TestNG compile gate.
- CLI/Maven e2e gate.
- Safety policy gate (strict/deterministic/unsupported construct behavior).

---

## Risks and Mitigations
- Classifier drift -> maintain labeled corpus and periodic recalibration.
- Rule-pack regressions -> strict schema + fixture regression snapshots.
- Inference edge cases -> safe-bail with explicit reasoning.
- Provider inconsistency -> contract tests across providers.
- Trust gap -> always expose confidence and rationale.

---

## Definition of Done (Final)
- Real PSI + JavaParser extraction without scaffold dependence for standard targets.
- Reliable and explainable layer classification with structured diagnostics.
- High-fidelity prerequisites/stubs/scenarios grounded in code semantics.
- Stable renderers with compile-validated generated Java.
- Complete IntelliJ, CLI, and Maven workflows.
- Calibration metrics and safety gates enforced in CI.
- Pilot exit criteria met and documented for release.
