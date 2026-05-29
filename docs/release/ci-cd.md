# Android CI/CD

The `main` release workflow is designed for a protected branch:

1. Calculate the next Android version from conventional commits since the latest `v*` tag.
2. Update `app/build.gradle` with the next `versionName` and `versionCode`.
3. Run unit tests and build signed release APK/AAB artifacts.
4. Commit the version bump back to `main` and create the matching git tag.
5. Create a GitHub Release with the APK and AAB attached.
6. Upload the AAB to Google Play when Play service-account secrets exist.
7. Upload the APK to Firebase App Distribution when Firebase distribution secrets exist.

## Version Bump Rules

- `BREAKING CHANGE:` or `type!:` -> major version.
- `feat:` -> minor version.
- `fix:`, `perf:`, `refactor:`, or `copy:` -> patch version.
- Other commit types do not create a release.

Every release increments `versionCode` by one.

## Required GitHub Secrets

- `GOOGLE_SERVICES_JSON`
- `MYSHARE_ADMOB_APP_ID`
- `MYSHARE_ADMOB_BANNER_AD_UNIT_ID`
- `MYSHARE_ADMOB_NATIVE_AD_UNIT_ID`
- `MYSHARE_ADMOB_INTERSTITIAL_AD_UNIT_ID`
- `MYSHARE_ADMOB_APP_OPEN_AD_UNIT_ID`
- `MYSHARE_ADMOB_REWARDED_AD_UNIT_ID`
- `MYSHARE_RELEASE_KEYSTORE_BASE64`
- `MYSHARE_RELEASE_STORE_PASSWORD`
- `MYSHARE_RELEASE_KEY_ALIAS`
- `MYSHARE_RELEASE_KEY_PASSWORD`

## Publishing Secrets

Google Play upload is skipped unless this secret exists:

- `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`

Firebase App Distribution upload is skipped unless both secrets exist:

- `FIREBASE_SERVICE_ACCOUNT_JSON`
- `FIREBASE_ANDROID_APP_ID`

Current Firebase App Distribution CI service account:

- `github-firebase-distribution@my-share-finance.iam.gserviceaccount.com`

## Optional GitHub Variables

- `GOOGLE_PLAY_TRACK`, default `internal`.
- `GOOGLE_PLAY_RELEASE_STATUS`, default `draft`.
- `FIREBASE_APP_DISTRIBUTION_GROUPS`, default `internal-testers`.

Keep `GOOGLE_PLAY_RELEASE_STATUS=draft` until the Play publishing flow is verified.

Current Google Play CI service account:

- `github-play-publisher@my-share-finance.iam.gserviceaccount.com`

Grant this account access in Play Console API access before expecting automated
Play uploads to work. Start with app-level access for `pt.ms.myshare` and keep
the release status as `draft` until the first CI upload is verified.

## Branch Protection Note

The release workflow commits the version bump back to `main` with `GITHUB_TOKEN`.
If `main` blocks GitHub Actions from pushing, either allow the workflow to bypass
that rule or replace `GITHUB_TOKEN` checkout/push with a narrowly scoped release
bot token.
