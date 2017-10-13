-- CASDA-831 -  Retrieve from CASDA Archive for re-deposit


--step 1 create table
CREATE TABLE casda.refresh_job
(
  id bigserial NOT NULL, 
  job_start_time timestamp with time zone DEFAULT now(),
  job_complete_time timestamp with time zone,
  last_modified timestamp with time zone DEFAULT now(), 
  CONSTRAINT refresh_job_pkey PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);

--step 2 add comments
COMMENT ON TABLE casda.refresh_job IS 'Describes a job to refesh the metadata of a group of observations.';
COMMENT ON COLUMN casda.refresh_job.id IS 'The primary key';
COMMENT ON COLUMN casda.refresh_job.job_start_time IS 'The time at which the job was started.';
COMMENT ON COLUMN casda.refresh_job.job_complete_time IS 'The time at which the job was completed.';
COMMENT ON COLUMN casda.refresh_job.last_modified IS 'When the row was last modified (usually via an insert)';


--step 3 create table
CREATE TABLE casda.observation_refresh
(
  id bigserial NOT NULL,
  refresh_job_id bigint NOT NULL,
  observation_id bigint NOT NULL,
  sbid integer NOT NULL,
  last_modified timestamp with time zone DEFAULT now(), 
  refresh_state character varying(255) NOT NULL DEFAULT 'UNREFRESHED'::character varying, 
  refresh_state_changed timestamp with time zone, 
  CONSTRAINT observation_refresh_pkey PRIMARY KEY (id),
  CONSTRAINT observation_refresh_observation_id_fkey FOREIGN KEY (observation_id)
      REFERENCES casda.observation (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);

--step 4 add comments
COMMENT ON TABLE casda.observation_refresh IS 'Describes a task to refesh the metadata of an observation.';
COMMENT ON COLUMN casda.observation_refresh.id IS 'The primary key';
COMMENT ON COLUMN casda.observation_refresh.refresh_job_id IS 'The foreign key into the parent RefreshJob';
COMMENT ON COLUMN casda.observation_refresh.observation_id IS 'The foreign key into the Observation being refreshed';
COMMENT ON COLUMN casda.observation_refresh.sbid IS 'The id of the observation''s primary scheduling block.';
COMMENT ON COLUMN casda.observation_refresh.last_modified IS 'When the row was last modified (usually via an insert)';
COMMENT ON COLUMN casda.observation_refresh.refresh_state IS 'Refresh state of the task';
COMMENT ON COLUMN casda.observation_refresh.refresh_state_changed IS 'The datetime that the refresh state of the task last changed';


--step 5 create indexes
CREATE INDEX idx_observation_refresh_observation
  ON casda.observation_refresh
  USING btree
  (observation_id);
