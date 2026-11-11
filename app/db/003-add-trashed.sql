-- 앱 종료 후 기존 구조 및 trashed 열 부재를 확인하고 한 번만 적용하세요.
-- 기존 행의 내용/시각/보관 상태를 보존하며 휴지통 상태만 false로 초기화합니다.
ALTER TABLE rea_app_notes ADD COLUMN trashed BIT(1) NOT NULL DEFAULT 0;
