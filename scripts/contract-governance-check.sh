#!/usr/bin/env bash
# Fail when contract-sensitive paths change but CHANGELOG.md is not updated.
# Usage:
#   scripts/contract-governance-check.sh [BASE_REF]
# Env:
#   BASE_REF — git ref to diff against (default: origin/main, else HEAD~1).
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

BASE="${1:-${BASE_REF:-origin/main}}"
if ! git rev-parse --verify "$BASE" >/dev/null 2>&1; then
  if git rev-parse --verify HEAD~1 >/dev/null 2>&1; then
    BASE="HEAD~1"
  else
    echo "contract-governance-check: no suitable base ref; skipping." >&2
    exit 0
  fi
fi

RANGE="$BASE...HEAD"
CHANGED="$(git diff --name-only "$RANGE" 2>/dev/null || true)"
if [[ -z "$CHANGED" ]]; then
  exit 0
fi

TRIGGER='^(src/test/resources/snapshots/|docs/schema-contract\.md|docs/schema-migration-notes\.md|docs/rules-schema\.md|testseer-backend/docs/openapi\.yaml)'
if ! echo "$CHANGED" | grep -qE "$TRIGGER"; then
  exit 0
fi

if echo "$CHANGED" | grep -Fxq "CHANGELOG.md"; then
  exit 0
fi

echo "Contract-related files changed vs $BASE but CHANGELOG.md is not in the same diff." >&2
echo "Update CHANGELOG.md to describe the contract or documentation impact." >&2
exit 1
