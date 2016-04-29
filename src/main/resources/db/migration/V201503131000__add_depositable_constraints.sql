-- Add appropriate constaints for Observation and its Depositable Artefacts to the database

ALTER TABLE casda.observation ALTER COLUMN telescope SET NOT NULL;
ALTER TABLE casda.observation ALTER COLUMN obs_program SET NOT NULL;
ALTER TABLE casda.observation ALTER COLUMN obs_start SET NOT NULL;
ALTER TABLE casda.observation ALTER COLUMN obs_start_mjd SET NOT NULL;
ALTER TABLE casda.observation ALTER COLUMN obs_end SET NOT NULL;
ALTER TABLE casda.observation ALTER COLUMN obs_end_mjd SET NOT NULL;
ALTER TABLE casda.observation ALTER COLUMN deposit_started SET NOT NULL;

ALTER TABLE casda.project ALTER COLUMN opal_code SET NOT NULL;
ALTER TABLE casda.project ALTER COLUMN short_name SET NOT NULL;
-- The following constraint is being removed as it is seen as overly constraining
ALTER TABLE casda.project DROP CONSTRAINT unique_project_shortname;

ALTER TABLE casda.image_cube ALTER COLUMN observation_id SET NOT NULL;
ALTER TABLE casda.image_cube ALTER COLUMN project_id SET NOT NULL;
ALTER TABLE casda.image_cube ALTER COLUMN filename SET NOT NULL;
ALTER TABLE casda.image_cube ALTER COLUMN format SET NOT NULL;
CREATE UNIQUE INDEX image_cube_observation_filename_unique_key ON casda.image_cube (observation_id, filename);
COMMENT ON COLUMN casda.image_cube.filename IS 'Path to the image file (relative to the observation folder).';

ALTER TABLE casda.catalogue ALTER COLUMN project_id SET NOT NULL;
ALTER TABLE casda.catalogue ALTER COLUMN filename SET NOT NULL;
ALTER TABLE casda.catalogue ALTER COLUMN format SET NOT NULL;
-- The following constraint will probably break with Level 7 catalogues
CREATE UNIQUE INDEX catalogue_observation_filename_unique_key ON casda.catalogue (observation_id, filename);
COMMENT ON COLUMN casda.catalogue.filename IS 'Path to the catalogue file (relative to the observation folder).';

ALTER TABLE casda.measurement_set ALTER COLUMN observation_id SET NOT NULL;
ALTER TABLE casda.measurement_set ALTER COLUMN project_id SET NOT NULL;
ALTER TABLE casda.measurement_set ALTER COLUMN filename SET NOT NULL;
ALTER TABLE casda.measurement_set ALTER COLUMN format SET NOT NULL;
CREATE UNIQUE INDEX measurement_set_observation_filename_unique_key ON casda.measurement_set (observation_id, filename);
COMMENT ON COLUMN casda.measurement_set.filename IS 'Path to the measurement set file (relative to the observation folder).';

ALTER TABLE casda.evaluation_file ALTER COLUMN observation_id SET NOT NULL;
ALTER TABLE casda.evaluation_file ALTER COLUMN filename SET NOT NULL;
ALTER TABLE casda.evaluation_file ALTER COLUMN format SET NOT NULL;
CREATE UNIQUE INDEX evaluation_file_observation_filename_unique_key ON casda.evaluation_file (observation_id, filename);
COMMENT ON COLUMN casda.evaluation_file.filename IS 'Path to the evaluation file (relative to the observation folder).';

ALTER TABLE casda.scan ALTER COLUMN measurement_set_id SET NOT NULL;
ALTER TABLE casda.scan ALTER COLUMN scan_start SET NOT NULL;
ALTER TABLE casda.scan ALTER COLUMN scan_end SET NOT NULL;
ALTER TABLE casda.scan ALTER COLUMN field_centre_x SET NOT NULL;
ALTER TABLE casda.scan ALTER COLUMN field_centre_y SET NOT NULL;
ALTER TABLE casda.scan ALTER COLUMN coord_system SET NOT NULL;
ALTER TABLE casda.scan ALTER COLUMN polarisations SET NOT NULL;
ALTER TABLE casda.scan ALTER COLUMN num_channels SET NOT NULL;
ALTER TABLE casda.scan ALTER COLUMN centre_freq SET NOT NULL;
ALTER TABLE casda.scan ALTER COLUMN channel_width SET NOT NULL;
