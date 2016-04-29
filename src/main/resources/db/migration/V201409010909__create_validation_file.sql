-- Holds references to validation files --
-- The spec for this SQL is described in CASDA-2045 --

CREATE TABLE casda.validation_file (  
id               BIGSERIAL PRIMARY KEY,
observation_id   BIGINT references casda.observation(id),
format           VARCHAR( 255 ),
filename         VARCHAR( 1000 ),
last_modified  TIMESTAMP DEFAULT now()
);

CREATE INDEX idx_validation_file_obs ON casda.validation_file( observation_id );

COMMENT ON TABLE casda.validation_file is 'For each validation file, a file element will be present with the content from this table.';

COMMENT ON COLUMN casda.validation_file.id is 'The primary key';  
COMMENT ON COLUMN casda.validation_file.observation_id is 'The foreign key into the Observation table';
COMMENT ON COLUMN casda.validation_file.format is 'The format of the validation file, eg pdf';
COMMENT ON COLUMN casda.validation_file.filename is 'Full path to the validation file. Eg /scratch/askap/12345/validation.pdf';
COMMENT ON COLUMN casda.validation_file.last_modified is 'When the row was last modified (usually via an insert)';
