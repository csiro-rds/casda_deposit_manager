-- CASDA-5850 - Define objects and schema for storing moment maps


--step 1 create table
CREATE TABLE casda.moment_map
(
  id bigserial NOT NULL, 
  observation_id bigint NOT NULL,
  project_id bigint NOT NULL,
  image_cube_id bigint NOT NULL,
  format character varying(255) NOT NULL,
  filename character varying(1000) NOT NULL,
  last_modified timestamp with time zone DEFAULT now(), 
  filesize bigint, 
  target_name character varying(255), 
  ra_deg double precision, 
  dec_deg double precision,
  t_min double precision, 
  t_max double precision, 
  t_exptime double precision, 
  em_min double precision, 
  em_max double precision, 
  em_resolution double precision, 
  deposit_state character varying(255) NOT NULL DEFAULT 'UNDEPOSITED'::character varying, 
  channel_width double precision, 
  num_chan integer, 
  centre_freq double precision,
  stokes_parameters character varying(255) DEFAULT '//'::character varying, 
  deposit_state_changed timestamp with time zone, 
  checkpoint_state_type character varying(255) NOT NULL DEFAULT 'UNDEPOSITED'::character varying, 
  deposit_failure_count integer DEFAULT 0,
  quality_level character varying(15) DEFAULT 'NOT_VALIDATED'::character varying, 
  version bigint DEFAULT 1, 
  released_date timestamp with time zone, 
  dimensions text,
  type character varying(50) NOT NULL,
  thumbnail bigint, 
  header text,
  s_region geometry,
  s_region_poly spoly,
  CONSTRAINT moment_map_pkey PRIMARY KEY (id),
  CONSTRAINT moment_map_observation_id_fkey FOREIGN KEY (observation_id)
      REFERENCES casda.observation (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT moment_map_project_id_fkey FOREIGN KEY (project_id)
      REFERENCES casda.project (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT moment_map_image_id_fkey FOREIGN KEY (image_cube_id)
      REFERENCES casda.image_cube (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT thumbnail_id_fkey FOREIGN KEY (thumbnail)
      REFERENCES casda.thumbnail (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT momtypefk FOREIGN KEY (type)
      REFERENCES casda.image_type (type_name) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);

--step 2 add comments
COMMENT ON TABLE casda.moment_map IS 'Describes a moment_map of an astronomical object.';
COMMENT ON COLUMN casda.moment_map.id IS 'The primary key';
COMMENT ON COLUMN casda.moment_map.observation_id IS 'The foreign key into the parent Observation';
COMMENT ON COLUMN casda.moment_map.image_cube_id IS 'the Image Cube this moment_map was extracted from';
COMMENT ON COLUMN casda.moment_map.project_id IS 'The foreign key into the Project table';
COMMENT ON COLUMN casda.moment_map.format IS 'format of the moment_map. Eg fits';
COMMENT ON COLUMN casda.moment_map.filename IS 'Path to the moment_map file (relative to the observation folder).';
COMMENT ON COLUMN casda.moment_map.last_modified IS 'When the row was last modified (usually via an insert)';
COMMENT ON COLUMN casda.moment_map.filesize IS 'The size of the moment_map in kilobytes';
COMMENT ON COLUMN casda.moment_map.target_name IS 'Astronomical object observed, if any';
COMMENT ON COLUMN casda.moment_map.ra_deg IS 'Central right ascension, ICRS, decimal degrees';
COMMENT ON COLUMN casda.moment_map.dec_deg IS 'Central declination, ICRS, decimal degrees';
COMMENT ON COLUMN casda.moment_map.t_min IS 'Image exposure Start Time/Date in Modified Julian Date format';
COMMENT ON COLUMN casda.moment_map.t_max IS 'Image exposure Stop Time/Date in Modified Julian Date format';
COMMENT ON COLUMN casda.moment_map.t_exptime IS 'Total exposure time in seconds';
COMMENT ON COLUMN casda.moment_map.em_min IS 'Shortest wavelength observed, in metres.';
COMMENT ON COLUMN casda.moment_map.em_max IS 'Longest wavelength observed, in metres.';
COMMENT ON COLUMN casda.moment_map.em_resolution IS 'Value of resolution along the spectral axis, in metres';
COMMENT ON COLUMN casda.moment_map.deposit_state IS 'Deposit state of the moment_map';
COMMENT ON COLUMN casda.moment_map.channel_width IS 'Width of each frequency channel in kHz.';
COMMENT ON COLUMN casda.moment_map.num_chan IS 'Number of frequency channels.';
COMMENT ON COLUMN casda.moment_map.centre_freq IS 'The central frequency of the observation in MHz.';
COMMENT ON COLUMN casda.moment_map.stokes_parameters IS E'List of Stokes polarization parameters (I, Q, U, and/or V) recorded in the moment_map (values are delimited by ''/''s and are surrounded with a leading and trailing ''/'')';
COMMENT ON COLUMN casda.moment_map.deposit_state_changed IS 'The datetime that the deposit state of the moment_map last changed';
COMMENT ON COLUMN casda.moment_map.checkpoint_state_type IS 'Checkpoint state of moment_map';
COMMENT ON COLUMN casda.moment_map.deposit_failure_count IS 'Number of times the deposit of the moment_map has failed.';
COMMENT ON COLUMN casda.moment_map.quality_level IS 'Indicator of quality level, updated by validators';
COMMENT ON COLUMN casda.moment_map.version IS 'Version number field for optimistic locking on the record';
COMMENT ON COLUMN casda.moment_map.released_date IS 'The date that the moment_map data product was released';
COMMENT ON COLUMN casda.moment_map.dimensions IS 'The moment_map dimensions, json output from WCS lib';
COMMENT ON COLUMN casda.moment_map.type IS 'The type of moment_map contained in the FITS file';
COMMENT ON COLUMN casda.moment_map.thumbnail IS 'Id of the thumbnail of the moment_map';
COMMENT ON COLUMN casda.moment_map.header IS 'The text of the header of the moment_map''s FITS file';
COMMENT ON COLUMN casda.moment_map.s_region is 'Spatial region covered by the image.';
COMMENT ON COLUMN casda.moment_map.s_region_poly is 'Spatial region covered by the image.';  


--step 3 create indexes
CREATE INDEX idx_moment_map_filename
  ON casda.moment_map
  USING btree
  (filename COLLATE pg_catalog."default");

CREATE INDEX idx_moment_map_observation
  ON casda.moment_map
  USING btree
  (observation_id);

CREATE INDEX idx_moment_map_image_cube
  ON casda.moment_map
  USING btree
  (image_cube_id);

CREATE INDEX idx_moment_map_project
  ON casda.moment_map
  USING btree
  (project_id);
  
CREATE INDEX idx_moment_map_quality_level
  ON casda.moment_map
  USING btree
  (quality_level COLLATE pg_catalog."default");

CREATE UNIQUE INDEX moment_map_observation_filename_unique_key
  ON casda.moment_map
  USING btree
  (observation_id, filename COLLATE pg_catalog."default");



--step 4 create data access job table
CREATE TABLE casda.data_access_job_moment_map
(
  id bigserial NOT NULL,
  data_access_job_id bigint, 
  moment_map_id bigint, 
  last_modified timestamp with time zone DEFAULT now(), 
  CONSTRAINT data_access_job_moment_map_pkey PRIMARY KEY (id),
  CONSTRAINT data_access_job_moment_map_data_access_job_id_fkey FOREIGN KEY (data_access_job_id)
      REFERENCES casda.data_access_job (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT data_access_job_moment_map_moment_map_id_fkey FOREIGN KEY (moment_map_id)
      REFERENCES casda.moment_map (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);


--step 5 add comments
COMMENT ON TABLE casda.data_access_job_moment_map IS 'Join table providing a relationship between Data Access Job and Spectrum';
COMMENT ON COLUMN casda.data_access_job_moment_map.id IS 'The primary key';
COMMENT ON COLUMN casda.data_access_job_moment_map.data_access_job_id IS 'The foreign key into the Data Access Job table';
COMMENT ON COLUMN casda.data_access_job_moment_map.moment_map_id IS 'The foreign key into the moment_map table';
COMMENT ON COLUMN casda.data_access_job_moment_map.last_modified IS 'When the row was last modified (usually via an insert)';


--step 6 create indexes
CREATE INDEX idx_access_job_moment_map
  ON casda.data_access_job_moment_map
  USING btree
  (moment_map_id);

CREATE INDEX idx_access_job_moment_map_job
  ON casda.data_access_job_moment_map
  USING btree
  (data_access_job_id);

