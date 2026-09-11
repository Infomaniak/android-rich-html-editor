# Copilot Coding Agent Onboarding — android-rich-html-editor

## Overview
Infomaniak Rich HTML Editor — an Android library for displaying and editing HTML inside a `WebView` using the `contenteditable` attribute. Published on **Reposilite** (`maven.infomaniak.app`) as `com.infomaniak.richhtmleditor:android-rich-html-editor`. No Hilt, no Compose. Kotlin + XML.

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
├── src/main/java/com/infomaniak/lib/richhtmleditor/
│   └── RichHtmlEditorWebView.kt    # Main public class (extends WebView)
├── metalavaApi/api.txt             # Public API signature — keep in sync
└── src/main/assets/               # HTML/CSS/JS editor assets
sample/                            # Demo app
assertApiIsUnchanged.sh
```

## PR Review Instructions

- Maintain backward compatibility — breaking API changes affect all consuming apps.
- When adding/removing a runtime dependency, update `LICENSES.md` at the repo root.

## Conventional Comments

> Use [Conventional Comments](https://conventionalcomments.org/) to format review feedback exactly like this:

```
**<label> [decorations]:** <subject>

[optional discussion]
```

The subject should not be more than one short line/sentence. If more information is required to understand the comment, put it into the discussion part.

Use these labels:

- `issue`: Issues highlight specific problems with the subject under review.
- `suggestion`: Suggestions propose improvements to the current subject. It’s important to be explicit and clear on what is being suggested and why it is an improvement.
- `todo`: TODOs are small, trivial, but necessary changes.
- `typo`: Typo comments are like todo comments, where the main issue is a misspelling.
- `quibble`: Use that one instead of `nitpick` for trivial preference- or style-based requests. These should be non-blocking by nature.
- `polish`: Polish comments are like a suggestion, where there is nothing necessarily wrong with the relevant content, there are just some ways to immediately improve the quality.
- `note`: Notes are always non-blocking and simply highlight something the reader should take note of.

You may use decorations after the label, but only if it really improves the value:

- `(blocking)` A comment with this decoration should prevent the subject under review from being accepted, until it is resolved.
- `(non-blocking)` A comment with this decoration should not prevent the subject under review from being accepted.
- `(if-minor)` This decoration gives some freedom to the author that they should resolve the comment only if the changes end up being minor or trivial.
