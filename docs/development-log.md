# 개발 기록 — 첫 구현

## 최신 제출 준비 상태 — 2026-11-11

사용자가 검증된 버전의 제출 확정·커밋·origin/main 푸시를 요청했다. 새 기능이나 앱 코드는 변경하지 않고 docs/submission-summary.md에 확인 가능한 환경/도구/모델, 구현 화면, REA 과정과 증거, 관찰/설계/미확인 구분, 26개 자동 테스트와 69개 DOM 확인의 범위, 오류 해결, 제한, 발표 캡처 경로를 정리했다. README에서 연결했다.

Git 후보 67개 파일과 추적 diff를 점검했다. 알려진 비밀값 패턴에서 확인된 실제 비밀번호·키·토큰은 없었다. DatabaseSetup의 탐지 후보는 환경변수 읽기와 null 검사로 확인했다. 개인 메모 데이터·원시 브라우저 자료는 후보에 없고 실제 제외 경로는 .rea-setup-backup/ 및 app/target/이다. 이전 시각 검수된 PNG는 해시 검증으로 무변경을 확인했다. 4개 evidence 폴더의 SHA-256 대상 19개 모두 일치, 정제 기록의 명시적 DOM 확인 69개 모두 일치, git diff --check 통과. 패턴 검사는 모든 비밀값 부재의 절대 보장은 아니다.

main 브랜치, origin https://github.com/bae-seung/rea-keep.git, 기존 HEAD 1ef15e141e2b04b1b319ef61f6d54da6822055e7을 확인했다. git add 실행은 .git/index.lock 생성 권한 제한(Operation not permitted)으로 실패했다. 현재 환경의 .git 쓰기 금지를 우회하지 않았으며 스테이징·새 커밋·푸시는 완료하지 못했다. 기존 HEAD를 최신 제출 버전인 것처럼 푸시하지 않았다. 일반 터미널에서 최종 커밋·푸시가 필요하다.

## 2026-11-09 변경 내용

기존 파일 목록, `evidence/keep-archive/README.ko.md`, 정제 관찰·호출 기록, 과거 연결 실패 기록과 실행 스크립트를 확인했습니다. 설정 백업은 인증정보가 포함될 수 있어 내용을 출력하지 않았으며 수정하지 않았습니다. 기존 앱이나 AGENTS.md는 발견되지 않았습니다.

`app/`에 Spring Boot 앱, Maven Wrapper, Thymeleaf 템플릿, CSS·JavaScript, SQL, 숨김 입력 실행 스크립트, 자동 테스트를 추가했습니다. 루트 README와 `.gitignore`를 작성했습니다. 기존 evidence는 보존했습니다.

## 공식 버전 근거

2026-11-09 확인:

- Spring Boot **4.0.9** 안정판 선택. [공식 요구 사항](https://docs.spring.io/spring-boot/4.0/system-requirements.html)은 Java 17–27, Maven 3.6.3 이상을 지원합니다. Java 21 사용.
- [공식 관리 의존성 표](https://docs.spring.io/spring-boot/4.0/appendix/dependency-versions/coordinates.html)에 따라 Boot BOM 사용. Spring Web은 4.x의 `spring-boot-starter-webmvc` 사용.
- 실제 빌드 JAR 확인: Spring Web MVC **7.0.10**, Spring Data JPA **4.0.8**, Thymeleaf **3.1.5.RELEASE**, Hibernate Validator **9.0.2.Final**, MySQL Connector/J **9.7.0**.
- [MySQL 공식 호환성 문서](https://docs.oracle.com/cd/E17952_01/connector-j-en/connector-j-versions.html): Connector/J 9.7은 MySQL 8.0 이상 및 JRE 8 이상 지원. 실제 MySQL 8.0.44 연결은 별도 미확인.
- Maven **3.10.0** 배포 URL 고정. 네트워크 제한 때문에 로컬 Maven archetype 캐시(`am.ik.archetype:elm-spring-boot-blank-archetype:0.0.3`)에 포함된 기존 Maven Wrapper를 추출했습니다. Wrapper JAR 메타데이터는 `io.takari:maven-wrapper:0.4.2`입니다. 스크립트의 라이선스 문구를 보존했고 이 Wrapper로 실제 빌드했습니다. 최신 Apache Wrapper를 생성했다고 주장하지 않습니다. 신규 다운로드 경로 자체는 이번 환경에서 검증하지 못했습니다.

## 실제 관찰과 이번 설계 선택

| 구분 | 근거 및 내용 |
|---|---|
| 기존 REA 실제 관찰 | 테스트 메모에서 보관·보관 해제 UI가 전환됨. 같은 URL 재로드 후 제목·본문·보관 해제 상태 유지. 기존 REA 6.8.0 결과를 근거로 삼았으며 이번에 새 분석을 수행하지 않음 |
| 원본 미확인 | 서버 API, DB 테이블·필드, 서버 저장·동기화 내부 처리, 완전 종료 후 유지, 전체 UI 동일성 |
| 이번 설계 | Spring MVC 폼 POST, 명시적 저장, 서비스 트랜잭션, MySQL `rea_app_notes`, 숫자 ID, 제목·본문·생성/수정 시각, UTC 시각, 수정 시각 내림차순 카드 목록 |
| 이번 범위 밖 | 보관함·휴지통·라벨, 인증, 다중 사용자·동시 편집 충돌 해결, 자동 저장, 중복 요청 멱등성 |

설계는 원본 서버의 복원 결과가 아닙니다. 제목 200자·본문 10,000자 제한과 카드/독립 편집창의 화면 구성도 과제 구현의 선택입니다. 원본과 나란히 비교한 시각 검증을 하지 않았습니다.

## 저장·오류 처리

- `NoteController → NoteService → NoteRepository` 계층 분리.
- 클라이언트가 ID·시각을 폼 필드로 덮어쓰지 못하도록 제목·본문 DTO만 바인딩. 대상 ID는 URL에서 받음.
- 생성 시각은 서버에서 생성하고 수정에서 보존. 수정 시각은 서버 Clock으로 갱신. DATETIME(6) 정밀도에 맞춰 마이크로초 단위 UTC 사용.
- 서버 Bean Validation으로 길이 제한 및 양쪽 공백 거부. 일반 공백·탭·개행·Unicode 공백 검사. 서비스에서도 검증해 컨트롤러 밖 호출도 거부.
- `saveAndFlush` 및 서비스 트랜잭션 완료 후에만 성공 flash와 목록 리다이렉트. 실패 시 503 + 입력 보존 + 오류 안내. 존재하지 않는 메모는 404, 검증 오류는 400.
- 실패 화면에 예외 원문·DB 접속 세부정보를 출력하지 않음. 목록 조회 실패도 빈 목록 성공 화면으로 처리하지 않음.
- Thymeleaf 텍스트 이스케이프 사용. 저장 버튼 중복 클릭은 JavaScript로 방지하나 서버 멱등성을 보장하지는 않음.

## DB 보존 및 인증정보

실제 MySQL에는 이번 작업에서 연결하거나 DDL을 실행하지 않았습니다. 기존 테이블 존재·구조는 **미확인**입니다. 사용자가 이미 TCP 접속 및 DB 선택을 확인했다는 정보와 이번 앱 연결 검증은 구분합니다.

`ddl-auto=validate`, SQL 자동 실행 꺼짐. README에서 `SHOW FULL TABLES` 및 information_schema 조회를 먼저 수행하도록 안내했습니다. 기존 테이블은 건드리지 않고 `rea_app_notes`가 없을 때만 수동 SQL로 생성합니다. 동명 테이블에는 자동 ALTER나 덮어쓰기를 하지 않습니다. 재실행 시 SQL을 다시 적용하지 않고 스키마 검증만 합니다.

비밀번호를 요청·조회·저장하지 않았습니다. 환경변수 `DB_USERNAME`, `DB_PASSWORD`만 사용합니다. 실행 스크립트는 일반 터미널에서 숨김 입력받으며 파일 기록·명령행 인자를 사용하지 않습니다.

## 실행 결과

환경: macOS Apple Silicon, `/usr/libexec/java_home -v 21`로 Temurin **21.0.9** 확인.

1. Maven Central 셸 연결 시도: `curl: (6) Could not resolve host: repo.maven.apache.org`. 다운로드 성공으로 기록하지 않음.
2. 로컬 캐시를 쓰기 가능한 `/private/tmp/rea-m2`로 복사해 오프라인 빌드. 최초 테스트에서 Mockito inline mock maker의 JVM self-attach가 실패. 일반 클래스 모킹만 필요하므로 테스트에 `mock-maker-subclass`를 설정해 해결.
3. 최종 실행 명령 (`app/`에서):

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) \
MAVEN_USER_HOME=/private/tmp/rea-m2 \
MAVEN_OPTS=-Dmaven.user.home=/private/tmp/rea-m2 \
./mvnw -o -Dmaven.repo.local=/private/tmp/rea-m2/repository verify
```

결과: **BUILD SUCCESS**, 2026-11-09 22:30 KST, 18.422초. **9 tests, 0 failures, 0 errors, 0 skipped**. 실행 가능한 `app/target/keep-0.0.1-SNAPSHOT.jar` 생성. 테스트 상세 결과는 `app/target/surefire-reports/`에 생성되며 빌드 결과이므로 제출 제외.

| 검증 | 결과 |
|---|---|
| Java 21 컴파일·JAR 패키징·Wrapper 실행 | 통과 |
| H2 저장·새 트랜잭션 조회·수정·생성 시각 보존 | 통과 |
| 공백 작성·수정 거부, 기존 내용 보존 | 통과 |
| 본문만 저장, 길이 경계 및 초과 거부 | 통과 |
| MockMvc 빈 목록·작성·수정·재조회·404·HTML 이스케이프 | 통과 |
| 모의 저장·수정 실패 시 503/입력 보존/성공 안내 없음 | 통과 |
| 모의 목록 실패 시 오류 화면 | 통과 |
| 숨김 입력 스크립트 및 mvnw 셸 문법 검사 | 통과 |
| 기존 SHA256SUMS.txt의 10개 파일 해시 | 모두 일치 |
| 실제 MySQL 연결·스키마 생성·저장 | **미확인** |
| 실제 브라우저 렌더링·클릭·JavaScript 실행 | **미확인** (MockMvc는 브라우저가 아님) |
| MySQL 앱 종료·재시작 후 데이터 유지 | **미확인** |
| 원본 UI와 동일성 | **미확인** |

H2 검증은 MySQL 드라이버 연결·DDL 호환성 또는 프로세스 재시작 지속성 검증을 대체하지 않습니다. README의 일반 터미널 실행 및 브라우저 수동 확인 절차를 다음 단계로 남겼습니다.

## 증거 제출

기존 정제 evidence와 해시는 제출 가능 대상으로 유지했습니다. 초기 개인 메모·계정 정보가 담길 수 있는 원시 관찰, 인증정보, `.rea-setup-backup/` 설정 백업은 제출 제외입니다. `.gitignore`는 evidence 전체를 제외하지 않습니다. 첫 구현 당시에는 Git 저장소로 초기화되어 있지 않았으며 커밋을 생성하지 않았습니다.

## 2026-11-09 22:34 KST 후속 재검증

사용자의 계속 진행 요청에 따라 기존 앱·서비스·테스트·DB 설정·숨김 입력 스크립트를 재확인하고, 위 오프라인 Wrapper 명령의 목표를 `clean verify`로 바꾸어 실행했습니다. 기존 빌드 결과를 제거한 뒤 Java 21로 다시 컴파일·테스트·패키징했습니다.

- 결과: **BUILD SUCCESS**, 종료 코드 0, 17.898초.
- 테스트: **9개 통과, 실패 0, 오류 0, 건너뜀 0**.
- 실행 JAR: `app/target/keep-0.0.1-SNAPSHOT.jar` 재생성.
- 앱 기능 변경 없이 현재 구현의 재현 가능한 빌드를 확인했습니다.
- 실제 MySQL 접속·기존 테이블 조회·브라우저·재시작 지속성 검증은 여전히 **미확인**입니다. 비밀번호는 받거나 조회하지 않았습니다. 사용자는 README의 스키마 사전 확인 후 `./app/scripts/run-local.sh`로 숨김 입력하여 검증할 수 있습니다.


## 2026-11-09 사용자 수동 검증 보고 및 첫 업로드 준비

### 사용자 수동 검증 완료

사용자는 실제 **MySQL 8.0.44** 환경에서 브라우저로 **메모 작성 → 수정 → 새로고침 → 앱 종료·재실행 후 내용 유지**를 확인했고 오류가 없었다고 보고했습니다. 해당 흐름은 **사용자 수동 검증 완료**로 기록합니다. 실행 주체는 사용자이며, Codex가 직접 실행하거나 화면·DB를 재관찰한 결과가 아닙니다. 위의 미확인 기록은 각 실행 시점의 상태로 보존합니다.

| 항목 | 최신 상태 및 근거 |
|---|---|
| 실제 MySQL 환경에서 메모 작성·수정 | 사용자 수동 검증 완료 — 사용자 보고 |
| 브라우저 새로고침 후 내용 유지 | 사용자 수동 검증 완료 — 사용자 보고 |
| 앱 종료·재실행 후 내용 유지 | 사용자 수동 검증 완료 — 사용자 보고 |
| 해당 흐름에서 오류 발생 여부 | 사용자가 오류 없음을 보고 |
| 빈 목록·공백 거부·제목만/본문만 저장·길이 제한·저장 실패 안내 | 자동 테스트 통과 기록 유지, 실제 MySQL·브라우저 수동 검증은 미확인 |
| 기존 테이블 사전 조회·스키마 생성 절차 | 수행 여부 미확인 — 이번 보고에 없음 |
| JavaScript 세부 동작·원본 UI 동일성 | 미확인 |

직접 실행한 H2·MockMvc 자동 테스트 9개 및 빌드 성공과 위 사용자 보고를 구분합니다. 이번 문서·업로드 준비 작업에서 앱 소스는 변경하지 않았고 자동 테스트를 추가 실행하지 않았습니다.

### Git 상태

프로젝트 루트에서 기존 Git 저장소 및 원격 연결을 먼저 조회했으며 저장소가 발견되지 않았습니다. `git init -b main`을 실행했으나 `.git: Operation not permitted`로 실패했습니다. 현재 실행 환경의 `.git` 쓰기 제한으로 루트 초기화를 완료하지 못했으며, 원격 연결·커밋·업로드는 수행하지 않았습니다. 일반 터미널에서 다음 명령으로 초기화할 수 있습니다.

```bash
cd /Users/baeseungjun/Projects/rea-assignment
git init -b main
```

업로드 후보 점검에는 프로젝트 밖 임시 Git 메타데이터와 프로젝트 작업 트리를 사용합니다. 이는 루트 저장소 초기화나 스테이징을 대신하지 않습니다.

### 업로드 후보 점검 결과

- 후보 **38개 파일**. 앱 소스·테스트·SQL·Maven Wrapper·문서·기존 정제 증거 포함.
- 실제 제외 경로: `.rea-setup-backup/`, `app/target/`.
- 인증정보 파일, 개인 메모 디렉터리, 브라우저 프로필·쿠키, 원시 캡처·HAR, 터미널/대화 기록, DB 덤프 및 빌드 결과를 제외하는 규칙을 보강했습니다. `evidence/`는 검수한 기존 11개 파일만 명시적으로 허용합니다.
- 후보 텍스트 및 설정 백업의 비밀값 패턴을 점검했고, 확인된 비밀값은 발견되지 않았습니다. 패턴이 표시한 `app/src/test/resources/application-test.properties`는 비밀번호가 빈 테스트 전용 H2 설정으로 확인했습니다. 패턴 검사는 모든 종류의 비밀값 부재를 보장하지 않습니다.
- 제출용 PNG 4장을 직접 확인했습니다. 지정한 테스트 메모만 보이며 계정 표시·배경 개인 메모는 보이지 않습니다. 기존 정제 JSON·설명과 원시 자료 제외 원칙을 유지합니다.
- 아래 목록은 업로드 **후보**이며 스테이징·커밋·원격 연결·업로드는 하지 않았습니다.

```text
.gitignore
README.md
app/.mvn/wrapper/maven-wrapper.jar
app/.mvn/wrapper/maven-wrapper.properties
app/db/001-create-notes.sql
app/mvnw
app/mvnw.cmd
app/pom.xml
app/scripts/run-local.sh
app/src/main/java/kr/rea/keep/KeepApplication.java
app/src/main/java/kr/rea/keep/Note.java
app/src/main/java/kr/rea/keep/NoteController.java
app/src/main/java/kr/rea/keep/NoteForm.java
app/src/main/java/kr/rea/keep/NoteNotFoundException.java
app/src/main/java/kr/rea/keep/NoteRepository.java
app/src/main/java/kr/rea/keep/NoteService.java
app/src/main/resources/application.properties
app/src/main/resources/static/app.js
app/src/main/resources/static/style.css
app/src/main/resources/templates/editor.html
app/src/main/resources/templates/error.html
app/src/main/resources/templates/notes.html
app/src/test/java/kr/rea/keep/NoteFailureTest.java
app/src/test/java/kr/rea/keep/NoteIntegrationTest.java
app/src/test/resources/application-test.properties
app/src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker
docs/development-log.md
evidence/keep-archive/01-before.png
evidence/keep-archive/02-archived.png
evidence/keep-archive/03-unarchived.png
evidence/keep-archive/04-refreshed.png
evidence/keep-archive/README.ko.md
evidence/keep-archive/SHA256SUMS.txt
evidence/keep-archive/calls.sanitized.json
evidence/keep-archive/launch-analysis-chrome.sh
evidence/keep-archive/observations.sanitized.json
evidence/keep-archive/previous-connection-failure.ko.md
evidence/keep-archive/rea-connection-error.json
```

## 2026-11-09 보관함·보관·보관 해제 구현

### 시작 상태 및 범위

작업 시작 시 Git 작업 트리는 깨끗했고 HEAD는 `b7ba111`이었습니다. 원격 `origin`은 `https://github.com/bae-seung/rea-keep.git`으로 확인했습니다. 기존 코드, 템플릿, 실행 스크립트, SQL, 테스트, README 및 `evidence/keep-archive/`의 설명·기존 정제 관찰 기록을 근거로 작업했습니다. 이번에 커밋·푸시는 하지 않았습니다.

### REA 추가 관찰

`reverse-engineer-anything` 스킬의 런타임 관찰 지침을 읽고 REA로 CDP 연결을 확인했습니다. `list_browser_targets`는 성공했으며, 기존 테스트 메모가 열린 Chrome을 대상으로 `inspect_web_page`를 수행했습니다. 제목·본문은 기존 지정 테스트 메모와 일치했습니다.

Google Keep의 접근성 결과에서 `지금은 연결할 수 없습니다. 나중에 다시 시도해 주세요.`와 `보관처리` 버튼 disabled 상태가 관찰됐습니다. 따라서 보관/해제 클릭을 시도하지 않았고 개인 메모를 열거나 수정하지 않았습니다. **전체 목록↔보관함 이동은 미확인**입니다. 사용자에게 분석용 Chrome의 인터넷·로그인 상태 확인과 기존 테스트 메모 새로고침을 안내했습니다. CDP 연결 자체는 정상으로, 재설치나 REA 설정 변경은 수행하지 않았습니다.

기존 증거는 그대로 보존하고 `evidence/keep-archive-list/`에 새 README, 접근성 발췌 JSON, 해시를 추가했습니다. 원시 DOM·메모 URL·계정·네트워크 본문·개인 메모는 새 제출 파일에 저장하지 않았습니다. `.gitignore`에는 새 정제 파일 3개만 명시적으로 허용했습니다.

### 앱 설계 및 변경

- `Note.archived` boolean 필드, MySQL `BIT(1) NOT NULL DEFAULT 0` 추가. **우리 앱의 설계 선택**으로 Google의 DB 구조를 알아낸 것이 아닙니다.
- `/` 전체 메모는 `archived=false`, `/archive` 보관함은 `archived=true`만 Repository 쿼리로 조회합니다. 각 목록은 수정 시각 내림차순입니다.
- 카드의 독립 POST 폼으로 보관 및 해제. `/notes/{id}/archive`, `/notes/{id}/unarchive`는 명시적 목표 상태를 지정하며 반복 요청으로 반전되지 않습니다.
- 서비스 트랜잭션에서 상태만 갱신합니다. 제목·본문·ID·생성 시각·수정 시각은 유지합니다. 보관은 내용 수정이 아니라는 설계 판단에 따라 수정 시각도 변경하지 않습니다.
- `@DynamicUpdate`로 변경된 열만 갱신해 상태 변경 시 불필요한 내용 열 덮어쓰기를 줄입니다. 동시 편집의 충돌 해결을 보장하는 구현은 아닙니다.
- 보관 중 내용 수정도 가능하며 상태는 유지하고 저장 후 보관함으로 돌아갑니다.
- 존재하지 않는 대상은 404, 상태 변경 실패는 503과 오류 안내, 성공 flash 없음. 오류 화면에서 전체 목록과 보관함으로 이동 가능.
- 목록에 여러 폼이 생기므로 JavaScript를 모든 폼에 적용하고 처리 상태 요소가 없는 경우도 처리했습니다.
- 당시 실행 스크립트에 소스/빌드 설정과 JAR의 시각 비교를 추가했습니다. **이 검사는 고정된 JAR 시각 때문에 오작동하여 2026-11-10 수정에서 제거했습니다.** 현재 실행 절차는 아래 후속 기록을 따릅니다.

### 스키마 적용과 보존

기존 `001-create-notes.sql`은 변경하지 않았습니다. `002-add-archived.sql`은 archived 열이 없을 때 한 번 실행하는 ALTER이며 기존 행의 기본 상태를 0으로 설정합니다. 제목·본문·생성/수정 시각·행 수는 유지합니다. 실제 MySQL에는 적용하지 않았습니다.

README에 앱 종료 → 구조·열·행 수 조회 → 열이 없을 때 002만 적용 → 변경 후 구조·행 수 확인 → 새 JAR 빌드 → 숨김 비밀번호 입력으로 실행 순서를 명시했습니다. 새 DB는 001과 002를 차례로 적용합니다. `ddl-auto=validate`와 SQL 자동 실행 금지 설정을 유지합니다.

### 자동 검증 결과

최초 빌드 호출은 프로젝트 루트에서 `./mvnw`를 찾지 못해 실행되지 않았고, 올바른 `app/` 작업 디렉터리로 수정했습니다. 기존 로컬 캐시를 이용해 다음 명령으로 전체 빌드·테스트를 실행했습니다.

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) \
MAVEN_USER_HOME=/private/tmp/rea-m2 \
MAVEN_OPTS=-Dmaven.user.home=/private/tmp/rea-m2 \
./mvnw -o -Dmaven.repo.local=/private/tmp/rea-m2/repository clean verify
```

결과: **BUILD SUCCESS**, 2026-11-09 22:55:59 KST, 18.580초. **15개 테스트 통과, 실패 0, 오류 0, 건너뜀 0**. 기존 테스트 9개를 유지하고 6개를 추가했습니다.

| 검증 | 결과와 실행 주체 |
|---|---|
| Java 21 컴파일·실행 JAR 패키징 | Codex 직접 실행, 성공 |
| 상태 기본값·보관/해제·반복 요청·목록 분리 | H2 자동 테스트 통과 |
| 제목·본문·생성 시각·수정 시각 보존 | H2 자동 테스트 통과 |
| 보관 중 편집 상태 유지·보관함 리다이렉트 | H2/MockMvc 자동 테스트 통과 |
| 빈 보관함·카드 버튼·없는 대상 404 | MockMvc 자동 테스트 통과 |
| 상태 변경/보관함 조회 오류 시 503·성공 안내 없음 | MockMvc 모의 장애 테스트 통과 |
| 실제 002 SQL 실행 후 기존 행·내용·시각·기본값 보존 | H2 MySQL 모드 자동 테스트 통과. 실제 MySQL DDL을 대체하지 않음 |
| 실행 스크립트 셸 문법, Git 공백 오류 | 통과 |
| 첫 구현의 MySQL 작성·수정·새로고침·재시작 유지 | 앞선 **사용자 수동 검증 완료** 기록 유지 |
| 새 보관 기능의 실제 MySQL 마이그레이션·저장·재시작 | **미확인** |
| 새 앱 UI의 실제 브라우저·JavaScript 실행 | **미확인**. MockMvc는 브라우저가 아님 |
| Google Keep 전체 목록↔보관함 이동 | **미확인**. 연결 불가 안내와 disabled 버튼만 새로 관찰 |

현재 실행 JAR는 `app/target/keep-0.0.1-SNAPSHOT.jar`이며 빌드 결과는 Git 제외 대상입니다. 다음 단계는 사용자가 README 순서로 MySQL 002 적용, 빌드 및 재실행 후 테스트 메모의 보관·해제·내용 유지·재시작 상태 유지를 검증하는 것입니다.


## 2026-11-10 실행 스크립트의 JAR 시각 의존 제거

### 원인 확인

사용자는 성공한 빌드의 JAR 수정 시각이 1980-02-01로 설정되어 `-nt`/`-newer` 검사가 실행을 차단한다고 보고했습니다.

- `app/pom.xml`은 Boot 4.0.9 부모와 `spring-boot-maven-plugin`을 사용하며, 프로젝트 및 `.mvn` 설정에는 `project.build.outputTimestamp`나 1980 시각의 직접 지정이 없습니다. 로컬 부모 POM에서 `repackage` 실행 바인딩을 확인했습니다. POM은 수정하지 않았습니다.
- 플러그인의 `META-INF/maven/plugin.xml`에서 `outputTimestamp`의 기본 참조가 `${project.build.outputTimestamp}`임을 확인했습니다. [공식 Repackager API](https://docs.spring.io/spring-boot/4.0/api/java/org/springframework/boot/loader/tools/Repackager.html)는 지정된 수정 시각을 아카이브 파일과 내부 항목에 적용할 수 있음을 설명합니다.
- 처음 조회한 파일 자체의 시각은 2026-11-10 16:04:09였고 내부 ZIP 항목에는 1980-02-01이 표시됐습니다. 둘은 별개의 시각이므로 이 조회만으로 사용자 보고 재현이라 기록하지 않았습니다.
- 이어서 **실제 `clean verify` 성공 직후** `stat`으로 파일 수정 시각 **1980-02-01 09:00:00 KST**를 확인했습니다. 빌드 로그에는 jar 생성과 Boot repackage가 실행됐습니다. 따라서 이 프로젝트의 패키징 결과 시각이 빌드 완료 시각을 나타내지 않는 현상을 재현했습니다. 오래된 시각을 빌드 실패/구형 JAR의 증거로 삼은 실행 스크립트가 결함의 원인입니다. 내부 기본 시각을 결정하는 모든 플러그인 코드 경로까지 추적했다고 주장하지 않습니다.
- `help:effective-pom`은 오프라인 캐시에 help 플러그인이 없어 실패했습니다. 유효 POM 출력을 얻었다고 기록하지 않습니다.

### 변경

`run-local.sh`는 Java 21 선택 → 기존 DB_PASSWORD 해제 → `./mvnw verify` → JAR 존재 확인 → 비밀번호 숨김 입력 → Java 실행 순서입니다. `set -e`로 빌드 실패 시 즉시 종료하며 기존 JAR가 있어도 실행하지 않습니다. `-nt`, `find -newer` 검사를 제거했습니다. 비밀번호는 Maven 실행 이후에만 입력받고, 파일·로그·명령행 인자에 기록하지 않습니다. 종료 시 변수도 해제합니다.

README의 기존 별도 빌드·시각 검사 안내를 수정했습니다. 이제 DB 변경 절차와 기존 앱 종료를 마친 후 `./app/scripts/run-local.sh`를 실행하면 매번 verify 후 앱을 실행합니다. 앞서 수행한 보관 기능 변경은 유지했으며 이번에 앱 기능·POM·DB·증거 파일은 변경하지 않았습니다.

### 실제 검증 결과

1. 스크립트 셸 문법 검사 통과.
2. 임시 디렉터리의 스크립트 사본에서 Maven/Java를 모의 실행기로 대체해 다음 분기를 확인했습니다. 실제 MySQL 검증과 구분합니다.
   - Maven 실패(exit 17): Java 실행 없이 종료.
   - Maven 성공 + 1980 시각의 JAR: Java 실행 경로 도달.
   - Maven 성공 + JAR 없음: exit 1, Java 실행 없음.
   - 테스트용 무작위 입력값이 표준 출력·오류에 나타나지 않음. 입력값은 파일에 저장하지 않았고 모의 Maven에는 DB_PASSWORD가 전달되지 않음을 검사함.
3. 실제 스크립트 실행 시 초기 캐시 설정 시도는 사용자 홈 Maven 저장소의 잠금 파일 쓰기 제한으로 실패했습니다. 스크립트도 비밀번호 입력 전 종료했습니다. `MAVEN_OPTS`로 쓰기 가능한 캐시를 지정한 후 재실행했습니다.

```bash
MAVEN_USER_HOME=/private/tmp/rea-m2 \
MAVEN_OPTS='-Dmaven.user.home=/private/tmp/rea-m2 -Dmaven.repo.local=/private/tmp/rea-m2/repository' \
./app/scripts/run-local.sh </dev/null
```

Maven 결과는 **BUILD SUCCESS**, 2026-11-10 16:09:48 KST, **15 tests / 0 failures / 0 errors / 0 skipped**였습니다. stdin을 `/dev/null`로 두어 비밀번호 입력 단계에서 EOF로 스크립트가 종료(exit 1)했으며, 실제 앱을 실행하거나 MySQL에 접속하지 않았습니다. 원격 metadata 조회에는 DNS 경고가 있었으나 캐시로 빌드는 성공했습니다.

4. 파일 시각 재현을 위해 `app/`에서 기존 오프라인 캐시 옵션으로 `./mvnw clean verify`를 실행했습니다. **BUILD SUCCESS**, 2026-11-10 16:10:24 KST, 18.684초, **15개 테스트 모두 통과**. 직후 위 1980 파일 시각을 확인했습니다.
5. Git 공백 오류 검사 통과. 커밋·푸시하지 않았습니다. 실제 비밀번호 입력 후 앱 실행·MySQL·브라우저 검증은 이번 작업에서 수행하지 않았습니다.


## 2026-11-10 보관 기능 사용자 수동 검증 보고 및 실행 스크립트 재확인

사용자는 우리 앱의 실제 MySQL·브라우저에서 다음 세 가지를 모두 확인했다고 보고했습니다.

| 항목 | 최신 상태 | 실행 주체·근거 |
|---|---|---|
| 보관 시 전체 목록 제외 및 보관함 표시 | **사용자 수동 검증 완료** | 사용자 보고 |
| 서버 재시작 후 보관 상태 유지 | **사용자 수동 검증 완료** | 사용자 보고 |
| 보관 해제 시 전체 목록 복귀 및 제목·본문 유지 | **사용자 수동 검증 완료** | 사용자 보고 |
| 원본 Google Keep의 전체 목록↔보관함 이동 | **미확인 유지** | 기존 REA 관찰에서 연결 불가 안내·비활성 보관 버튼 확인 |

앞선 미확인 기록은 당시의 상태로 보존하며, 우리 앱의 위 세 항목은 이 보고로 갱신합니다. Codex가 직접 실행한 H2·MockMvc 자동 테스트 15개와 사용자 수동 검증은 별개입니다. 생성/수정 시각 보존, 보관 해제 후 재시작 유지, 오류 안내, 마이그레이션 절차·행 수 비교 등 사용자가 보고하지 않은 수동 검증은 완료로 바꾸지 않았습니다. 원본 Google Keep의 증거 파일도 변경하지 않았습니다.

`app/scripts/run-local.sh`를 다시 읽어 이전 수정이 반영돼 있음을 확인했습니다. 현재는 `set -eu`, `set +x` 적용 후 기존 DB_PASSWORD를 해제하고 `./mvnw verify`를 실행합니다. 성공한 경우에만 JAR 존재 확인, `read -r -s` 숨김 입력, Java 실행으로 진행합니다. 빌드 실패 시 종료하며 JAR 수정 시각 비교는 없습니다. 요청한 동작이 이미 구현돼 있어 스크립트를 추가 변경하지 않았습니다.

이번 변경 파일은 **README.md**, **docs/development-log.md** 두 개입니다. 셸 문법 검사와 Git 공백 오류 검사를 통과했습니다. 앱 소스·스크립트 변경이 없어 빌드/자동 테스트를 재실행하지 않았으며, 앞선 15개 통과 기록을 유지합니다. 실제 DB·브라우저도 이번에 직접 실행하지 않았습니다. 기존 미커밋 구현 변경을 보존했고 커밋·푸시하지 않았습니다.

## 2026-11-10 검색·휴지통 구현

### 시작 상태와 원본 관찰

HEAD `1ef15e1`, 깨끗한 작업 트리에서 시작했습니다. 기존 코드·테스트·SQL·실행 스크립트와 기존 보관 증거, REA 런타임 관찰 지침을 읽었습니다.

REA `list_browser_targets`와 `inspect_web_page`를 각각 한 번 실행했습니다. Chrome CDP는 연결됐고 기존 과제 메모의 제목·본문이 확인됐지만 Google Keep은 연결 불가 안내를 표시했습니다. 이번에는 보관 버튼이 focusable로 보였으므로 이전 disabled 결과를 이번 관찰로 재사용하지 않았습니다. 사용자 지시에 따라 반복 재시도·원본 상태 변경은 하지 않았습니다. 개인 메모를 열거나 수정하지 않았고 새 메모도 생성하지 않았습니다. 영구 삭제 및 그 확인창 관찰도 실행하지 않았습니다.

제목/본문 검색, 보관 메모 검색 포함, 삭제 메모 검색 제외, 휴지통 이동/복원, 보관 메모 복원 위치, 영구 삭제 확인창은 모두 **원본 미확인**입니다. `evidence/keep-search-trash/`에 별도 정제 기록과 해시를 추가했습니다. 메모 URL·계정·개인 메모·원시 DOM·스크린샷·네트워크 본문은 제출 파일에 저장하지 않았습니다. 기존 증거는 보존했습니다.

### 설계 선택과 구현

- 검색은 보관 메모를 포함하고 휴지통은 제외합니다. 이는 원본 확인 사실이 아닌 **우리 앱의 선택**입니다.
- JPQL `:term` 바인딩과 `locate(lower(:term), ...)`로 제목/본문을 부분 검색합니다. SQL 문자열에 입력을 연결하지 않습니다. `%`/`_`는 문자 그대로 처리합니다. 공백 제거 후 최대 200자이며 빈 검색은 빈 결과입니다.
- `trashed` boolean 추가. 삭제는 상태만 변경하고 행을 유지합니다. 복원은 `archived`를 그대로 두어 삭제 전 위치로 돌려보냅니다. 제목·본문·생성/수정 시각을 변경하지 않습니다. Google의 DB 필드 구조를 분석했다는 뜻이 아닙니다.
- 전체 목록·보관함·검색은 모두 `trashed=false`를 강제합니다. 휴지통은 전용 화면으로 분리하며 편집 링크·보관 버튼을 제공하지 않습니다.
- 서버에서도 휴지통 편집·보관/해제 요청을 409로 거부합니다. 복원과 영구 삭제는 휴지통만 허용합니다. 수정·상태 변경·영구 삭제는 행 잠금 후 상태 확인과 쓰기를 하나의 트랜잭션으로 처리합니다.
- 영구 삭제는 별도 확인 화면과 동의 체크를 제공합니다. GET 확인 화면은 삭제하지 않고 POST에서 확인값·현재 상태를 검사한 후 행을 지웁니다. 취소 링크 제공. 이 확인 화면은 JavaScript 없이도 동작합니다.
- 자동 기간 경과 삭제는 구현하지 않았습니다. 앱은 기존과 동일한 로컬 단일 사용자 과제 범위이며 인증·배포용 보안 기능을 새로 구현한 것은 아닙니다.
- `003-add-trashed.sql`은 한 번 실행하는 열 추가 SQL입니다. 기존 메모는 false로 유지하고 행·내용·시각·보관 상태를 변경하지 않습니다. 실제 MySQL 적용은 사용자에게 안내했으며 직접 수행하지 않았습니다.
- 실행 스크립트는 변경하지 않았습니다. Maven verify 성공 후 비밀번호 숨김 입력·앱 실행, JAR 시각 검사 없음이라는 기존 수정을 유지합니다.

### 자동 검증

`app/`에서 Java 21, Maven Wrapper, 쓰기 가능한 오프라인 캐시를 사용해 `clean verify`를 실행했습니다.

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) \
MAVEN_USER_HOME=/private/tmp/rea-m2 \
MAVEN_OPTS=-Dmaven.user.home=/private/tmp/rea-m2 \
./mvnw -o -Dmaven.repo.local=/private/tmp/rea-m2/repository clean verify
```

**BUILD SUCCESS**, 2026-11-10 16:32:36 KST, 19.160초. **20 tests, 0 failures, 0 errors, 0 skipped**. 기존 테스트를 유지하고 `SearchTrashTest` 5개를 추가했으며 기존 마이그레이션 테스트에서 실제 003 SQL도 실행했습니다.

| 항목 | 결과 |
|---|---|
| 제목·본문 검색, 보관 포함, 대소문자 및 특수문자, 빈/긴 검색어 | H2 자동 테스트 통과 |
| SQL 주입 형태 입력이 검색 조건을 변경하지 않음 | H2 자동 테스트 통과 |
| 휴지통의 전체·보관·검색 제외 및 복원 상태별 분리 | H2 자동 테스트 통과 |
| 복원 시 제목·본문·생성/수정 시각 유지 | H2 자동 테스트 통과 |
| 휴지통 편집·보관 서버 거부 | 서비스 및 MockMvc 통과 |
| 휴지통 밖 영구 삭제 거부, 확인 GET/취소 경로로 행 유지, 확인 없는 POST 거부 | H2/MockMvc 통과 |
| 확인 후 휴지통 행 영구 삭제 | H2/MockMvc 통과 |
| 003 SQL 후 기존 행 기본 상태 및 내용·시각 보존 | H2 MySQL 모드 통과 |
| 실제 MySQL 003 적용·검색·휴지통·복원·영구 삭제·재시작 유지 | **미확인** |
| 실제 새 화면의 브라우저 렌더링·클릭·JS | **미확인** |
| Google Keep의 검색·휴지통 요청 관찰 항목 | **미확인** |

H2 결과는 MySQL collation·DDL·동시성 동작 및 브라우저 검증을 대신하지 않습니다. 이전 사용자 수동 검증은 작성·수정·보관의 보고된 항목에만 적용하고 검색·휴지통에 확대하지 않았습니다. [기능 비교표](feature-comparison.md)를 추가하고 README의 설치 SQL 순서와 실행·수동 확인 절차를 갱신했습니다. Git 공백 오류 검사 통과, 커밋·푸시 없음.

## 2026-11-11 전체 기능 작업 재개

### 중단 지점 확인 및 코드 수정

이전 중단 시점에 체크리스트·라벨·고정·색상 코드와 `004-details.sql`, DB 준비 CLI 및 일괄 실행 스크립트가 작성돼 있었습니다. 이전 최초 테스트에서는 `[x] `가 strip 후 `[x]`가 되면서 빈 항목으로 거부되지 않는 오류가 발견됐고, 2026-11-10 수정 후 25개 테스트가 통과한 로그를 확인했습니다. 이 결과를 당시 기록에 없던 실제 MySQL 검증으로 해석하지 않았습니다.

이번에는 현행 코드의 수정 반영 여부를 직접 확인하고 빈 체크항목 회귀 테스트를 추가했습니다. `[x]`, `[x] `, `[X]`+탭, `[ ]`, `[ ] `, `[x]` 뒤 Unicode 공백을 생성·수정·HTTP 요청에서 거부하며 기존 행을 보존하는지 검사했습니다. 체크리스트는 제목·본문 없이도 유효한 항목이 있으면 저장되며, 체크항목 텍스트도 검색 대상입니다.

일괄 실행 스크립트에서 별도 Chrome 프로세스에 DB_PASSWORD가 상속되지 않도록 `env -u DB_PASSWORD`를 적용했습니다. 비밀번호는 한 번의 숨김 입력으로 Java 준비/서버 프로세스에만 필요한 환경변수로 전달합니다. 파일·명령 인자·로그에 기록하지 않습니다.

### 구현된 전체 범위

- 작성·수정·전체 목록·보관함·검색·휴지통·복원·영구 삭제 확인.
- 체크리스트 추가/제거(줄 편집), 항목 완료/해제, 항목·메모 검증.
- 라벨 생성·이름 변경·삭제, 메모에 여러 라벨 지정, 라벨별 조회. 라벨 삭제는 기존 메모를 삭제하지 않음.
- 고정/해제와 고정 우선 정렬, 5개 허용 색상 변경.
- 휴지통에서 편집·체크·고정·색상·보관 변경 서버 거부. 복원은 삭제 전 상태와 내용 보존.
- 추가 필드·테이블·검색 범위·정렬·복원 규칙은 우리 앱의 설계 선택. 미관찰 원본 동작은 계속 미확인.

### DB 준비 및 실행

`DatabaseSetup`은 `--prepare-db` 명시적 실행에서만 JDBC로 metadata를 확인하고 없는 항목만 001~004 순서로 추가합니다. 기존 행의 삭제·초기화는 하지 않습니다. 정상 서버는 여전히 `ddl-auto=validate`이며 불일치 스키마를 자동 덮어쓰지 않습니다. H2 마이그레이션 테스트에서는 준비를 두 번 실행하고 기존 제목·본문·시각·archived=true 보존 및 새 필드 기본값을 확인했습니다. 실제 MySQL DDL은 아직 미확인입니다.

`prepare-and-run.sh`는 verify 성공 → 숨김 비밀번호 1회 입력 → DB 추가 변경 → 서버 → 별도 Chrome(CDP 9227)을 실행합니다. 기존 8080 서버가 있으면 종료를 요구하고 다른 프로세스를 임의로 중단하지 않습니다. `target/restart.request` 생성 시 자신이 시작한 서버만 재시작합니다. 종료 시 변수 해제. DB 준비·서버 재시작에 대한 브라우저 검증은 실제 연결 이후에만 완료로 기록합니다.

### 이번 직접 검증

- Java 21 및 오프라인 Maven 캐시를 사용한 `clean verify`: **BUILD SUCCESS**, 2026-11-11 12:36:38 KST, 26.810초.
- **26 tests / 0 failures / 0 errors / 0 skipped**. H2·MockMvc이며 실제 MySQL 성공이라는 뜻이 아닙니다.
- 셸 문법 검사와 `git diff --check`: 통과.
- 앱용 REA `list_browser_targets` 9227: **provider_unavailable / endpoint_unreachable**. 연결된 앱 브라우저를 찾지 못했습니다. 이전 셸 로컬 HTTP는 `Operation not permitted`였고 이번 단순 연결도 실패했습니다. 이것만으로 MySQL 서버가 중단됐다고 단정하지 않습니다.
- 실제 MySQL 인증정보를 요청·조회·기록하지 않았습니다. 사용자에게 일반 터미널에서 일괄 실행 스크립트와 숨김 입력 한 번만 안내했습니다. 기능별 수동 검사는 요청하지 않았습니다.
- 실제 앱 브라우저 화면·새 MySQL 저장·재시작 지속성은 **연결 전 미확인**입니다. 기존 사용자 보고의 작성·보관 검증을 새 기능에 확대하지 않았습니다.
- 원본 Keep에서는 기존 연결 불가 관찰을 유지합니다. 미확인 동작을 관찰 성공으로 바꾸지 않았습니다.

README를 전체 기능과 일괄 실행 중심으로 정리하고, 기능 비교표와 `docs/runtime-verification.md`를 갱신했습니다. 기존 증거를 보존했습니다. GitHub 푸시는 하지 않았습니다.


## 2026-11-11 — 실제 MySQL·브라우저 전체 흐름 및 재시작 검증

사용자가 준비 스크립트 실행을 알린 뒤 REA로 앱 전용 Chrome CDP 9227에 연결했다. 기존 개인 메모를 열거나 수정하지 않고 `REA-QA-1111` 테스트 메모 3개(ID 3/4/5), 라벨 2개(ID 1/2)만 생성·변경했다. 제목·본문·체크항목 검색, 보관 포함/휴지통 제외, 작성·수정·새 페이지 유지, 보관·해제, 휴지통·복원 위치, 영구 삭제 확인·취소·동의 후 삭제, 체크항목 추가·제거·완료 토글, 라벨 생성·이름 변경·지정·조회·삭제 시 메모 보존, 고정·색상을 확인했다. 공백 메모와 내용 없는 [x] 오류 안내도 실제 화면에서 확인했다.

Java 프로세스의 3306 연결을 확인했다. restart.request로 두 번 재시작(PID 39471→40114→40349)한 뒤 데이터와 상태를 재조회했다. 첫 재시작은 일반/보관/휴지통 상태를 모두 포함하고 두 번째는 영구 삭제·라벨 삭제 유지까지 확인했다. 두 차례 모두 테스트 라벨 페이지 HTML이 전후 동일했다. 별도 SQL 조회·DB 버전 조회·생성 시각 값 비교는 하지 않았으며, 8.0.44는 사용자 환경 정보다. 추가 스키마가 적용된 앱의 validate 기동과 확장 필드 저장은 확인했지만 개인 데이터 전체의 변경 전후 대조로 확대하지 않는다.

이전 26개 H2 자동 테스트 및 clean verify 성공 기록과 이번 직접 검증을 구분한다. 현재 Surefire 보고서도 26개 실패/오류/건너뜀 0임을 확인했다. 이번 코드 변경은 없으므로 빌드를 반복하지 않았다. 자동화의 잘못된 CSS 선택자 대기는 중단한 뒤 제한 시간 있는 역할 선택자로 정상 수행했다.

정제 시나리오 59개와 명시적 DOM 확인 69개(모두 일치), 개인정보 없는 실제 화면 PNG 3개를 evidence/app-verification에 저장했다. 원시 DOM·세션 식별자·쿠키·전체 터미널 로그는 제출 증거에 넣지 않았다. README, 검증표, 원본·구현 비교표, .gitignore 허용 목록을 갱신했다. 기존 원본 증거는 보존했고 미관찰 원본 동작은 미확인으로 유지했다. 최종 데모 메모 ID 3/4와 라벨 ID 1을 남겼고 테스트 ID 5와 라벨 ID 2만 영구 삭제했다. 커밋·푸시는 하지 않았다.
