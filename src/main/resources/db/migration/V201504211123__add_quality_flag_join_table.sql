-- Join table between observation, project and quality flag, CASDA-272

CREATE TABLE casda.observation_project_quality_flag (  
id                   BIGSERIAL PRIMARY KEY,
observation_id       BIGINT references casda.observation(id) NOT NULL,
project_id           BIGINT references casda.project(id) NOT NULL,
quality_flag_id      BIGINT references casda.quality_flag(id) NOT NULL,
last_modified  TIMESTAMP DEFAULT now()
);

CREATE INDEX idx_obs_proj_qual_obs ON casda.observation_project_quality_flag( observation_id );
CREATE INDEX idx_obs_proj_qual_proj ON casda.observation_project_quality_flag( project_id );
CREATE INDEX idx_obs_proj_qual_qual ON casda.observation_project_quality_flag( quality_flag_id );
  
COMMENT ON TABLE casda.observation_project_quality_flag is 'Join table providing a relationship between Observation, Project and Quality Flag';

COMMENT ON COLUMN casda.observation_project_quality_flag.id is 'The primary key';  
COMMENT ON COLUMN casda.observation_project_quality_flag.observation_id is 'The foreign key into the Observation table';
COMMENT ON COLUMN casda.observation_project_quality_flag.project_id is 'The foreign key into the Project table';
COMMENT ON COLUMN casda.observation_project_quality_flag.quality_flag_id is 'The foreign key into the Quality Flag table';
COMMENT ON COLUMN casda.observation_project_quality_flag.last_modified is 'When the row was last modified (usually via an insert)';

