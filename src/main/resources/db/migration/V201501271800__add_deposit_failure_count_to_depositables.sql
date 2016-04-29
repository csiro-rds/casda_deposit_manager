-- Add deposit_state to the various 'Depositable' tables
-- see CASDA-3153

ALTER TABLE casda.observation ADD COLUMN deposit_failure_count INT DEFAULT 0;
COMMENT ON COLUMN casda.observation.deposit_failure_count is 'Number of times the observation deposit has failed';

ALTER TABLE casda.observation ADD COLUMN metadata_file_deposit_failure_count INT DEFAULT 0;
COMMENT ON COLUMN casda.observation.metadata_file_deposit_failure_count is 'Number of times the deposit of the observation metadata file has failed.';

ALTER TABLE casda.catalogue ADD COLUMN deposit_failure_count INT DEFAULT 0;
COMMENT ON COLUMN casda.catalogue.deposit_failure_count is 'Number of times the deposit of the catalogue has failed.';

ALTER TABLE casda.image_cube ADD COLUMN deposit_failure_count INT DEFAULT 0;
COMMENT ON COLUMN casda.image_cube.deposit_failure_count is 'Number of times the deposit of the image cube has failed.';

ALTER TABLE casda.measurement_set ADD COLUMN deposit_failure_count INT DEFAULT 0;
COMMENT ON COLUMN casda.measurement_set.deposit_failure_count is 'Number of times the deposit of the measurement set has failed.';

ALTER TABLE casda.validation_file ADD COLUMN deposit_failure_count INT DEFAULT 0;
COMMENT ON COLUMN casda.validation_file.deposit_failure_count is 'Number of times the deposit of the validation file has failed.';
