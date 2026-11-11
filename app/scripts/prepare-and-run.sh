#!/bin/bash
set -eu
set +x
cd "$(dirname "$0")/.."
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
unset DB_PASSWORD
if nc -z 127.0.0.1 8080 2>/dev/null; then
  printf '8080 포트가 사용 중입니다. 기존 과제 앱을 종료한 뒤 다시 실행하세요.\n' >&2
  exit 1
fi
./mvnw verify
export DB_USERNAME=rea_keep_app
IFS= read -r -s -p 'MySQL 비밀번호 (숨김 입력, 저장하지 않음): ' DB_PASSWORD
printf '\n'
export DB_PASSWORD
app_pid=''
cleanup() {
  if [[ -n "$app_pid" ]]; then kill "$app_pid" 2>/dev/null || true; wait "$app_pid" 2>/dev/null || true; fi
  unset DB_PASSWORD
}
trap cleanup EXIT
trap 'exit 130' INT TERM
"$JAVA_HOME/bin/java" -jar target/keep-0.0.1-SNAPSHOT.jar --prepare-db
rm -f target/restart.request
"$JAVA_HOME/bin/java" -jar target/keep-0.0.1-SNAPSHOT.jar &
app_pid=$!
profile=$(mktemp -d /private/tmp/rea-app-browser.XXXXXX)
env -u DB_PASSWORD '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome' --user-data-dir="$profile" --remote-debugging-address=127.0.0.1 --remote-debugging-port=9227 --no-first-run --no-default-browser-check 'http://127.0.0.1:8080/' >/dev/null 2>&1 &
printf '검증용 서버와 Chrome 시작. 종료: Ctrl+C. 재시작 요청: app/target/restart.request 생성.\n'
while kill -0 "$app_pid" 2>/dev/null; do
  if [[ -f target/restart.request ]]; then
    rm target/restart.request
    kill "$app_pid"
    wait "$app_pid" || true
    "$JAVA_HOME/bin/java" -jar target/keep-0.0.1-SNAPSHOT.jar &
    app_pid=$!
    printf '검증용 서버를 재시작했습니다.\n'
  fi
  sleep 1
done
wait "$app_pid"
