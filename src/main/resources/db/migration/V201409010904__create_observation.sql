-- Represents a single Observation --
-- The spec for this SQL is described in CASDA-2045 --

CREATE TABLE casda.observation (  
id               BIGSERIAL PRIMARY KEY,
obs_start        TIMESTAMP,
obs_end          TIMESTAMP,
obs_start_mjd    DOUBLE PRECISION,
obs_end_mjd      DOUBLE PRECISION,
telescope        VARCHAR( 255 ),
sbid             INT,
obs_program      INT,
num_scans        INT, 
last_modified  TIMESTAMP DEFAULT now()
);

CREATE INDEX idx_obs_obs_start ON casda.observation( obs_start );
CREATE INDEX idx_obs_obs_end ON casda.observation( obs_end );
CREATE INDEX idx_obs_telescope ON casda.observation( telescope );
CREATE INDEX idx_obs_sbid ON casda.observation( sbid );
CREATE INDEX idx_obs_obs_program ON casda.observation( obs_program );

-- we can only have one scheduling block for observations
ALTER TABLE casda.observation ADD CONSTRAINT unique_observation_sbid UNIQUE (sbid);

COMMENT ON TABLE casda.observation is 'Describes an observation (e.g. start and end times, \
    which telescope made the observation etc)'; 
  
COMMENT ON COLUMN casda.observation.id is 'The primary key';
COMMENT ON COLUMN casda.observation.obs_start is 'Observation Start Time/Date';
COMMENT ON COLUMN casda.observation.obs_end is 'Observation Stop Time/Date';
COMMENT ON COLUMN casda.observation.obs_start_mjd is 'Observation Start Time/Date in Modified Julian Date format';
COMMENT ON COLUMN casda.observation.obs_end_mjd is 'Observation Stop Time/Date in Modified Julian Date format';
COMMENT ON COLUMN casda.observation.telescope is 'The telescope name';  
COMMENT ON COLUMN casda.observation.sbid is 'This uniquely identifies the observation in the \
    telescope operating system and central processor sub-systems';  
COMMENT ON COLUMN casda.observation.obs_program is 'A collection of scheduling blocks';  
COMMENT ON COLUMN casda.observation.num_scans is 'Number of individual scans in an observation';
COMMENT ON COLUMN casda.observation.last_modified is 'When the row was last modified (usually via an insert)';