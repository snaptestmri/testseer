# TestSeer Schema Contract

> **Status:** Canonical (v0.1 JSON plan wire format)  
> **Last verified:** 2026-06-05  
> **Scope:** Applies to v0.1 engine JSON output. **Backend REST** uses [OpenAPI](../testseer-backend/docs/openapi.yaml), not this envelope.

This document defines versioning policy, deprecation policy, and compatibility rules for TestSeer JSON outputs, especially `VersionedTestPlan`.

## Scope

The policy applies to:

- JSON output produced by `JsonPlanRenderer`
- Envelope record `VersionedTestPlan`
- Core contract records in `model/plan/*`, `model/codeintel/*`, and `model/render/*` when serialized

## Versioning Policy

TestSeer uses semantic versioning for schema:

- **Major** (`X.0`): breaking contract changes
  - Example: removing a required field
  - Example: changing field type incompatibly (`string` -> `object`)
  - Consumer action: required upgrade and migration
- **Minor** (`1.Y`): backward-compatible additions
  - Example: adding optional fields
  - Example: adding optional enum values where consumers can ignore unknown values
  - Consumer action: optional adoption
- **Patch** (`1.0.Z`): non-structural fixes and clarifications
  - Example: docs clarifications, typo corrections in non-contract text
  - Consumer action: no parser changes

`VersionedTestPlan.schemaVersion` is the source of truth for payload compatibility.

## Deprecation Policy

- Deprecations are announced in `CHANGELOG.md`.
- Deprecated fields are retained for at least one **minor** release before removal.
- Deprecated fields are documented in migration notes with:
  - first deprecated version
  - planned removal version
  - replacement field/path
- Hard removals only occur in a **major** schema version.

## Backward Compatibility Matrix

- Producer `1.x` -> Consumer expecting `1.x`: compatible (patch/minor within major 1).
- Producer `1.x` -> Consumer expecting `2.x`: not assumed compatible unless dual-reader support exists.
- Producer `2.x` -> Consumer expecting `1.x`: incompatible without migration.
- Producer `2.x` -> Consumer expecting `2.x`: compatible (patch/minor within major 2).

Interpretation rules:

- A consumer built for `1.x` must accept any `1.x` payload.
- A consumer built for `2.x` should not be assumed to accept `1.x` without explicit compatibility support.
- Unknown optional fields in same major version should be ignored by consumers.

## Required Change Process

For any contract-affecting change:

1. Update schema version as needed (major/minor/patch).
2. Update snapshot fixtures under `src/test/resources/snapshots/`.
3. Add `CHANGELOG.md` entry describing the contract impact.
4. Update `docs/schema-migration-notes.md` if migration behavior changes.
5. Keep tests green (`mvn clean test`).
