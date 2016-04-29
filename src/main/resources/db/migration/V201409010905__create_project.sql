-- A project involved in an observation. --
-- The spec for this SQL is described in CASDA-2045 --

CREATE TABLE casda.project (  
id               BIGSERIAL PRIMARY KEY,
opal_code        VARCHAR( 255 ),
short_name       VARCHAR( 255 ),   
last_modified    TIMESTAMP DEFAULT now()
);

CREATE INDEX idx_project_opal_code ON casda.project( opal_code );
CREATE INDEX idx_project_short_name ON casda.project( short_name );

-- project is unique as it is a lookup table 
ALTER TABLE casda.project ADD CONSTRAINT unique_project_shortname UNIQUE (short_name);

COMMENT ON TABLE casda.project is 'A project involved in an observation.';

COMMENT ON COLUMN casda.project.id is 'The primary key';
COMMENT ON COLUMN casda.project.opal_code is 'OPAL ID of the project';
COMMENT ON COLUMN casda.project.short_name is 'The short (human-friendly) name of the project';
COMMENT ON COLUMN casda.project.last_modified is 'When the row was last modified (usually via an insert)';