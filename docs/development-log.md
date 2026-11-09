# 개발 기록 — 첫 구현

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
