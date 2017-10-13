-- CASDA-5808 - Schema Sort Order

ALTER TABLE casda.tap_schemas ADD COLUMN schema_order integer;
ALTER TABLE casda.tap_schemas ALTER COLUMN schema_order SET DEFAULT 20;
COMMENT ON COLUMN casda.tap_schemas.schema_order is 'The order in which the schemas should be displayed in the tap/tables endpoint.';

UPDATE casda.tap_schemas set schema_order = 0 where schema_name = 'casda';
UPDATE casda.tap_schemas set schema_order = 5 where schema_name = 'ivoa';
UPDATE casda.tap_schemas set schema_order = 99 where schema_name = 'TAP_SCHEMA';
