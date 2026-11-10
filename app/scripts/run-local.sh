#!/bin/bash
set -eu
set +x
cd "$(dirname "$0")/.."
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
# 이전 셸의 비밀번호도 빌드 프로세스에 넘기지 않습니다.
unset DB_PASSWORD
trap 'unset DB_PASSWORD' EXIT
./mvnw verify
if [[ ! -f target/keep-0.0.1-SNAPSHOT.jar ]]; then
  printf '빌드는 성공했지만 실행 JAR를 찾을 수 없습니다.\n' >&2
  exit 1
fi
export DB_USERNAME=rea_keep_app
IFS= read -r -s -p 'MySQL 비밀번호 (화면에 표시되지 않음): ' DB_PASSWORD
printf '\n'
export DB_PASSWORD
"$JAVA_HOME/bin/java" -jar target/keep-0.0.1-SNAPSHOT.jar
