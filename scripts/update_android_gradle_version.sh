#!/usr/bin/env bash
set -euo pipefail

APP_GRADLE="${1:-}"
VERSION_NAME="${2:-}"
VERSION_CODE="${3:-}"

if [[ -z "$APP_GRADLE" || -z "$VERSION_NAME" || -z "$VERSION_CODE" ]]; then
  echo "Usage: $(basename "$0") <app/build.gradle> <versionName> <versionCode>" >&2
  exit 2
fi

if [[ ! -f "$APP_GRADLE" ]]; then
  echo "Missing Gradle file: $APP_GRADLE" >&2
  exit 2
fi

if ! [[ "$VERSION_CODE" =~ ^[0-9]+$ ]]; then
  echo "versionCode must be numeric: $VERSION_CODE" >&2
  exit 2
fi

tmp_file="$(mktemp)"
sed \
  -e "s/^[[:space:]]*versionCode[[:space:]]*=[[:space:]]*[0-9][0-9]*/        versionCode = ${VERSION_CODE}/" \
  -e "s/^[[:space:]]*versionName[[:space:]]*=[[:space:]]*\"[^\"]*\"/        versionName = \"${VERSION_NAME}\"/" \
  "$APP_GRADLE" > "$tmp_file"
mv "$tmp_file" "$APP_GRADLE"
