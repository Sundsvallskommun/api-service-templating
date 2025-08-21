CREATE TABLE IF NOT EXISTS template_content (
    id VARCHAR(255) NOT NULL,
    content LONGTEXT NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_template_content_templates_id
        FOREIGN KEY (id) REFERENCES templates(id)
        ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

INSERT INTO template_content (id, content)
    SELECT t.id, t.content
    FROM templates t
    LEFT JOIN template_content tc ON tc.id = t.id
    WHERE tc.id IS NULL;
