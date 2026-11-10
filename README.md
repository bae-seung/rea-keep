# REA 메모

Google Keep의 카드 목록·편집창 구성을 참고한 Spring Boot 과제 앱입니다. 메모 목록, 빈 목록 안내, 작성, 기존 메모 열기·수정, DB 저장, 공백 검증, 실패 안내를 구현했습니다. 보관함과 보관·보관 해제를 추가했습니다. 휴지통·라벨은 구현하지 않았습니다. 원본 UI와 동일하다고 주장하지 않습니다.

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
SOURCE app/db/002-add-archived.sql;
SHOW CREATE TABLE rea_app_notes;
EXIT;
```

`rea_app_notes`가 이미 있다면 SQL을 재실행하지 말고 `SHOW CREATE TABLE rea_app_notes;` 결과와 `app/db/001-create-notes.sql`을 비교하세요. 다른 용도의 동명 테이블 또는 스키마 불일치가 있으면 앱을 시작하지 말고 별도의 비파괴 변경 계획을 세워야 합니다. `001`은 신규 테이블 생성 전용이고, `002`는 기존 행을 보존하면서 `archived` 열만 추가합니다. 기존 테이블에는 `001`을 재실행하지 않습니다. CREATE/ALTER 권한이 없으면 DB 관리자가 해당 SQL을 적용해야 합니다.

운영 설정은 `spring.jpa.hibernate.ddl-auto=validate`, `spring.sql.init.mode=never`입니다. 정상 테이블이 있으면 재시작해도 데이터를 유지하고, 스키마가 없거나 맞지 않으면 시작을 거부합니다. 이후 버전의 스키마 변경도 별도 SQL로 검토·적용해야 합니다.

## 3. 숨김 입력으로 앱 실행

```bash
cd /Users/baeseungjun/Projects/rea-assignment
./app/scripts/run-local.sh
```

스크립트는 먼저 Maven Wrapper로 `verify`를 실행합니다. 빌드·테스트가 실패하면 비밀번호 입력이나 앱 실행 없이 종료합니다. 성공한 경우에만 `DB_USERNAME=rea_keep_app`을 지정하고, `read -s`로 비밀번호를 숨겨 입력받아 `DB_PASSWORD` 환경변수로 Java 프로세스에 전달합니다. 입력값을 파일이나 로그에 기록하지 않으며 종료 시 셸 변수도 지웁니다. 비밀번호를 채팅, 명령 인자, `.env`, 설정 파일, Git에 붙여 넣지 마세요. `set -x`, `env`, 환경변수 덤프나 디버그 로그로 인증정보를 출력하지 마세요.

접속 URL은 `jdbc:mysql://127.0.0.1:3306/rea_keep`입니다. 브라우저에서 **http://127.0.0.1:8080/** 를 엽니다. 이 앱은 인증 없는 로컬 과제 앱이므로 서버를 `127.0.0.1`에만 바인딩합니다.

## 4. 실제 MySQL·브라우저 확인 절차

**사용자 수동 검증 완료:** 사용자는 실제 MySQL **8.0.44** 환경의 브라우저에서 **메모 작성 → 수정 → 새로고침 → 앱 종료·재실행 후 내용 유지**를 확인했고, 해당 과정에 오류가 없었다고 보고했습니다. 이는 사용자 보고에 따른 결과이며 Codex가 직접 MySQL이나 브라우저를 실행해 검증한 결과가 아닙니다.

첫 구현에서 Codex가 직접 실행한 검증은 H2·MockMvc 기반 자동 테스트 9개와 빌드입니다. 보관 기능 추가 후에는 마이그레이션 검증을 포함한 총 15개 테스트와 빌드가 통과했습니다. 빈 목록 안내, 양쪽 공백 거부, 제목만/본문만 저장, 길이 제한, 저장 실패 안내의 **실제 MySQL·브라우저 수동 검증은 미확인**으로 유지합니다. 기존 테이블 사전 점검 및 스키마 생성 절차의 수행 여부도 이번 보고만으로 확인하지 않습니다.

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
- `evidence/` 전체를 무조건 제외하지 않았습니다. 기존 정제 자료는 발췌 기록이며 원시 Evidence 전체가 아닙니다. 기존 정제 증거 11개와 새 `keep-archive-list/`의 정제 기록만 `.gitignore` 예외 목록으로 허용합니다. 새 증거는 개인정보를 검토한 뒤 예외 목록에 추가하세요.


## 5. 보관 기능 업데이트 — 기존 사용자 적용 순서

전체 메모(`/`)에는 보관되지 않은 메모만, 보관함(`/archive`)에는 보관한 메모만 표시합니다. 카드의 **보관** 또는 **보관 해제** 버튼으로 이동하며, 제목·본문·생성 시각·수정 시각은 유지합니다. 보관한 메모도 열어 수정할 수 있으며 저장 후 보관함으로 돌아갑니다. 버튼은 편집창이 아닌 목록 카드에 있습니다.

`archived` boolean과 MySQL `BIT(1)` 열은 **우리 앱의 설계 선택**입니다. Google의 DB 구조를 알아낸 것이 아닙니다. 이번 REA 연결은 성공했지만 Google Keep이 연결 불가 안내와 비활성 보관 버튼을 표시해 원본의 전체 목록↔보관함 이동은 **미확인**입니다. [새 관찰 기록](evidence/keep-archive-list/README.ko.md)을 참고하세요.

### 기존 DB에 변경 SQL 적용

1. 실행 중인 앱을 `Ctrl+C`로 종료합니다.
2. 일반 터미널에서 프로젝트 루트로 이동한 다음 MySQL에 숨김 비밀번호로 접속합니다.

```bash
cd /Users/baeseungjun/Projects/rea-assignment
mysql --protocol=TCP -h 127.0.0.1 -P 3306 -u rea_keep_app -p rea_keep
```

3. 먼저 현재 구조와 행 수를 확인합니다.

```sql
SHOW CREATE TABLE rea_app_notes;
SHOW COLUMNS FROM rea_app_notes LIKE 'archived';
SELECT COUNT(*) AS note_count_before FROM rea_app_notes;
```

4. `archived` 열이 **없을 때만 한 번** 다음 변경 SQL을 실행합니다. 이미 있다면 재적용하지 말고 `BIT(1) NOT NULL DEFAULT 0`과 일치하는지 확인합니다. 예상과 다르면 중단하고 구조를 먼저 검토하세요.

```sql
SOURCE app/db/002-add-archived.sql;
SHOW CREATE TABLE rea_app_notes;
SELECT COUNT(*) AS note_count_after,
       SUM(CASE WHEN archived = 0 THEN 1 ELSE 0 END) AS unarchived_count
FROM rea_app_notes;
EXIT;
```

변경 SQL은 기존 행의 `archived`를 `0`으로 만들며 제목·본문·시각을 수정하거나 테이블을 삭제하지 않습니다. 최초 적용 직후 기존 메모는 전부 전체 목록에 남아야 합니다. MySQL DDL은 자동 커밋되므로 트랜잭션 롤백으로 취소된다고 가정하지 마세요. 새 DB는 `001` → `002` 순서로 적용합니다. 기존 DB는 **`002`만** 적용합니다. 앱은 여전히 스키마 자동 변경 없이 `validate`만 수행합니다.

### 반드시 새 JAR 빌드 후 재실행

실행 스크립트가 매번 `./mvnw verify`를 실행합니다. 기존 앱 종료와 위 스키마 확인·적용을 마친 뒤 다음 명령으로 빌드와 실행을 진행하세요.

```bash
cd /Users/baeseungjun/Projects/rea-assignment/app
./scripts/run-local.sh
```

스크립트가 Java 21을 선택하고 `verify` 성공과 실행 JAR 존재를 확인한 뒤 DB 비밀번호를 숨겨 입력받습니다. JAR 수정 시각은 검사하지 않습니다. 이 프로젝트에서 `clean verify` 성공 후에도 JAR 시각이 1980-02-01로 설정되는 현상을 재현했으므로 시각을 빌드 성공 여부의 기준으로 삼지 않습니다. 빌드 실패 시 기존 JAR가 있어도 실행하지 않습니다. 비밀번호는 빌드 후에만 입력받고 파일·로그에 저장하지 않습니다. `clean verify`로 별도 빌드했더라도 스크립트는 다시 `verify`를 수행합니다.

### 보관 기능 — 사용자 수동 검증 완료

2026-11-10 사용자 보고에 따라, 우리 앱의 실제 MySQL·브라우저 환경에서 다음 세 항목을 **사용자 수동 검증 완료**로 기록합니다.

- 보관 시 전체 목록에서 제외되고 보관함에 표시됨.
- 서버 재시작 후 보관 상태가 유지됨.
- 보관 해제 시 전체 목록으로 복귀하며 제목·본문이 유지됨.

이는 사용자가 실행한 검증이며 Codex가 직접 MySQL·브라우저에서 재검증한 결과가 아닙니다. **원본 Google Keep의 전체 목록↔보관함 이동은 여전히 미확인**입니다. 생성/수정 시각 보존, 보관 해제 후 재시작, 실패 안내 등 이번 보고에 없는 수동 검증까지 완료로 확장하지 않습니다.

아래는 추가 확인·재현 절차이며 모든 단계를 사용자가 수행했다는 뜻은 아닙니다.

1. 기존 메모의 제목·본문·생성 시각을 확인하고 전체 목록에서 보관 버튼을 누릅니다.
2. 전체 목록에서 사라지고 보관함에 나타나는지 확인합니다.
3. 제목·본문·생성 시각이 유지되는지 확인합니다. 필요하면 MySQL에서 해당 테스트 메모 ID만 지정해 조회하세요.
4. 새로고침 및 앱 종료·재실행 후에도 보관함에 남는지 확인합니다.
5. 보관 해제를 누르고 보관함에서 사라져 전체 목록에 돌아오는지 확인합니다.
6. 다시 새로고침·재실행해 내용과 보관 해제 상태가 유지되는지 확인합니다.

첫 구현의 작성·수정·재시작 지속성 검증 기록에 더해, 위 보관 기능 세 항목도 사용자 수동 검증 완료로 갱신했습니다. 스키마 변경 SQL의 실행 과정·기존 행 수 비교 등은 별도 보고가 없어 미확인으로 유지합니다.
