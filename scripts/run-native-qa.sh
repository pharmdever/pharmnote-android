#!/usr/bin/env bash
set +e
gradle --no-daemon :app:testDebugUnitTest :app:connectedDebugAndroidTest
qa_result=$?
mkdir -p native-qa
for name in qa-reader.png qa-landscape.png qa-summary.png qa-summary-cards.png qa-summary-contents.png qa-summary-jump.png qa-summary-menu.png qa-ai-tools.png; do
  adb exec-out cat "/sdcard/Pictures/Pharmnote-QA/$name" > "native-qa/$name"
done
exit "$qa_result"
