#!/usr/bin/env bash
# The three Gradle tasks, in the order .github/workflows/android.yml runs them,
# then the artefacts out to /out.
#
# WHY A SCRIPT RATHER THAN A CMD. lintDebug is `continue-on-error: true` in the
# workflow, so a lint warning must not fail this build either — but the two
# tasks around it MUST fail it. A single `./gradlew a b c` cannot express that;
# `set -e` plus one explicit `|| true` on the middle task can.
#
# Anything after -- is passed to every Gradle invocation, so a caller can add
# -Png.naijaleague.catalogueUrl=... or --stacktrace without editing this file.
set -euo pipefail

OUT_DIR="${OUT_DIR:-/out}"
GRADLE_ARGS=(--no-daemon "$@")

echo "==> Unit tests"
./gradlew testDebugUnitTest "${GRADLE_ARGS[@]}" --stacktrace

echo "==> Lint"
# Non-fatal, exactly as in CI. The report still lands in /out, which is the
# point of running it at all.
./gradlew lintDebug "${GRADLE_ARGS[@]}" || echo "lint reported problems (not fatal, matching CI)"

echo "==> Assemble debug APK"
./gradlew assembleDebug "${GRADLE_ARGS[@]}" --stacktrace

if [ ! -d "$OUT_DIR" ]; then
  echo "No $OUT_DIR to write to. Run with -v \"\$PWD/artifacts:$OUT_DIR\"." >&2
  exit 1
fi

mkdir -p "$OUT_DIR/reports"
cp app/build/outputs/apk/debug/*.apk "$OUT_DIR/"
# Test and lint reports, so a failure is readable without a second run.
cp -r app/build/reports/. "$OUT_DIR/reports/" 2>/dev/null || true

apk=$(find "$OUT_DIR" -maxdepth 1 -name '*.apk' | head -1)
printf '==> %s (%s)\n' "$(basename "$apk")" "$(du -h "$apk" | cut -f1)"
