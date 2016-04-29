-- Remove deposit_state_name from the various 'Depositable' tables
-- see CASDA-3153

ALTER TABLE casda.observation DROP COLUMN deposit_state_name;

ALTER TABLE casda.observation DROP COLUMN metadata_file_deposit_state_name;

ALTER TABLE casda.catalogue DROP COLUMN deposit_state_name;

ALTER TABLE casda.image_cube DROP COLUMN deposit_state_name;

ALTER TABLE casda.measurement_set DROP COLUMN deposit_state_name;

ALTER TABLE casda.validation_file DROP COLUMN deposit_state_name;
