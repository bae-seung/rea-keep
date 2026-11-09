#!/bin/bash
set -eu
set +x
cd "$(dirname "$0")/.."
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
export DB_USERNAME=rea_keep_app
if [[ ! -f target/keep-0.0.1-SNAPSHOT.jar ]]; then
  printf '먼저 app/에서 ./mvnw verify를 실행해 주세요.\n' >&2
  exit 1
fi
trap 'unset DB_PASSWORD' EXIT
IFS= read -r -s -p 'MySQL 비밀번호 (화면에 표시되지 않음): ' DB_PASSWORD
printf '\n'
export DB_PASSWORD
"$JAVA_HOME/bin/java" -jar target/keep-0.0.1-SNAPSHOT.jar
