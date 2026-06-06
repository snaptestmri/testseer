# TestSeer

TestSeer indexes Java codebases, builds a cross-service dependency graph, and answers **what to test when code changes** — via REST API and Cursor MCP tools.

**Current status:** [docs/CURRENT_STATUS.md](docs/CURRENT_STATUS.md)

## Clone

```bash
git clone --recurse-submodules https://github.com/snaptestmri/testseer.git
cd testseer
```

Without `--recurse-submodules`, init submodules after clone:

```bash
git submodule update --init --recursive
```

## Quick start

| Component | Where | Command |
|-----------|-------|---------|
| **Backend** (index, graph, impact API) | [`testseer-backend/`](testseer-backend/README.md) | Docker Compose + `mvn spring-boot:run` (see backend README) |
| **MCP server** (Cursor agent tools) | [`testseer-mcp/`](testseer-mcp/README.md) | `npm install && npm run build` |
| **IntelliJ plugin** (local PSI only) | [`intellij-plugin/`](intellij-plugin/README.md) | Requires `io.testseer:testseer-v01` in Maven Local; then `./gradlew runIde` |

This workspace does **not** include the v0.1 engine source (`src/`, root `pom.xml`). Backend and MCP are the active delivery path.

## Documentation map

Full index: [`docs/README.md`](docs/README.md)

### Operational (start here)

- [`docs/CURRENT_STATUS.md`](docs/CURRENT_STATUS.md) — what is shipped vs planned
- [`testseer-backend/README.md`](testseer-backend/README.md) — ingestion, graph API, impact analysis
- [`testseer-mcp/README.md`](testseer-mcp/README.md) — MCP tools for Cursor
- [`testseer-backend/docs/openapi.yaml`](testseer-backend/docs/openapi.yaml) — REST API contract

### Contracts and governance

- [`docs/schema-contract.md`](docs/schema-contract.md) — v0.1 JSON plan schema (historical; v0.1 source external)
- [`docs/schema-migration-notes.md`](docs/schema-migration-notes.md) — schema migration history
- [`docs/rules-schema.md`](docs/rules-schema.md) — YAML rule-pack schema (v0.1 engine)
- [`CHANGELOG.md`](CHANGELOG.md) — project change log
- [`scripts/contract-governance-check.sh`](scripts/contract-governance-check.sh) — CI: contract/OpenAPI edits require CHANGELOG

### Concepts and backend architecture

- [`testseer-backend/docs/README.md`](testseer-backend/docs/README.md) — backend doc index
- [`testseer-backend/docs/graph-database-explained.md`](testseer-backend/docs/graph-database-explained.md) — graph model, Postgres CTEs vs Cypher
- [`testseer-backend/docs/TestSeer_Phase1_Architecture.md`](testseer-backend/docs/TestSeer_Phase1_Architecture.md)
- [`testseer-backend/docs/TestSeer_Phase1_SystemDesign.md`](testseer-backend/docs/TestSeer_Phase1_SystemDesign.md)
- [`testseer-backend/docs/TestSeer_Phase1_User_Stories.md`](testseer-backend/docs/TestSeer_Phase1_User_Stories.md)

### Historical / planning

- [`testseer-backend/docs/TestSeer_Central_Backend_PRD.md`](testseer-backend/docs/TestSeer_Central_Backend_PRD.md) — original PRD + Postgres vs Neo4j decision
- [`docs/TestSeer_100Repo_Target_Architecture.md`](docs/TestSeer_100Repo_Target_Architecture.md)
- [`docs/TestSeer_Architecture_v0.1.md`](docs/TestSeer_Architecture_v0.1.md) — v0.1 engine design (source not in this repo)
- [`docs/TestSeer_Full_Functionality_Plan.md`](docs/TestSeer_Full_Functionality_Plan.md)
- [`docs/TestSeer_Completion_Execution_Plan.md`](docs/TestSeer_Completion_Execution_Plan.md) — v0.1 checklist (source not in this repo)
- [`testseer-backend/docs/archive/plans/`](testseer-backend/docs/archive/plans/README.md) — P1–P15 implementation plans

## Contract governance

- **Backend API changes:** update `testseer-backend/docs/openapi.yaml` and add an entry to `CHANGELOG.md`.
- **Schema contract docs** (`docs/schema-*.md`, `docs/rules-schema.md`): update `CHANGELOG.md` when policy changes.
- v0.1 JSON snapshot tests apply only when the v0.1 engine source is present in the checkout.

## IntelliJ plugin

Local PSI-based test plan generation. **Does not** call `testseer-backend`. Requires the `testseer-v01` artifact published to Maven Local from a separate v0.1 engine checkout.

See [`intellij-plugin/README.md`](intellij-plugin/README.md).
