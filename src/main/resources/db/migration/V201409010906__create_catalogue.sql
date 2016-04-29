-- A Catalog to be archived (e.g format, filename) --
-- The spec for this SQL is described in CASDA-2045 --

CREATE TABLE casda.catalogue (  
id               BIGSERIAL PRIMARY KEY,
observation_id   BIGINT references casda.observation(id),
project_id       BIGINT references casda.project(id),
format           VARCHAR ( 255 ),
filename         VARCHAR( 1000 ),
last_modified  TIMESTAMP DEFAULT now()
);

CREATE INDEX idx_catalogue_observation ON casda.catalogue( observation_id );
CREATE INDEX idx_catalogue_project ON casda.catalogue( project_id );
CREATE INDEX idx_catalogue_filename ON casda.catalogue( filename );
  
COMMENT ON TABLE casda.catalogue is 'Catalog to be archived (e.g format, filename)';  
  
COMMENT ON COLUMN casda.catalogue.id is 'The primary key';
COMMENT ON COLUMN casda.catalogue.observation_id is 'The foreign key into the Observation table';
COMMENT ON COLUMN casda.catalogue.project_id is 'The foreign key into the Project table';
COMMENT ON COLUMN casda.catalogue.format is 'The format of the metadata, eg votable';
COMMENT ON COLUMN casda.catalogue.filename is 'Full path to the catalogue file';
COMMENT ON COLUMN casda.catalogue.last_modified is 'When the row was last modified (usually via an insert)';  