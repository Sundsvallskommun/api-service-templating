INSERT INTO `templates` (`id`, `identifier`, `name`, `description`, `major`, `minor`, `content`, `changelog`)
VALUES ('852d055a-4ebc-4141-a6e7-530c55cf9bbb', 'first.template', 'First template', 'Some description', 1, 0, 'YsOkIGLDpCB2aXRhIGxhbW0=', 'Initial version');

INSERT INTO `templates` (`id`, `identifier`, `name`, `description`, `major`, `minor`, `content`, `changelog`)
VALUES ('df21b2c9-ddbf-4b4d-bdac-207b29216c60', 'second.template', 'Second template', 'Some description', 1, 0, 'aGFyIGR1IG7DpWdvbiB1bGw=', 'Initial version');

INSERT INTO `templates` (`id`, `identifier`, `name`, `description`, `major`, `minor`, `content`, `changelog`)
VALUES ('a9b98ede-4475-49a4-ad31-6607fb154544', 'third.template', 'Third template', 'Some description', 1, 0, 'amEgamEga8OkcmEgYmFybg==', 'Initial version');
INSERT INTO `templates` (`id`, `identifier`, `name`, `description`, `major`, `minor`, `content`, `changelog`)
VALUES ('72eac1d7-2767-4642-84ab-f9786de7fe63', 'third.template', 'Third template', 'Some description', 1, 1, 'amEgamEga8OkcmEgdW5nZQ==', 'Updated version');

INSERT INTO `templates` (`id`, `identifier`, `name`, `description`, `major`, `minor`, `content`, `changelog`)
VALUES ('d0339b9d-392a-44c9-8038-7480edd471af', 'fourth.template', 'Fourth template', 'Some description', 1, 0, 'amFnIGhhciBzw6Rja2rDpHZlbG4gZnVsbA==', 'Initial version');

INSERT INTO `templates_metadata` (`id`, `template_id`, `metadata_key`, `value`)
VALUES('6a4f8f00-29fa-4dc2-951b-1a71d617e8b3', 'df21b2c9-ddbf-4b4d-bdac-207b29216c60', 'verksamhet', 'SBK');
INSERT INTO `templates_metadata` (`id`, `template_id`, `metadata_key`, `value`)
VALUES('0d39d9a9-d769-4425-a1d9-01e0503132fb', 'df21b2c9-ddbf-4b4d-bdac-207b29216c60', 'process', 'PRH');

INSERT INTO `templates_metadata` (`id`, `template_id`, `metadata_key`, `value`)
VALUES('5a7eb3bc-fdae-4ff1-8acd-12442c81e3aa', 'd0339b9d-392a-44c9-8038-7480edd471af', 'verksamhet', 'SBK');
INSERT INTO `templates_metadata` (`id`, `template_id`, `metadata_key`, `value`)
VALUES('e88118c2-2c2f-4550-9b30-9129e1a021c2', 'd0339b9d-392a-44c9-8038-7480edd471af', 'process', 'MMU');

INSERT INTO `templates_default_values` (`id`, `template_id`, `field_name`, `value`)
VALUES('d3a62c04-e135-497a-a5b6-01c9ee24653b', 'df21b2c9-ddbf-4b4d-bdac-207b29216c60', 'first_name', 'Bobby');
INSERT INTO `templates_default_values` (`id`, `template_id`, `field_name`, `value`)
VALUES('28ea2806-7ebe-40b3-b112-7efbbc5bb6cc', 'df21b2c9-ddbf-4b4d-bdac-207b29216c60', 'last_name', 'Brun');
