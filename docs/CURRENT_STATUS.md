# TestSeer — current implementation status

> **Last verified:** 2026-06-05  
> **This repo contains:** `testseer-backend/`, `testseer-mcp/`, `intellij-plugin/`, and documentation.  
> The v0.1 Java engine (`src/`, root `pom.xml`) is **not** in this workspace.

## Shipped (local development)

| Area | Status | Location |
|------|--------|----------|
| Service registry CRUD | Done | `testseer-backend` — `/registry/services` |
| GitHub webhook ingestion | Done | `/webhook/github` + Kafka workers |
| Admin index / discover / local index | Done | `/admin/index/*`, `/admin/discover` |
| Symbol + outbound facts | Done | `/v1/facts/*` |
| Graph projection + 5 graph REST endpoints | Done | `/v1/graph/*` |
| PR impact analysis | Done | `GET /v1/impact/pr` |
| Service description (LLM) | Done | `/v1/services/{id}/description` (needs `ANTHROPIC_ENABLED=true`) |
| Freshness + Redis cache | Done | `ResponseEnvelope`, `/v1/status/{serviceId}` |
| MCP server (8 tools) | Done | `testseer-mcp/` |
| OpenAPI spec | Done | `testseer-backend/docs/openapi.yaml` |

## Partial / blocked

| Area | Status | Notes |
|------|--------|-------|
| MCP `testseer_get_gaps` | **Blocked** | Backend `GET /v1/gaps` not implemented (P12). Use `testseer_get_impact` → `missingTestClasses` for commit-scoped gaps only. |
| Graph `crossServiceBoundary` | Internal only | Implemented in `GraphProjectionService`; **no REST route** |
| IntelliJ plugin | Local PSI only | No backend REST integration; requires external `testseer-v01` JAR in Maven Local |
| Nightly batch scheduler | Not wired | `NIGHTLY` job type exists; no Cloud Scheduler / cron in repo |
| GCP Pub/Sub → plugin push | Not built | Cache invalidation via Redis; plugin does not subscribe |

## Not started (Phase 2+)

| Area | Plan reference |
|------|----------------|
| Portfolio-wide test gap API | [P12](../testseer-backend/docs/archive/plans/2026-06-05-p12-gap-detection.md) |
| IntelliJ impact consumer | [P15](../testseer-backend/docs/archive/plans/2026-06-05-p15-intellij-impact-consumer.md) |
| PR comment bot | [P14](../testseer-backend/docs/archive/plans/2026-06-05-p14-pr-comment-bot.md) |
| Plugin/CLI API-backed test plans | Phase 1 PRD surface integration (unchecked) |

## Primary surfaces today

1. **REST API** — `testseer-backend` (Swagger at `/swagger-ui/index.html`)
2. **MCP in Cursor** — `testseer-mcp` wrapping the REST API
3. **IntelliJ** — local test plan generation only (legacy v0.1 engine dependency)

## Authoritative API reference

Always prefer **[testseer-backend/docs/openapi.yaml](../testseer-backend/docs/openapi.yaml)** over architecture markdown when they diverge.
