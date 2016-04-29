CREATE TABLE casda.observation_other_sbid
(
  observation_id bigserial NOT NULL, -- The primary key
  sbid integer, -- The foreign key into the Observation table (using the sbid column)  
  CONSTRAINT observation_other_sbid_pkey PRIMARY KEY (observation_id, sbid),
  CONSTRAINT observation_other_sbid_unique UNIQUE (observation_id, sbid),
  CONSTRAINT observation_other_sbid_fkey FOREIGN KEY (observation_id)
      REFERENCES casda.observation (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);
COMMENT ON TABLE casda.observation_other_sbid IS 'Multiple Scheduling Blocks associated with an Observation';
COMMENT ON COLUMN casda.observation_other_sbid.observation_id IS 'The observation Id';
COMMENT ON COLUMN casda.observation_other_sbid.sbid IS 'sbid(s) associated with the observation';