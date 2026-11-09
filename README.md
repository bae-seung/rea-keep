# REA 메모 첫 구현

Google Keep의 카드 목록·편집창 구성을 참고한 Spring Boot 과제 앱입니다. 메모 목록, 빈 목록 안내, 작성, 기존 메모 열기·수정, DB 저장, 공백 검증, 실패 안내를 구현했습니다. 보관함·휴지통·라벨은 구현하지 않았습니다. 원본 UI와 동일하다고 주장하지 않습니다.

## 구성과 버전

- 앱: `app/` — Java 21, Spring Boot **4.0.9**, Maven **3.10.0**.
- Spring Web MVC, Thymeleaf, Spring Data JPA, Validation, MySQL Connector/J: Boot BOM으로 버전 관리.
- `NoteController → NoteService → NoteRepository → MySQL` 구조.
- `evidence/keep-archive/`는 기존 분석 자료이며 변경하지 않았습니다.
- [개발·검증 기록](docs/development-log.md), [REA 관찰 근거](evidence/keep-archive/README.ko.md).

[공식 시스템 요구 사항](https://docs.spring.io/spring-boot/4.0/system-requirements.html)에서 4.0.9 안정판과 Java 17–27, Maven 3.6.3 이상 지원을 확인했습니다. Java 21이 이 범위에 포함됩니다. [공식 의존성 관리표](https://docs.spring.io/spring-boot/4.0/appendix/dependency-versions/coordinates.html)에 따라 의존성 버전을 개별 덮어쓰지 않습니다. 최신판이라는 의미는 아닙니다.

## 1. 빌드 및 자동 테스트

일반 터미널에서 프로젝트 루트로 이동합니다.

```bash
cd /Users/baeseungjun/Projects/rea-assignment/app
export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
./mvnw verify
```

Maven 설치는 필요 없습니다. 첫 실행은 Maven 및 의존성을 받으므로 인터넷 연결이 필요합니다. `mvnw`, `mvnw.cmd`, Wrapper JAR와 설정을 포함했습니다. 테스트는 테스트 전용 H2 인메모리 DB를 사용하며 MySQL 비밀번호가 필요 없습니다. `create-drop`은 테스트 리소스에만 있습니다.

## 2. 기존 DB를 먼저 확인

앱은 스키마를 자동 생성·변경하지 않습니다. DB `rea_keep` 및 계정 `rea_keep_app`은 이미 있다는 전제입니다. 다음은 **사용자의 일반 터미널에서** 실행합니다. `-p` 뒤에 비밀번호를 붙이지 말고 숨김 프롬프트에만 입력하세요.

```bash
cd /Users/baeseungjun/Projects/rea-assignment
mysql --protocol=TCP -h 127.0.0.1 -P 3306 -u rea_keep_app -p rea_keep
```

MySQL 프롬프트에서 먼저 읽기 전용으로 확인합니다.

```sql
SELECT DATABASE(), VERSION();
SHOW FULL TABLES;
SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, IS_NULLABLE, COLUMN_KEY
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'rea_keep'
ORDER BY TABLE_NAME, ORDINAL_POSITION;
```

기존 테이블은 보존합니다. **`rea_app_notes`가 없을 때만** 아래 파일로 새 테이블 하나를 생성합니다.

```sql
SOURCE app/db/001-create-notes.sql;
SHOW CREATE TABLE rea_app_notes;
EXIT;
```

`rea_app_notes`가 이미 있다면 SQL을 재실행하지 말고 `SHOW CREATE TABLE rea_app_notes;` 결과와 `app/db/001-create-notes.sql`을 비교하세요. 다른 용도의 동명 테이블 또는 스키마 불일치가 있으면 앱을 시작하지 말고 별도의 비파괴 변경 계획을 세워야 합니다. 파일은 충돌 시 실패하도록 단순 `CREATE TABLE`만 사용하며, 삭제·초기화·ALTER 명령은 없습니다. CREATE 권한이 없으면 DB 관리자가 이 SQL을 적용해야 합니다.

운영 설정은 `spring.jpa.hibernate.ddl-auto=validate`, `spring.sql.init.mode=never`입니다. 정상 테이블이 있으면 재시작해도 데이터를 유지하고, 스키마가 없거나 맞지 않으면 시작을 거부합니다. 이후 버전의 스키마 변경도 별도 SQL로 검토·적용해야 합니다.

## 3. 숨김 입력으로 앱 실행

```bash
cd /Users/baeseungjun/Projects/rea-assignment
./app/scripts/run-local.sh
```

스크립트는 `DB_USERNAME=rea_keep_app`을 지정하고, `read -s`로 비밀번호를 숨겨 입력받아 `DB_PASSWORD` 환경변수로 Java 프로세스에 전달합니다. 입력값을 파일이나 로그에 기록하지 않으며 종료 시 셸 변수도 지웁니다. 비밀번호를 채팅, 명령 인자, `.env`, 설정 파일, Git에 붙여 넣지 마세요. `set -x`, `env`, 환경변수 덤프나 디버그 로그로 인증정보를 출력하지 마세요.

접속 URL은 `jdbc:mysql://127.0.0.1:3306/rea_keep`입니다. 브라우저에서 **http://127.0.0.1:8080/** 를 엽니다. 이 앱은 인증 없는 로컬 과제 앱이므로 서버를 `127.0.0.1`에만 바인딩합니다.

## 4. 실제 MySQL·브라우저 확인 절차

**사용자 수동 검증 완료:** 사용자는 실제 MySQL **8.0.44** 환경의 브라우저에서 **메모 작성 → 수정 → 새로고침 → 앱 종료·재실행 후 내용 유지**를 확인했고, 해당 과정에 오류가 없었다고 보고했습니다. 이는 사용자 보고에 따른 결과이며 Codex가 직접 MySQL이나 브라우저를 실행해 검증한 결과가 아닙니다.

Codex가 직접 실행한 검증은 H2·MockMvc 기반 자동 테스트 9개와 빌드입니다. 빈 목록 안내, 양쪽 공백 거부, 제목만/본문만 저장, 길이 제한, 저장 실패 안내의 **실제 MySQL·브라우저 수동 검증은 미확인**으로 유지합니다. 기존 테이블 사전 점검 및 스키마 생성 절차의 수행 여부도 이번 보고만으로 확인하지 않습니다.

아래는 재확인 절차입니다. 2–6번의 작성·수정·내용 유지 흐름은 사용자 보고로 확인됐으며, 예시 제목·본문 자체를 사용했다고 단정하지 않습니다.

1. 목록을 확인합니다. 비어 있으면 빈 목록 안내가 보여야 합니다.
2. 새 메모에서 제목 `첫 구현 확인`, 본문 `저장 확인`을 입력하고 저장합니다.
3. 카드를 열어 본문을 `수정 후 유지 확인`으로 바꿔 저장합니다.
4. 브라우저를 새로고침하고 제목·본문이 유지되는지 확인합니다.
5. 터미널에서 `Ctrl+C`로 앱을 종료하고 `./app/scripts/run-local.sh`를 다시 실행합니다.
6. 비밀번호를 다시 숨김 입력하고 같은 메모의 수정 내용이 유지되는지 확인합니다.
7. 제목·본문 모두 공백이면 저장을 거부하는지, 제목만 또는 본문만 있어도 저장 가능한지 확인합니다.

시각은 서버의 UTC로 관리하고 화면에도 UTC로 표시합니다. 제목 200자, 본문 10,000자까지 지원합니다. 명시적인 저장 버튼을 사용하며 취소는 변경 내용을 저장하지 않습니다. 오류가 나면 입력을 보관하고 목록에서 저장 여부를 확인하세요. 통신 단절 시 서버 커밋 여부가 모호할 수 있어 신규 메모를 바로 반복 저장하면 중복될 수 있습니다. 다중 사용자 인증·동시 편집 충돌 해결·중복 요청 멱등성은 이번 범위에 포함하지 않았습니다.

## 제출 자료 구분

- 제출 대상: 앱 소스, Maven Wrapper, SQL, README, 개발 기록 및 기존 정제 증거(잘라낸 이미지·sanitized JSON·해시·설명).
- 제외 대상: `.rea-setup-backup/` 설정 백업, 인증정보, 개인 프로필·쿠키, 빌드 결과, 원시 개인 데이터. `.gitignore`에 반영했습니다.
- `evidence/` 전체를 무조건 제외하지 않았습니다. 기존 정제 자료는 발췌 기록이며 원시 Evidence 전체가 아닙니다. 현재 정제 증거 11개만 `.gitignore` 예외 목록으로 허용합니다. 새 증거는 개인정보를 검토한 뒤 예외 목록에 추가하세요.
