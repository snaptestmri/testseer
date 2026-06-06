# TestSeer documentation index

Use this page to find the right document. **Canonical** docs describe current behavior; **Historical** docs record decisions and plans.

**Implementation status (2026-06-05):** [CURRENT_STATUS.md](CURRENT_STATUS.md)

## Operational — read these first

| Document | Status | Purpose |
|----------|--------|---------|
| [CURRENT_STATUS.md](CURRENT_STATUS.md) | **Canonical** | Shipped vs partial vs planned features (platform-wide) |
| [../testseer-backend/README.md](../testseer-backend/README.md) | **Canonical** | Run backend, API overview, Docker Compose |
| [../testseer-mcp/README.md](../testseer-mcp/README.md) | **Canonical** | MCP server setup and tools |
| [../intellij-plugin/README.md](../intellij-plugin/README.md) | **Canonical** | IntelliJ plugin build and run |
| [../testseer-backend/docs/openapi.yaml](../testseer-backend/docs/openapi.yaml) | **Canonical** | REST API contract |

## Backend architecture and plans

All Phase 1 backend design docs live in the backend repo folder:

| Document | Status | Purpose |
|----------|--------|---------|
| [../testseer-backend/docs/README.md](../testseer-backend/docs/README.md) | **Canonical** | Backend doc index |
| [../testseer-backend/docs/TestSeer_Phase1_Architecture.md](../testseer-backend/docs/TestSeer_Phase1_Architecture.md) | **Canonical** | Architecture, components, graph layer, APIs |
| [../testseer-backend/docs/graph-database-explained.md](../testseer-backend/docs/graph-database-explained.md) | **Canonical** | Graph model, Postgres CTEs vs Cypher, Apache AGE |
| [../testseer-backend/docs/archive/plans/](../testseer-backend/docs/archive/plans/README.md) | **Historical** | P1–P15 implementation plans |

## Contracts and governance

| Document | Status | Purpose |
|----------|--------|---------|
| [schema-contract.md](schema-contract.md) | **Canonical** | v0.1 JSON wire schema (v0.1 Java source external to this repo) |
| [schema-migration-notes.md](schema-migration-notes.md) | **Canonical** | Per-version migration notes |
| [rules-schema.md](rules-schema.md) | **Canonical** | YAML rule-pack schema |
| [../CHANGELOG.md](../CHANGELOG.md) | **Canonical** | Change log (required for contract edits) |
| [../scripts/contract-governance-check.sh](../scripts/contract-governance-check.sh) | **Canonical** | CI guard for contract + CHANGELOG |

**Governance rules:**

- Backend REST / OpenAPI changes → update `testseer-backend/docs/openapi.yaml` and `CHANGELOG.md`.
- Schema policy docs (`docs/schema-*.md`, `docs/rules-schema.md`) → update `CHANGELOG.md`.
- v0.1 JSON snapshots (`src/test/resources/snapshots/`) apply only when v0.1 engine source is in the checkout.
- Enforced by [`scripts/contract-governance-check.sh`](../scripts/contract-governance-check.sh).

## Historical and planning (platform / v0.1)

| Document | Status | Purpose |
|----------|--------|---------|
| [TestSeer_Central_Backend_PRD.md](../testseer-backend/docs/TestSeer_Central_Backend_PRD.md) | **Historical** | Original PRD; Appendix A retains Postgres vs Neo4j spike results |
| [TestSeer_100Repo_Target_Architecture.md](TestSeer_100Repo_Target_Architecture.md) | **Historical** | Long-range target architecture (100+ repos) |
| [TestSeer_Architecture_v0.1.md](TestSeer_Architecture_v0.1.md) | **Historical** | Original v0.1 engine design |
| [TestSeer_Full_Functionality_Plan.md](TestSeer_Full_Functionality_Plan.md) | **Historical** | v0.1 engine build plan |
| [TestSeer_Completion_Execution_Plan.md](TestSeer_Completion_Execution_Plan.md) | **Historical** | v0.1 completion checklist |

## Repository layout

```
testseer/                      # this repo (github.com/snaptestmri/testseer)
├── testseer-backend/          # submodule → snaptestmri/testseer-backend
├── testseer-mcp/              # submodule → snaptestmri/testseer-mcp
├── intellij-plugin/           # local PSI plugin (in this repo)
├── docs/                      # platform index, CURRENT_STATUS, v0.1 + schema contracts
├── scripts/
├── README.md
└── CHANGELOG.md
```

Clone with `git clone --recurse-submodules` so `testseer-backend/` and `testseer-mcp/` are populated.

The v0.1 Java engine (`src/`, root `pom.xml`) is maintained separately and is **not** part of this workspace.
