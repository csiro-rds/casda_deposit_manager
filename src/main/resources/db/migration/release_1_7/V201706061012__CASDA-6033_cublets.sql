--step 1 create table
CREATE TABLE casda.cubelet
(
  id bigserial NOT NULL, -- The primary key
  observation_id bigint, -- The foreign key into the parent Observation
  project_id bigint NOT NULL, -- The foreign key into the Project table
  image_cube_id bigint, -- the Image Cube this cubelet was extracted from
  format character varying(255) NOT NULL, -- format of the cubelet. Eg fits
  filename character varying(1000) NOT NULL, -- Path to the cubelet file (relative to the observation folder).
  last_modified timestamp with time zone DEFAULT now(), -- When the row was last modified (usually via an insert)
  filesize bigint, -- The size of the cubelet in kilobytes
  target_name character varying(255), -- Astronomical object observed, if any
  ra_deg double precision, -- Minimum right ascension, ICRS, decimal degrees
  dec_deg double precision, -- Maximum right ascension, ICRS, decimal degrees
  t_min double precision, -- Image exposure Start Time/Date in Modified Julian Date format
  t_max double precision, -- Image exposure Stop Time/Date in Modified Julian Date format
  t_exptime double precision, -- Total exposure time in seconds
  em_min double precision, -- Shortest wavelength observed, in metres.
  em_max double precision, -- Longest wavelength observed, in metres.
  em_resolution double precision, -- Value of resolution along the spectral axis, in metres
  deposit_state character varying(255) NOT NULL DEFAULT 'UNDEPOSITED'::character varying, -- Deposit state of the cubelet
  channel_width double precision, -- Width of each frequency channel in kHz.
  num_chan integer, -- Number of frequency channels.
  centre_freq double precision, -- The central frequency of the observation in MHz.
  stokes_parameters character varying(255) DEFAULT '//'::character varying, -- List of Stokes polarization parameters (I, Q, U, and/or V) recorded in the cubelet (values are delimited by '/'s and are surrounded with a leading and trailing '/')
  deposit_state_changed timestamp with time zone, -- The datetime that the deposit state of the cubelet last changed
  checkpoint_state_type character varying(255) NOT NULL DEFAULT 'UNDEPOSITED'::character varying, -- Checkpoint state of cubelet
  deposit_failure_count integer DEFAULT 0, -- Number of times the deposit of the cubelet has failed.
  quality_level character varying(15) DEFAULT 'NOT_VALIDATED'::character varying, -- Indicator of quality level, updated by validators
  version bigint DEFAULT 1, -- Version number field for optimistic locking on the record
  released_date timestamp with time zone, -- The date that the cubelet data product was released
  dimensions text, -- The cubelet dimensions, json output from WCS lib
  type character varying(50) NOT NULL, -- The type of cubelet contained in the FITS file
  thumbnail bigint, -- Id of the thumbnail of the cubelet
  header text, -- The text of the header of the cubelet's FITS file
  s_region geometry, -- Spatial region covered by the image.
  s_region_poly spoly, -- Spatial region covered by the image.
  encapsulation_file_id bigint, -- The id of the encapsulation file which houses this cubelet
  rest_frequency double precision, -- The rest frequnecy of the cubelet
  level7_collection_id bigint, -- The foreign key into the Level7 Collection table.
  b_unit character varying(255), -- The physical units in which the quantities in the array, after application of BSCALE and BZERO, are expressed.
  b_type character varying(255), -- The type of beam
  CONSTRAINT cubelet_pkey PRIMARY KEY (id),
  CONSTRAINT encapsulation_file_id_fkey FOREIGN KEY (encapsulation_file_id)
      REFERENCES casda.encapsulation_file (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT level7_collection_id_fkey FOREIGN KEY (level7_collection_id)
      REFERENCES casda.level7_collection (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT cubelet_image_id_fkey FOREIGN KEY (image_cube_id)
      REFERENCES casda.image_cube (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT cubelet_observation_id_fkey FOREIGN KEY (observation_id)
      REFERENCES casda.observation (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT cubelet_project_id_fkey FOREIGN KEY (project_id)
      REFERENCES casda.project (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT cubtypefk FOREIGN KEY (type)
      REFERENCES casda.image_type (type_name) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT thumbnail_id_fkey FOREIGN KEY (thumbnail)
      REFERENCES casda.thumbnail (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
  
--step 2 add comments
COMMENT ON TABLE casda.cubelet
  IS 'Describes a cubelet of an astronomical object.';
COMMENT ON COLUMN casda.cubelet.id IS 'The primary key';
COMMENT ON COLUMN casda.cubelet.observation_id IS 'The foreign key into the parent Observation';
COMMENT ON COLUMN casda.cubelet.project_id IS 'The foreign key into the Project table';
COMMENT ON COLUMN casda.cubelet.image_cube_id IS 'the Image Cube this cubelet was extracted from';
COMMENT ON COLUMN casda.cubelet.format IS 'format of the cubelet. Eg fits';
COMMENT ON COLUMN casda.cubelet.filename IS 'Path to the cubelet file (relative to the observation folder).';
COMMENT ON COLUMN casda.cubelet.last_modified IS 'When the row was last modified (usually via an insert)';
COMMENT ON COLUMN casda.cubelet.filesize IS 'The size of the cubelet in kilobytes';
COMMENT ON COLUMN casda.cubelet.target_name IS 'Astronomical object observed, if any';
COMMENT ON COLUMN casda.cubelet.ra_deg IS 'Minimum right ascension, ICRS, decimal degrees';
COMMENT ON COLUMN casda.cubelet.dec_deg IS 'Maximum right ascension, ICRS, decimal degrees';
COMMENT ON COLUMN casda.cubelet.t_min IS 'Image exposure Start Time/Date in Modified Julian Date format';
COMMENT ON COLUMN casda.cubelet.t_max IS 'Image exposure Stop Time/Date in Modified Julian Date format';
COMMENT ON COLUMN casda.cubelet.t_exptime IS 'Total exposure time in seconds';
COMMENT ON COLUMN casda.cubelet.em_min IS 'Shortest wavelength observed, in metres.';
COMMENT ON COLUMN casda.cubelet.em_max IS 'Longest wavelength observed, in metres.';
COMMENT ON COLUMN casda.cubelet.em_resolution IS 'Value of resolution along the spectral axis, in metres';
COMMENT ON COLUMN casda.cubelet.deposit_state IS 'Deposit state of the cubelet';
COMMENT ON COLUMN casda.cubelet.channel_width IS 'Width of each frequency channel in kHz.';
COMMENT ON COLUMN casda.cubelet.num_chan IS 'Number of frequency channels.';
COMMENT ON COLUMN casda.cubelet.centre_freq IS 'The central frequency of the observation in MHz.';
COMMENT ON COLUMN casda.cubelet.stokes_parameters IS 'List of Stokes polarization parameters (I, Q, U, and/or V) recorded in the cubelet (values are delimited by ''/''s and are surrounded with a leading and trailing ''/'')';
COMMENT ON COLUMN casda.cubelet.deposit_state_changed IS 'The datetime that the deposit state of the cubelet last changed';
COMMENT ON COLUMN casda.cubelet.checkpoint_state_type IS 'Checkpoint state of cubelet';
COMMENT ON COLUMN casda.cubelet.deposit_failure_count IS 'Number of times the deposit of the cubelet has failed.';
COMMENT ON COLUMN casda.cubelet.quality_level IS 'Indicator of quality level, updated by validators';
COMMENT ON COLUMN casda.cubelet.version IS 'Version number field for optimistic locking on the record';
COMMENT ON COLUMN casda.cubelet.released_date IS 'The date that the cubelet data product was released';
COMMENT ON COLUMN casda.cubelet.dimensions IS 'The cubelet dimensions, json output from WCS lib';
COMMENT ON COLUMN casda.cubelet.type IS 'The type of cubelet contained in the FITS file';
COMMENT ON COLUMN casda.cubelet.thumbnail IS 'Id of the thumbnail of the cubelet';
COMMENT ON COLUMN casda.cubelet.header IS 'The text of the header of the cubelet''s FITS file';
COMMENT ON COLUMN casda.cubelet.s_region IS 'Spatial region covered by the image.';
COMMENT ON COLUMN casda.cubelet.s_region_poly IS 'Spatial region covered by the image.';
COMMENT ON COLUMN casda.cubelet.encapsulation_file_id IS 'The id of the encapsulation file which houses this cubelet';
COMMENT ON COLUMN casda.cubelet.rest_frequency IS 'The rest frequnecy of the cubelet';
COMMENT ON COLUMN casda.cubelet.level7_collection_id IS 'The foreign key into the Level7 Collection table.';
COMMENT ON COLUMN casda.cubelet.b_unit IS 'The physical units in which the quantities in the array, after application of BSCALE and BZERO, are expressed.';
COMMENT ON COLUMN casda.cubelet.b_type IS 'The type of beam';

--step 3 create indexes
CREATE INDEX idx_cubelet_filename
  ON casda.cubelet
  USING btree
  (filename COLLATE pg_catalog."default");

CREATE INDEX idx_cubelet_image_cube
  ON casda.cubelet
  USING btree
  (image_cube_id);

CREATE INDEX idx_cubelet_observation
  ON casda.cubelet
  USING btree
  (observation_id);

CREATE INDEX idx_cubelet_project
  ON casda.cubelet
  USING btree
  (project_id);

CREATE INDEX idx_cubelet_quality_level
  ON casda.cubelet
  USING btree
  (quality_level COLLATE pg_catalog."default");

CREATE UNIQUE INDEX cubelet_observation_filename_unique_key
  ON casda.cubelet
  USING btree
  (observation_id, filename COLLATE pg_catalog."default");

--step 4 create data access job table
CREATE TABLE casda.data_access_job_cubelet
(
  id bigserial NOT NULL,
  data_access_job_id bigint, 
  cubelet_id bigint, 
  last_modified timestamp with time zone DEFAULT now(), 
  CONSTRAINT data_access_job_cubelet_pkey PRIMARY KEY (id),
  CONSTRAINT data_access_job_cubelet_data_access_job_id_fkey FOREIGN KEY (data_access_job_id)
      REFERENCES casda.data_access_job (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT data_access_job_cubelet_cubelet_id_fkey FOREIGN KEY (cubelet_id)
      REFERENCES casda.cubelet (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);


--step 5 add comments
COMMENT ON TABLE casda.data_access_job_cubelet IS 'Join table providing a relationship between Data Access Job and Cublet';
COMMENT ON COLUMN casda.data_access_job_cubelet.id IS 'The primary key';
COMMENT ON COLUMN casda.data_access_job_cubelet.data_access_job_id IS 'The foreign key into the Data Access Job table';
COMMENT ON COLUMN casda.data_access_job_cubelet.cubelet_id IS 'The foreign key into the cubelet table';
COMMENT ON COLUMN casda.data_access_job_cubelet.last_modified IS 'When the row was last modified (usually via an insert)';


--step 6 create indexes
CREATE INDEX idx_access_job_cubelet
  ON casda.data_access_job_cubelet
  USING btree
  (cubelet_id);

CREATE INDEX idx_access_job_cubelet_job
  ON casda.data_access_job_cubelet
  USING btree
  (data_access_job_id);
  
--step 7 add path for l7 cubelets
ALTER TABLE casda.level7_collection ADD COLUMN cubelet_path CHARACTER VARYING(255);
COMMENT ON COLUMN casda.level7_collection.cubelet_path IS 'The path of any cubelet files contained in this collection';