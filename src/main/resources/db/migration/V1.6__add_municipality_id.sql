ALTER TABLE `templates` ADD COLUMN `municipality_id` VARCHAR(255) NULL;

UPDATE `templates`
SET `municipality_id` = '2281';

ALTER TABLE `templates` MODIFY `municipality_id` VARCHAR(255) NOT NULL;

ALTER TABLE `templates` ADD CONSTRAINT UNIQUE (identifier, major, minor, municipality_id);

CREATE INDEX idx_templates_municipality_id on templates (municipality_id);