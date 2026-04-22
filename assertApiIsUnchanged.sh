./gradlew metalavaGenerateSignatureRelease
if ! git diff --quiet; then
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
