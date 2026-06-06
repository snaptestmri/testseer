# API rule pack (YAML) schema

> **Status:** Canonical  
> **Last verified:** 2026-06-05

Rule packs are loaded from `src/main/resources/rules/api-rule-pack.yml` or a user-supplied file path. The engine validates shape with `RulePackSchemaValidator` before use.

## Top-level keys (allowed only)

| Key | Required | Description |
|-----|----------|-------------|
| `schemaVersion` | yes | Integer; engine supports `1`–`2` (see `ApiRulePackService` constants). |
| `version` | yes | Rule-pack release label (string/number), used in prerequisite rationale text. |
| `rules` | yes | Non-empty list of rule objects. |
| `compatibility` | no | Build-tool and schema-range hints (see below). |
| `conflictResolution` | no | `collect_all` (default) or `first_match`. |

Unknown top-level keys are rejected.

## `compatibility` (optional mapping)

| Key | Description |
|-----|-------------|
| `supportedBuildTools` | List of build tools (e.g. `maven`, `gradle`); if non-empty, the model’s `buildTool` hint must match. |
| `minSchemaVersion` / `maxSchemaVersion` | Range checked against the engine’s supported schema band. |

Unknown `compatibility` keys are rejected.

## Each rule (mapping)

| Key | Required | Description |
|-----|----------|-------------|
| `id` | yes | Unique string identifier (also used as prerequisite key). |
| `detect` | yes | **Java `Pattern` regex** matched against a detection haystack: package, class name, class/method annotations, imports, and method bodies. Uses `CASE_INSENSITIVE` and `DOTALL`. Globs like `*.api.*` are invalid; use e.g. `\\.api\\.` or `com\\.acme\\.api`. |
| `emit` | yes | Prerequisite value text (shown on the plan). |
| `priority` | no | Sort order for matching rules (lower first). Default `100`. |
| `description` | no | Reserved for documentation; not required by the engine. |

Unknown rule keys are rejected.

## `conflictResolution`

- **`collect_all`**: Every rule whose `detect` matches contributes a prerequisite (in priority order), then the fixed `rest-assured` prerequisite is appended.
- **`first_match`**: Only the first matching rule in that ordered list contributes a rule-derived prerequisite; `rest-assured` is still appended.

Invalid values fail validation at load time.

## Change process

1. Keep `detect` expressions valid regex (run tests / load locally).
2. For contract or snapshot-related repo updates, follow `docs/schema-contract.md` and update `CHANGELOG.md` when required by project policy.

## Golden regression (parsed model → prerequisites + stubs)

End-to-end expectations for the rule pack + stub inference live under `src/test/resources/rules/fixtures/golden/`. Each case includes `parsed-model.json`, `expected.json`, and (when not using the bundled default pack) `rule-pack.yml`. `RulePackGoldenRegressionTest` compares engine output to `expected.json` (stubs are sorted for stable diffs). After intentional changes, refresh goldens with:

`mvn test -Dtest=RulePackGoldenRegressionTest -Dtestseer.golden.update=true`
