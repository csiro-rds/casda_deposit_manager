-- Join table providing a relationship between Observation and Project --
-- The spec for this SQL is described in CASDA-2045 --

CREATE TABLE casda.observation_project (  
id               BIGSERIAL PRIMARY KEY,
observation_id   BIGINT references casda.observation(id),
project_id       BIGINT references casda.project(id),
last_modified  TIMESTAMP DEFAULT now()
);

CREATE INDEX idx_obs_project_obs ON casda.observation_project( observation_id );
CREATE INDEX idx_obs_project_project ON casda.observation_project( project_id );
  
COMMENT ON TABLE casda.observation_project is 'Join table providing a relationship between Observation and Project';

COMMENT ON COLUMN casda.observation_project.id is 'The primary key';  
COMMENT ON COLUMN casda.observation_project.observation_id is 'The foreign key into the Observation table';
COMMENT ON COLUMN casda.observation_project.project_id is 'The foreign key into the Project table';
COMMENT ON COLUMN casda.observation_project.last_modified is 'When the row was last modified (usually via an insert)';