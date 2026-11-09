#!/bin/zsh
set -eu
analysis_profile=$(mktemp -d /private/tmp/rea-keep-chrome.XXXXXX)
printf '분석 전용 임시 프로필: %s\nCDP: http://127.0.0.1:9226\n' "$analysis_profile"
exec '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome' \
  --user-data-dir="$analysis_profile" \
  --remote-debugging-address=127.0.0.1 \
  --remote-debugging-port=9226 \
  --no-first-run --no-default-browser-check --new-window \
  'https://keep.google.com/'
