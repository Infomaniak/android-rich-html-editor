#!/usr/bin/env bash

set -euo pipefail

API_FILE="rich-html-editor/metalavaApi/api.txt"

if [ ! -f "$API_FILE" ]; then
  echo "API signature file '$API_FILE' is missing!"
  echo "Please run './gradlew metalavaGenerateSignatureRelease' to generate one and make sure you have correctly configured metalava in your gradle config"
  exit 1
fi

./gradlew metalavaGenerateSignatureRelease

if ! git diff --quiet -- "$API_FILE"; then
  echo ""
  echo "Metalava API signature has changed but api file was not updated."
  echo ""
  echo "Please run the following command and commit the result:"
  echo ""
  echo "    ./gradlew metalavaGenerateSignatureRelease"
  echo ""
  echo "Changed files:"
  git --no-pager diff --name-only
  exit 1
fi
