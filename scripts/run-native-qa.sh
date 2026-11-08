#!/usr/bin/env bash
set +e
gradle --no-daemon :app:testDebugUnitTest :app:connectedDebugAndroidTest
qa_result=$?
mkdir -p native-qa
for name in qa-reader.png qa-landscape.png qa-summary.png; do
  adb exec-out run-as com.pharmnote.study.debug cat "files/$name" > "native-qa/$name"
done
exit "$qa_result"
