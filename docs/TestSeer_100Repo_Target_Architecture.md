# TestSeer Target Architecture for 100+ Repos

> **Status:** Historical — long-range target; partially reflected in Phase 1 backend  
> **Last verified:** 2026-06-05  
> **Current delivery:** [CURRENT_STATUS.md](CURRENT_STATUS.md)

## Context
- Portfolio size: ~100 repositories
- Service topology: most repos have 3+ microservices; at least one has ~40 services
- Need: scalable, low-latency, high-confidence test planning and dependency-aware analysis

## Objectives
- Provide reliable test planning across many repos/services without full local rescans.
- Support incremental analysis on PRs and default branches.
- Enable cross-service dependency insights and change-impact queries.
- Keep local plugin/CLI responsive via cached central intelligence.

## Guiding Principles
- Local-first UX, central intelligence backend.
- Incremental indexing over full re-index.
- Schema-versioned facts and deterministic fallbacks.
- “Fail-safe over false-confidence” when evidence is insufficient.

## Recommended System Shape

```mermaid
flowchart LR
    scm[SCMEventsPRPushSchedule] --> queue[IngestionQueue]
    queue --> workers[IndexWorkers]
    workers --> parse[ParsersJavaParserSymbolSolverPSI]
    parse --> facts[FactEmitter]
    facts --> store[(FactStorePostgres)]
    facts --> graph[(GraphProjection)]
    store --> api[QueryAPITestSeerCore]
    graph --> api
    api --> ide[IntelliJPlugin]
    api --> cli[CLIMaven]
    api --> dash[CalibrationDashboard]
```

## Core Components

### 1) Repository/Service Registry
- Tracks `org/repo/service` boundaries and ownership.
- Stores build tool, language version, source roots, test roots, and runbook metadata.
- Supports manual overrides for non-standard layouts.

### 2) Ingestion Orchestrator
- Triggers:
  - PR updates
  - default branch pushes
  - scheduled nightly refresh
- Assigns jobs per service, not just per repo.
- Supports priority queues (active PRs first).

### 3) Analysis Workers
- Parse service source with JavaParser + SymbolSolver (CLI side).
- Extract normalized facts:
  - classes/methods/annotations
  - constructor + field dependencies
  - endpoint mappings
  - outbound calls and inferred protocols
- Compute transitive dependency depth (bounded by config).

### 4) Fact Store
- PostgreSQL as primary store (fast delivery, strong tooling).
- Schema partitioning:
  - `repo_id`, `service_id`, `commit_sha`
- Keep snapshots + deltas:
  - full baseline snapshot
  - changed-file incremental updates

### 5) Graph Projection Layer
- Materialized edges for fast traversals:
  - `class_depends_on_class`
  - `method_calls_method`
  - `service_calls_service`
  - `endpoint_calls_outbound`
- Can remain in Postgres initially; move to dedicated graph DB only if needed.

### 6) Query API Layer
- Read APIs for plugin/CLI:
  - class/service dependency graph
  - transitive impact
  - outbound map
  - confidence and provenance
- Response includes evidence traces and freshness metadata.

### 7) Surface Integrations
- IntelliJ plugin:
  - query central facts for rich context
  - local fallback when API unavailable
- CLI/Maven:
  - default to API-backed mode
  - strict local mode option

### 8) Calibration and Quality Service
- Tracks:
  - layer classification accuracy
  - stub inference precision/recall
  - skeleton edit distance
  - adoption/usage metrics
- Enforces release gates and alerts on drift.

## Data Model (Minimum Viable)

### Keys
- `org`, `repo`, `service`, `commit_sha`, `file_path`, `symbol_fqn`

### Fact Tables
- `class_facts`
- `method_facts`
- `constructor_dependency_facts`
- `field_dependency_facts`
- `endpoint_facts`
- `outbound_call_facts`
- `analysis_runs`

### Metadata Columns
- `evidence_source` (psi|javaparser|symbolsolver)
- `confidence`
- `schema_version`
- `indexed_at`

## Ingestion Strategy

### Baseline
- One-time full index per service on default branch.

### Incremental
- For PRs and pushes:
  - parse changed files
  - refresh dependent symbols in bounded neighborhood
  - update edge projections

### Freshness Targets
- PR updates visible within 2-5 minutes for active repos.
- Nightly full consistency sweep for drift correction.

## Scaling Strategy
- Horizontal workers with queue backpressure.
- Per-service parallelism; per-repo concurrency caps.
- Cache hot repos/services in query tier.
- Retry with dead-letter queue for parse failures.

## Reliability and Safety
- Strict mode: reject low-evidence plans.
- Partial mode: allow with confidence downgrade + warnings.
- Unsupported constructs produce explicit “cannot safely infer” results.
- Provenance attached to every high-impact suggestion.

## Security and Governance
- Least-privilege SCM tokens.
- Encrypt fact store at rest.
- PII/secrets redaction in extracted artifacts.
- Tenant/team-level access controls for cross-repo visibility.

## Phased Rollout

### Phase 1 (Weeks 1-4): Platform MVP
- Repo/service registry
- Ingestion queue + workers
- Basic fact store schema
- Query API for dependency and outbound facts

### Phase 2 (Weeks 5-8): TestSeer Integration
- Plugin/CLI API-backed enrichment
- Confidence + provenance surfaces
- Strict/partial mode rollout by team

### Phase 3 (Weeks 9-12): Reliability + Calibration
- Metric dashboards and quality gates
- Drift detection and automatic recalibration jobs
- Performance tuning for high-traffic repos

### Phase 4 (Weeks 13+): Advanced Intelligence
- Cross-repo change impact ranking
- Similar-test retrieval and dedup
- Knowledge graph augmentation (docs/PRDs/incidents)

## SLOs (Initial)
- Index job success rate: >= 99%
- P95 query latency: <= 300 ms
- PR freshness lag: <= 5 minutes
- Classification accuracy on labeled set: >= 90%

## Biggest Risks and Mitigations
- Service boundary ambiguity -> enforce registry and team overrides.
- Symbol resolution failures in complex builds -> fallback tiers + failure telemetry.
- Cost blow-up from full rescans -> incremental-first strategy + selective deep reindex.
- Trust erosion from weak evidence -> strict mode + rationale traces everywhere.

## Immediate Next Build Items
1. Create service registry schema and seed format.
2. Add ingestion job envelope (`repo/service/commit/changed_files`).
3. Implement worker skeleton writing facts to Postgres.
4. Add first query endpoints for dependency depth and outbound calls.
5. Wire current engine to consume API facts when available.
