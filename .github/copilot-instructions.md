# Copilot Coding Agent Onboarding — android-rich-html-editor

## Overview
Infomaniak Rich HTML Editor — an Android library for displaying and editing HTML inside a `WebView` using the `contenteditable` attribute. Published on **JitPack** (`com.github.infomaniak:android-rich-html-editor`). No Hilt, no Compose. Kotlin + XML.

## Build
```bash
./gradlew build               # builds library + sample app
./gradlew :rich-html-editor:assembleRelease
```
JitPack uses OpenJDK 17 (see `jitpack.yml`). No env file, no submodules.

## Tests & Lint (CI: `.github/workflows/android.yml`)
```bash
./gradlew build
./assertApiIsUnchanged.sh     # validates public API signature (Metalava)
./gradlew testDebugUnitTest --stacktrace
```
CI runs all three on every non-draft PR.

## ⚠️ Public API Changes (Metalava)
When modifying the public API of the `rich-html-editor` module, **always regenerate the API signature file**:
```bash
./gradlew metalavaGenerateSignatureRelease
```
This updates `rich-html-editor/metalavaApi/api.txt`. CI will fail via `assertApiIsUnchanged.sh` if the API changed but `api.txt` was not updated. Always commit the updated `api.txt`.

## Project Layout
```
rich-html-editor/
├── src/main/kotlin/com/infomaniak/lib/richhtmleditor/   # Library source
│   ├── RichHtmlEditorWebView.kt    # Main public class (extends WebView)
│   └── executor/                  # JS command execution
├── metalavaApi/api.txt             # Public API signature — must stay in sync
└── src/main/assets/               # HTML/CSS/JS editor assets
sample/                            # Demo app
assertApiIsUnchanged.sh            # Run after any public API change
gradle/libs.versions.toml
```

## Key Rules
- The editor's public API is tracked by Metalava — update `api.txt` on every public API change.
- The editor is consumed via JitPack — maintain backward compatibility.
- No Compose, no Hilt — keep the dependency footprint minimal.
- When adding/removing a runtime dependency, update `LICENSES.md` at the repo root.
