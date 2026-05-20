#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
PROJECT_ID="${FIREBASE_PROJECT_ID:-my-share-finance}"
MODE="${1:-plan}"
TIMEOUT="${TEST_LAB_TIMEOUT:-5m}"
RESULTS_HISTORY="${TEST_LAB_RESULTS_HISTORY:-My Share release smoke}"
RESULTS_LABEL="${TEST_LAB_MATRIX_LABEL:-my-share-release-smoke}"
COST_CONFIRMATION="${TEST_LAB_CONFIRM_COST:-}"

APP_APK="$ROOT_DIR/app/build/outputs/apk/debug/app-debug.apk"
TEST_APK="$ROOT_DIR/app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk"

DEFAULT_DEVICES=(
  "model=SmallPhone.arm,version=35,locale=en,orientation=portrait"
  "model=MediumPhone.arm,version=35,locale=en,orientation=portrait"
)

print_usage() {
  cat <<USAGE
Usage: $(basename "$0") [plan|robo|instrumentation|all]

Environment:
  FIREBASE_PROJECT_ID        Firebase project id. Default: my-share-finance
  TEST_LAB_TIMEOUT           Per-device timeout. Default: 5m
  TEST_LAB_RESULTS_HISTORY   Firebase Test Lab history name.
  TEST_LAB_MATRIX_LABEL      Label shown in Firebase Test Lab.
  TEST_LAB_DEVICES           Optional semicolon-separated --device specs.
  TEST_LAB_CONFIRM_COST      Must be set to "yes" before remote Test Lab runs execute.

Examples:
  scripts/run_firebase_test_lab.sh plan
  TEST_LAB_CONFIRM_COST=yes scripts/run_firebase_test_lab.sh robo
  TEST_LAB_CONFIRM_COST=yes TEST_LAB_TIMEOUT=8m scripts/run_firebase_test_lab.sh all
  TEST_LAB_CONFIRM_COST=yes TEST_LAB_DEVICES='model=SmallPhone.arm,version=35,locale=en,orientation=portrait;model=SmallPhone.arm,version=35,locale=de,orientation=portrait' scripts/run_firebase_test_lab.sh robo
USAGE
}

device_args=()
if [[ -n "${TEST_LAB_DEVICES:-}" ]]; then
  IFS=';' read -r -a configured_devices <<< "$TEST_LAB_DEVICES"
  for device in "${configured_devices[@]}"; do
    [[ -n "$device" ]] && device_args+=("--device=$device")
  done
else
  for device in "${DEFAULT_DEVICES[@]}"; do
    device_args+=("--device=$device")
  done
fi

build_debug_apk() {
  "$ROOT_DIR/gradlew" -p "$ROOT_DIR" :app:assembleDebug
}

build_android_test_apk() {
  "$ROOT_DIR/gradlew" -p "$ROOT_DIR" :app:assembleDebugAndroidTest
}

require_cost_confirmation() {
  if [[ "$COST_CONFIRMATION" != "yes" ]]; then
    cat <<WARNING
Refusing to start Firebase Test Lab remotely without explicit cost confirmation.

Firebase Test Lab has no-cost daily quota, but usage beyond quota can be billed on Blaze.
Run this command again with TEST_LAB_CONFIRM_COST=yes after checking the device matrix:

  TEST_LAB_CONFIRM_COST=yes scripts/run_firebase_test_lab.sh $MODE

Current matrix:
WARNING
    printf '  --device=%s\n' "${device_args[@]#--device=}"
    exit 3
  fi
}

print_plan() {
  cat <<PLAN
Firebase Test Lab release smoke plan

Project: $PROJECT_ID
Timeout: $TIMEOUT per device
History: $RESULTS_HISTORY
Label:   $RESULTS_LABEL

Device matrix:
PLAN
  printf '  - %s\n' "${device_args[@]#--device=}"
  cat <<PLAN

Recommended cadence:
  - Run robo before Play internal/closed test uploads.
  - Run instrumentation only after deterministic smoke tests are enabled.
  - Keep the matrix small unless a specific device/locale bug needs coverage.

No remote Test Lab run was started.
PLAN
}

run_robo() {
  require_cost_confirmation
  build_debug_apk
  gcloud firebase test android run \
    --project="$PROJECT_ID" \
    --type=robo \
    --app="$APP_APK" \
    --timeout="$TIMEOUT" \
    --results-history-name="$RESULTS_HISTORY" \
    --client-details=matrixLabel="$RESULTS_LABEL-robo" \
    --performance-metrics \
    --record-video \
    "${device_args[@]}"
}

run_instrumentation() {
  require_cost_confirmation
  build_debug_apk
  build_android_test_apk
  gcloud firebase test android run \
    --project="$PROJECT_ID" \
    --type=instrumentation \
    --app="$APP_APK" \
    --test="$TEST_APK" \
    --timeout="$TIMEOUT" \
    --results-history-name="$RESULTS_HISTORY" \
    --client-details=matrixLabel="$RESULTS_LABEL-instrumentation" \
    --use-orchestrator \
    --performance-metrics \
    --record-video \
    "${device_args[@]}"
}

case "$MODE" in
  plan)
    print_plan
    ;;
  robo)
    run_robo
    ;;
  instrumentation)
    run_instrumentation
    ;;
  all)
    run_robo
    run_instrumentation
    ;;
  -h|--help|help)
    print_usage
    ;;
  *)
    print_usage
    exit 2
    ;;
esac
