-- 기존 메모를 보존하는 추가 변경. prepare-and-run.sh는 각 항목의 존재 여부를 먼저 확인합니다.
ALTER TABLE rea_app_notes ADD COLUMN pinned BIT(1) NOT NULL DEFAULT 0;
ALTER TABLE rea_app_notes ADD COLUMN color VARCHAR(16) NOT NULL DEFAULT 'DEFAULT';
CREATE TABLE rea_app_labels (id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY, name VARCHAR(50) NOT NULL UNIQUE);
CREATE TABLE rea_app_check_items (note_id BIGINT NOT NULL, item_order INTEGER NOT NULL, item_text VARCHAR(200) NOT NULL, done BIT(1) NOT NULL DEFAULT 0, PRIMARY KEY(note_id,item_order), FOREIGN KEY(note_id) REFERENCES rea_app_notes(id));
CREATE TABLE rea_app_note_labels (note_id BIGINT NOT NULL, label_id BIGINT NOT NULL, PRIMARY KEY(note_id,label_id), FOREIGN KEY(note_id) REFERENCES rea_app_notes(id), FOREIGN KEY(label_id) REFERENCES rea_app_labels(id));
