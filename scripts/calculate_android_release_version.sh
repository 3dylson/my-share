#!/usr/bin/env bash
set -euo pipefail

APP_GRADLE="${1:-app/build.gradle}"
TAG_PATTERN="${ANDROID_RELEASE_TAG_PATTERN:-v[0-9]*}"

if [[ ! -f "$APP_GRADLE" ]]; then
  echo "Missing Gradle file: $APP_GRADLE" >&2
  exit 2
fi

current_version_name="$(sed -n 's/^[[:space:]]*versionName[[:space:]]*=[[:space:]]*"\([^"]*\)".*/\1/p' "$APP_GRADLE" | head -1)"
current_version_code="$(sed -n 's/^[[:space:]]*versionCode[[:space:]]*=[[:space:]]*\([0-9][0-9]*\).*/\1/p' "$APP_GRADLE" | head -1)"

if [[ -z "$current_version_name" || -z "$current_version_code" ]]; then
  echo "Could not read versionName/versionCode from $APP_GRADLE" >&2
  exit 2
fi

last_tag="$(git describe --tags --match "$TAG_PATTERN" --abbrev=0 2>/dev/null || true)"
if [[ -n "$last_tag" ]]; then
  commit_range="${last_tag}..HEAD"
  base_version="${last_tag#v}"
else
  commit_range="HEAD"
  base_version="$current_version_name"
fi

commits="$(git log --format='%s%n%b%n---commit---' "$commit_range")"

bump="none"
if grep -Eq '(^|[[:space:]])BREAKING CHANGE:|^[a-zA-Z]+(\([^)]+\))?!:' <<< "$commits"; then
  bump="major"
elif grep -Eq '^feat(\([^)]+\))?:' <<< "$commits"; then
  bump="minor"
elif grep -Eq '^(fix|perf|refactor|copy)(\([^)]+\))?:' <<< "$commits"; then
  bump="patch"
fi

IFS='.' read -r major minor patch_extra <<< "$base_version"
patch="${patch_extra%%[-+]*}"
major="${major:-0}"
minor="${minor:-0}"
patch="${patch:-0}"

case "$bump" in
  major)
    major=$((major + 1))
    minor=0
    patch=0
    ;;
  minor)
    minor=$((minor + 1))
    patch=0
    ;;
  patch)
    patch=$((patch + 1))
    ;;
  none)
    ;;
  *)
    echo "Unsupported bump: $bump" >&2
    exit 2
    ;;
esac

next_version_name="${major}.${minor}.${patch}"
next_version_code="$current_version_code"
release_needed="false"

if [[ "$bump" != "none" ]]; then
  release_needed="true"
  if [[ "$next_version_name" == "$current_version_name" ]]; then
    next_version_code=$((current_version_code + 1))
  else
    next_version_code=$((current_version_code + 1))
  fi
fi

cat <<VERSION
release_needed=$release_needed
bump=$bump
last_tag=$last_tag
current_version_name=$current_version_name
current_version_code=$current_version_code
next_version_name=$next_version_name
next_version_code=$next_version_code
tag_name=v$next_version_name
VERSION
