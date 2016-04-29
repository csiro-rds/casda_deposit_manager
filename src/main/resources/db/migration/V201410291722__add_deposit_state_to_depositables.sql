-- Add deposit_state_name to the various 'Depositable' tables
-- see CASDA-3153

ALTER TABLE casda.observation ADD COLUMN deposit_state_name VARCHAR( 255 ) NOT NULL DEFAULT 'undeposited';
COMMENT ON COLUMN casda.observation.deposit_state_name is 'Deposit state of observation as a whole';

ALTER TABLE casda.observation ADD COLUMN metadata_file_deposit_state_name VARCHAR( 255 ) NOT NULL DEFAULT 'undeposited';
COMMENT ON COLUMN casda.observation.metadata_file_deposit_state_name is 'Deposit state of observation metadata file';

ALTER TABLE casda.catalogue ADD COLUMN deposit_state_name VARCHAR( 255 ) NOT NULL DEFAULT 'undeposited';
COMMENT ON COLUMN casda.catalogue.deposit_state_name is 'Deposit state of catalogue file';

ALTER TABLE casda.image_cube ADD COLUMN deposit_state_name VARCHAR( 255 ) NOT NULL DEFAULT 'undeposited';
COMMENT ON COLUMN casda.image_cube.deposit_state_name is 'Deposit state of image file';

ALTER TABLE casda.measurement_set ADD COLUMN deposit_state_name VARCHAR( 255 ) NOT NULL DEFAULT 'undeposited';
COMMENT ON COLUMN casda.measurement_set.deposit_state_name is 'Deposit state of measurement-set file';

ALTER TABLE casda.validation_file ADD COLUMN deposit_state_name VARCHAR( 255 ) NOT NULL DEFAULT 'undeposited';
COMMENT ON COLUMN casda.validation_file.deposit_state_name is 'Deposit state of validation file';
