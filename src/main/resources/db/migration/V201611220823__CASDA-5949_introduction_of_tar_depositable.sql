CREATE SEQUENCE casda.encapsulation_file_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

CREATE TABLE casda.encapsulation_file
(
  id bigint NOT NULL DEFAULT nextval('casda.encapsulation_file_id_seq'::regclass), -- The primary key
  observation_id bigint NOT NULL, -- The foreign key into the Observation table
  format character varying(255) NOT NULL, -- The format of the encapsulation file, eg tar
  filename character varying(1000) NOT NULL, -- Path to the encapsulation file (relative to the observation folder).
  last_modified timestamp with time zone DEFAULT now(), -- When the row was last modified (usually via an insert)
  deposit_state character varying(255) NOT NULL DEFAULT 'UNDEPOSITED'::character varying, -- Deposit state of encapsulation file
  deposit_state_changed timestamp with time zone,
  checkpoint_state_type character varying(255) NOT NULL DEFAULT 'UNDEPOSITED'::character varying, -- Checkpoint state of encapsulation file
  deposit_failure_count integer DEFAULT 0, -- Number of times the deposit of the encapsulation file has failed.
  filesize bigint, -- The size of the encapsulation file in kilobytes
  version bigint DEFAULT 1, -- Version number field for optimistic locking on the record
  CONSTRAINT encapsulation_file_pkey PRIMARY KEY (id),
  CONSTRAINT encapsulation_file_observation_id_fkey FOREIGN KEY (observation_id)
      REFERENCES casda.observation (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

CREATE INDEX idx_encapsulation_file_obs ON casda.encapsulation_file( observation_id );

COMMENT ON TABLE casda.encapsulation_file
  IS 'For each encapsulation file, a file element will be present with the content from this table.';
COMMENT ON COLUMN casda.encapsulation_file.id IS 'The primary key';
COMMENT ON COLUMN casda.encapsulation_file.observation_id IS 'The foreign key into the Observation table';
COMMENT ON COLUMN casda.encapsulation_file.format IS 'The format of the encapsulation file, eg tar';
COMMENT ON COLUMN casda.encapsulation_file.filename IS 'Path to the encapsulation file (relative to the observation folder).';
COMMENT ON COLUMN casda.encapsulation_file.last_modified IS 'When the row was last modified (usually via an insert)';
COMMENT ON COLUMN casda.encapsulation_file.deposit_state IS 'Deposit state of encapsulation file';
COMMENT ON COLUMN casda.encapsulation_file.checkpoint_state_type IS 'Checkpoint state of encapsulation file';
COMMENT ON COLUMN casda.encapsulation_file.deposit_failure_count IS 'Number of times the deposit of the encapsulation file has failed.';
COMMENT ON COLUMN casda.encapsulation_file.filesize IS 'The size of the encapsulation file in kilobytes';
COMMENT ON COLUMN casda.encapsulation_file.version IS 'Version number field for optimistic locking on the record';

--update existing tables with encapsulation file id column for linking smaller files to housing tars
ALTER TABLE casda.spectrum ADD COLUMN encapsulation_file_id bigint;
COMMENT ON COLUMN casda.spectrum.encapsulation_file_id IS 'The id of the encapsulation file which houses this spectrum';
ALTER TABLE casda.spectrum ADD CONSTRAINT encapsulation_file_id_fkey FOREIGN KEY (encapsulation_file_id) 
	REFERENCES casda.encapsulation_file (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE casda.moment_map ADD COLUMN encapsulation_file_id bigint;
COMMENT ON COLUMN casda.moment_map.encapsulation_file_id IS 'The id of the encapsulation file which houses this moment map';
ALTER TABLE casda.moment_map ADD CONSTRAINT encapsulation_file_id_fkey FOREIGN KEY (encapsulation_file_id) 
	REFERENCES casda.encapsulation_file (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;

ALTER TABLE casda.thumbnail ADD COLUMN encapsulation_file_id bigint;
COMMENT ON COLUMN casda.thumbnail.encapsulation_file_id IS 'The id of the encapsulation file which houses this thumbnail';
ALTER TABLE casda.thumbnail ADD CONSTRAINT encapsulation_file_id_fkey FOREIGN KEY (encapsulation_file_id) 
	REFERENCES casda.encapsulation_file (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;