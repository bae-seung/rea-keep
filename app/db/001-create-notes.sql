-- 먼저 README의 SHOW TABLES 및 기존 테이블 확인 절차를 수행하세요.
-- 동일 이름이 있으면 실패하며 변경하지 않습니다. IF NOT EXISTS로 충돌을 숨기지 않습니다.
CREATE TABLE rea_app_notes (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 title VARCHAR(200) NOT NULL,
 body TEXT NOT NULL,
 created_at DATETIME(6) NOT NULL,
 updated_at DATETIME(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
