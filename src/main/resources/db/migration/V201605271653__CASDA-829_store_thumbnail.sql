-- Table: casda.thumbnail
-- DROP TABLE casda.thumbnail;

CREATE TABLE casda.thumbnail
(
  id bigserial NOT NULL, 
  observation_id bigint NOT NULL,
  format character varying(255) NOT NULL, 
  filename character varying(1000) NOT NULL, 
  last_modified timestamp with time zone DEFAULT now(), 
  deposit_state character varying(255) NOT NULL DEFAULT 'UNDEPOSITED'::character varying,
  deposit_state_changed timestamp with time zone,
  checkpoint_state_type character varying(255) NOT NULL DEFAULT 'UNDEPOSITED'::character varying,
  deposit_failure_count integer DEFAULT 0, 
  filesize bigint, 
  version bigint DEFAULT 1,
  CONSTRAINT thumbnail_pkey PRIMARY KEY (id),
  CONSTRAINT thumbnail_observation_id_fkey FOREIGN KEY (observation_id)
      REFERENCES casda.observation (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

COMMENT ON TABLE casda.thumbnail
  IS 'For each thumbnail, a file element will be present with the content from this table.';
COMMENT ON COLUMN casda.thumbnail.id IS 'The primary key';
COMMENT ON COLUMN casda.thumbnail.observation_id IS 'The foreign key into the Observation table';
COMMENT ON COLUMN casda.thumbnail.format IS 'The format of the thumbnail, eg png';
COMMENT ON COLUMN casda.thumbnail.filename IS 'Path to the thumbnail (relative to the observation folder).';
COMMENT ON COLUMN casda.thumbnail.last_modified IS 'When the row was last modified (usually via an insert)';
COMMENT ON COLUMN casda.thumbnail.deposit_state IS 'Deposit state of thumbnail';
COMMENT ON COLUMN casda.thumbnail.checkpoint_state_type IS 'Checkpoint state of thumbnail';
COMMENT ON COLUMN casda.thumbnail.deposit_failure_count IS 'Number of times the deposit of the thumbnail has failed.';
COMMENT ON COLUMN casda.thumbnail.filesize IS 'The size of the thumbnail in kilobytes';
COMMENT ON COLUMN casda.thumbnail.version IS 'Version number field for optimistic locking on the record';

ALTER TABLE casda.image_cube DROP  thumbnail_name;
ALTER TABLE casda.image_cube ADD COLUMN large_thumbnail bigint;
ALTER TABLE casda.image_cube ADD COLUMN small_thumbnail bigint;
ALTER TABLE casda.image_cube ADD CONSTRAINT large_thumbnail_id_fkey FOREIGN KEY (large_thumbnail) 
	REFERENCES casda.thumbnail (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;
	
ALTER TABLE casda.image_cube ADD CONSTRAINT small_thumbnail_id_fkey FOREIGN KEY (small_thumbnail) 
	REFERENCES casda.thumbnail (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;
	
COMMENT ON COLUMN casda.image_cube.large_thumbnail IS 'Id of the Larger thumbnail of the image cube';
COMMENT ON COLUMN casda.image_cube.small_thumbnail IS 'Id of the Smaller thumbnail of the image cube';