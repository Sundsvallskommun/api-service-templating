-- Add latest column to templates table
ALTER TABLE `templates`
    ADD COLUMN `latest` BIT(1) NOT NULL DEFAULT 0;

-- Backfill: set latest = 1 for the highest version of each (identifier, municipality_id) group
UPDATE `templates` t
    JOIN (SELECT identifier, municipality_id, MAX(major * 10000 + minor) AS max_ver
          FROM `templates`
          GROUP BY identifier, municipality_id) g ON t.identifier = g.identifier
        AND t.municipality_id = g.municipality_id
        AND (t.major * 10000 + t.minor) = g.max_ver
SET t.latest = 1;

-- Add index for query performance
CREATE INDEX idx_templates_latest ON `templates` (`latest`);
