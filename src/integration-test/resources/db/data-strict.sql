INSERT INTO `templates` (`municipality_id`, `id`, `identifier`, `name`, `description`, `major`, `minor`, `changelog`, `latest`, `type`)
VALUES ('2281', '3f2c8a51-6d4e-4b7a-9c1d-2e5f8a7b6c40', 'strict.template', 'Strict template', 'Template with strict parameters', 1, 0, 'Initial version', 1, 'PEBBLE');

INSERT INTO `template_content` (`id`, `content`)
VALUES ('3f2c8a51-6d4e-4b7a-9c1d-2e5f8a7b6c40', 'SGVqIHt7IHBlcnNvbi5uYW1lIH19');

INSERT INTO `templates_metadata` (`id`, `template_id`, `metadata_key`, `value`)
VALUES ('b7e1d4c2-5a3f-4e8b-9d6c-1f2a3b4c5d6e', '3f2c8a51-6d4e-4b7a-9c1d-2e5f8a7b6c40', 'strictParameters', 'true');
