ALTER TABLE casda.catalogue ADD COLUMN version BIGINT DEFAULT 1;
ALTER TABLE casda.evaluation_file ADD COLUMN version BIGINT DEFAULT 1;
ALTER TABLE casda.image_cube ADD COLUMN version BIGINT DEFAULT 1;
ALTER TABLE casda.measurement_set ADD COLUMN version BIGINT DEFAULT 1;

COMMENT ON COLUMN casda.catalogue.version is 'Version number field for optimistic locking on the record';
COMMENT ON COLUMN casda.evaluation_file.version is 'Version number field for optimistic locking on the record';
COMMENT ON COLUMN casda.image_cube.version is 'Version number field for optimistic locking on the record';
COMMENT ON COLUMN casda.measurement_set.version is 'Version number field for optimistic locking on the record';