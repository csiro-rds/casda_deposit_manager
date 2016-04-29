-- Add checkpoint_state_type to the various 'Depositable' tables

ALTER TABLE casda.observation ADD COLUMN checkpoint_state_type VARCHAR( 255 ) NOT NULL DEFAULT 'UNDEPOSITED';
COMMENT ON COLUMN casda.observation.checkpoint_state_type is 'Checkpoint state of observation as a whole';

ALTER TABLE casda.observation ADD COLUMN metadata_file_checkpoint_state VARCHAR( 255 ) NOT NULL DEFAULT 'UNDEPOSITED';
COMMENT ON COLUMN casda.observation.metadata_file_checkpoint_state is 'Checkpoint state of observation metadata file';

ALTER TABLE casda.catalogue ADD COLUMN checkpoint_state_type VARCHAR( 255 ) NOT NULL DEFAULT 'UNDEPOSITED';
COMMENT ON COLUMN casda.catalogue.checkpoint_state_type is 'Checkpoint state of catalogue file';

ALTER TABLE casda.image_cube ADD COLUMN checkpoint_state_type VARCHAR( 255 ) NOT NULL DEFAULT 'UNDEPOSITED';
COMMENT ON COLUMN casda.image_cube.checkpoint_state_type is 'Checkpoint state of image file';

ALTER TABLE casda.measurement_set ADD COLUMN checkpoint_state_type VARCHAR( 255 ) NOT NULL DEFAULT 'UNDEPOSITED';
COMMENT ON COLUMN casda.measurement_set.checkpoint_state_type is 'Checkpoint state of measurement-set file';

ALTER TABLE casda.validation_file ADD COLUMN checkpoint_state_type VARCHAR( 255 ) NOT NULL DEFAULT 'UNDEPOSITED';
COMMENT ON COLUMN casda.validation_file.checkpoint_state_type is 'Checkpoint state of validation file';
