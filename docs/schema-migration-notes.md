# TestSeer Schema Migration Notes

> **Status:** Canonical  
> **Last verified:** 2026-06-05

This log tracks schema changes from `v1.0` onward and provides migration guidance.

## v1.0.0 (Baseline)

- Established `VersionedTestPlan` envelope with `schemaVersion` + `plan`.
- Baseline plan shape includes:
  - `warnings` on `TestPlan`
  - scenario metadata fields: `labels`, `sourceTraceRefs`
  - deterministic timestamp field: `generatedAt`

Consumer guidance:

- Treat unknown fields as non-fatal.
- Parse by `schemaVersion` first.

## v1.0.1 (Domain model split, no JSON break)

- Internal Java contracts moved from `model/Model.java` to:
  - `model/plan/PlanModel.java`
  - `model/codeintel/CodeIntelModel.java`
  - `model/render/RenderModel.java`
- Legacy `Model.java` deleted 2026-06-05 (internal cleanup only).
- No JSON wire-shape changes; schema remains `1.0`.

Consumer guidance:

- No payload migration needed.
- Java integrators should update imports to new package locations.

## Migration Checklist for Future Changes

When adding a new migration entry:

1. Record old and new fields/paths.
2. Specify if change is additive (minor) or breaking (major).
3. Add producer fallback behavior (if any).
4. Add consumer migration steps and example payload diffs.
5. Link to changelog entry.
