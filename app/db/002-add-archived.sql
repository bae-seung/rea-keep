-- 기존 앱을 종료하고 SHOW CREATE TABLE 및 archived 열 존재 여부를 먼저 확인하세요.
-- archived가 없을 때 한 번만 실행. 이미 있으면 재실행하지 않습니다.
-- 기존 행은 0(보관하지 않음)으로 유지하며 제목/본문/시각은 변경하지 않습니다.
ALTER TABLE rea_app_notes ADD COLUMN archived BIT(1) NOT NULL DEFAULT 0;
