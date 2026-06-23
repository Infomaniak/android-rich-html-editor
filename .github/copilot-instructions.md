# Copilot Coding Agent Onboarding — android-rich-html-editor

## Overview
Infomaniak Rich HTML Editor — an Android library for displaying and editing HTML inside a `WebView` using the `contenteditable` attribute. Published on **JitPack** (`com.github.infomaniak:android-rich-html-editor`). No Hilt, no Compose. Kotlin + XML.

## Build & Test (CI: `.github/workflows/android.yml`)
```bash
./gradlew build
./assertApiIsUnchanged.sh     # validates public API signature (Metalava)
./gradlew testDebugUnitTest --stacktrace
```

## ⚠️ Public API Changes (Metalava)
When modifying the public API of `rich-html-editor`, always regenerate the API signature file:
```bash
./gradlew metalavaGenerateSignatureRelease
```
This updates `rich-html-editor/metalavaApi/api.txt`. CI fails if the API changed but `api.txt` was not updated. Always commit the updated `api.txt`.

## Project Layout
```
rich-html-editor/
├── src/main/kotlin/com/infomaniak/lib/richhtmleditor/
│   └── RichHtmlEditorWebView.kt    # Main public class (extends WebView)
├── metalavaApi/api.txt             # Public API signature — keep in sync
└── src/main/assets/               # HTML/CSS/JS editor assets
sample/                            # Demo app
assertApiIsUnchanged.sh
```

## PR Review Instructions

- The editor's public API is tracked by Metalava — run `./gradlew metalavaGenerateSignatureRelease` and commit `api.txt` on every public API change.
- No Compose, no Hilt — keep the dependency footprint minimal. The library is consumed via JitPack.
- Maintain backward compatibility — breaking API changes affect all consuming apps.
- When adding/removing a runtime dependency, update `LICENSES.md` at the repo root.
