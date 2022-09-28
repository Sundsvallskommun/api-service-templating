CREATE TABLE `templates`(
    `id` VARCHAR(255) NOT NULL,
    `content` LONGTEXT NOT NULL,
    `description` VARCHAR(255) DEFAULT NULL,
    `identifier` VARCHAR(255) NOT NULL,
    `name` VARCHAR(64) NOT NULL,
    PRIMARY KEY(`id`),
    UNIQUE KEY `UK_4q1tdf7obrcywxc29xrrbarh7`(`identifier`),
    UNIQUE KEY `UK_1nah70jfu9ck93htxiwym9c3b`(`name`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE `templates_default_values`(
    `id` VARCHAR(36) NOT NULL,
    `field_name` VARCHAR(32) NOT NULL,
    `value` VARCHAR(255) NOT NULL,
    `template_id` VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY (`id`),
    KEY `FKqx7usakgnn052hgxb06fa9dlh`(`template_id`),
    CONSTRAINT `FKqx7usakgnn052hgxb06fa9dlh` FOREIGN KEY(`template_id`) REFERENCES `templates`(`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE `templates_metadata`(
    `id` VARCHAR(36)  NOT NULL,
    `metadata_key` VARCHAR(32)  NOT NULL,
    `value` VARCHAR(255) NOT NULL,
    `template_id` VARCHAR(255) DEFAULT NULL,
    PRIMARY KEY(`id`),
    KEY `FKbphtmw91jxf06nxg18kov2930`(`template_id`),
    CONSTRAINT `FKbphtmw91jxf06nxg18kov2930` FOREIGN KEY(`template_id`) REFERENCES `templates`(`id`)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
