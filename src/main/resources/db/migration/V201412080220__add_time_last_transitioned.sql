-- CASDA-3322 Update database schema (add timestamps for state changes)

ALTER TABLE casda.observation ADD COLUMN deposit_started TIMESTAMP;
COMMENT ON COLUMN casda.observation.deposit_started is 'The datetime that the deposit of this observation started';

ALTER TABLE casda.observation ADD COLUMN deposit_state_changed TIMESTAMP;
COMMENT ON COLUMN casda.observation.deposit_state_changed 
is 'The datetime that the deposit state of the observation last changed';

ALTER TABLE casda.observation ADD COLUMN metadata_file_deposit_state_changed TIMESTAMP;
COMMENT ON COLUMN casda.observation.metadata_file_deposit_state_changed 
is 'The datetime that the deposit state of the metadata file last changed';

ALTER TABLE casda.catalogue ADD COLUMN deposit_state_changed TIMESTAMP;
COMMENT ON COLUMN casda.catalogue.deposit_state_changed 
is 'The datetime that the deposit state of the catalogue artifact last changed';

ALTER TABLE casda.image_cube ADD COLUMN deposit_state_changed TIMESTAMP;
COMMENT ON COLUMN casda.image_cube.deposit_state_changed 
is 'The datetime that the deposit state of the image cube artifact last changed';

ALTER TABLE casda.measurement_set ADD COLUMN deposit_state_changed TIMESTAMP;
COMMENT ON COLUMN casda.measurement_set.deposit_state_changed 
is 'The datetime that the deposit state of the measurment set artifact last changed';

ALTER TABLE casda.validation_file ADD COLUMN deposit_state_changed TIMESTAMP;
COMMENT ON COLUMN casda.measurement_set.deposit_state_changed 
is 'The datetime that the deposit state of the validation file artifact last changed';