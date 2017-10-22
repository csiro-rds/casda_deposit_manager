-- CASDA-5853 - Define objects and schema for storing spectra


--step 1 create table
CREATE TABLE casda.spectrum
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
  CONSTRAINT spectrum_pkey PRIMARY KEY (id),
  CONSTRAINT spectrum_observation_id_fkey FOREIGN KEY (observation_id)
      REFERENCES casda.observation (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT spectrum_project_id_fkey FOREIGN KEY (project_id)
      REFERENCES casda.project (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT spectrum_image_id_fkey FOREIGN KEY (image_cube_id)
      REFERENCES casda.image_cube (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT thumbnail_id_fkey FOREIGN KEY (thumbnail)
      REFERENCES casda.thumbnail (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT spectypefk FOREIGN KEY (type)
      REFERENCES casda.spectrum_type (type_name) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);

--step 2 add comments
COMMENT ON TABLE casda.spectrum IS 'Describes a spectrum of an astronomical object.';
COMMENT ON COLUMN casda.spectrum.id IS 'The primary key';
COMMENT ON COLUMN casda.spectrum.observation_id IS 'The foreign key into the parent Observation';
COMMENT ON COLUMN casda.spectrum.image_cube_id IS 'the Image Cube this spectrum was extracted from';
COMMENT ON COLUMN casda.spectrum.project_id IS 'The foreign key into the Project table';
COMMENT ON COLUMN casda.spectrum.format IS 'format of the spectrum. Eg fits';
COMMENT ON COLUMN casda.spectrum.filename IS 'Path to the spectrum file (relative to the observation folder).';
COMMENT ON COLUMN casda.spectrum.last_modified IS 'When the row was last modified (usually via an insert)';
COMMENT ON COLUMN casda.spectrum.filesize IS 'The size of the spectrum in kilobytes';
COMMENT ON COLUMN casda.spectrum.target_name IS 'Astronomical object observed, if any';
COMMENT ON COLUMN casda.spectrum.ra_deg IS 'Central right ascension, ICRS, decimal degrees';
COMMENT ON COLUMN casda.spectrum.dec_deg IS 'Central declination, ICRS, decimal degrees';
COMMENT ON COLUMN casda.spectrum.t_min IS 'Image exposure Start Time/Date in Modified Julian Date format';
COMMENT ON COLUMN casda.spectrum.t_max IS 'Image exposure Stop Time/Date in Modified Julian Date format';
COMMENT ON COLUMN casda.spectrum.t_exptime IS 'Total exposure time in seconds';
COMMENT ON COLUMN casda.spectrum.em_min IS 'Shortest wavelength observed, in metres.';
COMMENT ON COLUMN casda.spectrum.em_max IS 'Longest wavelength observed, in metres.';
COMMENT ON COLUMN casda.spectrum.em_resolution IS 'Value of resolution along the spectral axis, in metres';
COMMENT ON COLUMN casda.spectrum.deposit_state IS 'Deposit state of the spectrum';
COMMENT ON COLUMN casda.spectrum.channel_width IS 'Width of each frequency channel in kHz.';
COMMENT ON COLUMN casda.spectrum.num_chan IS 'Number of frequency channels.';
COMMENT ON COLUMN casda.spectrum.centre_freq IS 'The central frequency of the observation in MHz.';
COMMENT ON COLUMN casda.spectrum.stokes_parameters IS E'List of Stokes polarization parameters (I, Q, U, and/or V) recorded in the spectrum (values are delimited by ''/''s and are surrounded with a leading and trailing ''/'')';
COMMENT ON COLUMN casda.spectrum.deposit_state_changed IS 'The datetime that the deposit state of the spectrum last changed';
COMMENT ON COLUMN casda.spectrum.checkpoint_state_type IS 'Checkpoint state of spectrum';
COMMENT ON COLUMN casda.spectrum.deposit_failure_count IS 'Number of times the deposit of the spectrum has failed.';
COMMENT ON COLUMN casda.spectrum.quality_level IS 'Indicator of quality level, updated by validators';
COMMENT ON COLUMN casda.spectrum.version IS 'Version number field for optimistic locking on the record';
COMMENT ON COLUMN casda.spectrum.released_date IS 'The date that the spectrum data product was released';
COMMENT ON COLUMN casda.spectrum.dimensions IS 'The spectrum dimensions, json output from WCS lib';
COMMENT ON COLUMN casda.spectrum.type IS 'The type of spectrum contained in the FITS file';
COMMENT ON COLUMN casda.spectrum.thumbnail IS 'Id of the thumbnail of the spectrum';
COMMENT ON COLUMN casda.spectrum.header IS 'The text of the header of the spectrum''s FITS file';


--step 3 create indexes
CREATE INDEX idx_spectrum_filename
  ON casda.spectrum
  USING btree
  (filename COLLATE pg_catalog."default");

CREATE INDEX idx_spectrum_observation
  ON casda.spectrum
  USING btree
  (observation_id);

CREATE INDEX idx_spectrum_image_cube
  ON casda.spectrum
  USING btree
  (image_cube_id);

CREATE INDEX idx_spectrum_project
  ON casda.spectrum
  USING btree
  (project_id);
  
CREATE INDEX idx_spectrum_quality_level
  ON casda.spectrum
  USING btree
  (quality_level COLLATE pg_catalog."default");

CREATE UNIQUE INDEX spectrum_observation_filename_unique_key
  ON casda.spectrum
  USING btree
  (observation_id, filename COLLATE pg_catalog."default");



--step 4 create data access job table
CREATE TABLE casda.data_access_job_spectrum
(
  id bigserial NOT NULL,
  data_access_job_id bigint, 
  spectrum_id bigint, 
  last_modified timestamp with time zone DEFAULT now(), 
  CONSTRAINT data_access_job_spectrum_pkey PRIMARY KEY (id),
  CONSTRAINT data_access_job_spectrum_data_access_job_id_fkey FOREIGN KEY (data_access_job_id)
      REFERENCES casda.data_access_job (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT data_access_job_spectrum_spectrum_id_fkey FOREIGN KEY (spectrum_id)
      REFERENCES casda.spectrum (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);


--step 5 add comments
COMMENT ON TABLE casda.data_access_job_spectrum IS 'Join table providing a relationship between Data Access Job and Spectrum';
COMMENT ON COLUMN casda.data_access_job_spectrum.id IS 'The primary key';
COMMENT ON COLUMN casda.data_access_job_spectrum.data_access_job_id IS 'The foreign key into the Data Access Job table';
COMMENT ON COLUMN casda.data_access_job_spectrum.spectrum_id IS 'The foreign key into the spectrum table';
COMMENT ON COLUMN casda.data_access_job_spectrum.last_modified IS 'When the row was last modified (usually via an insert)';


--step 6 create indexes
CREATE INDEX idx_access_job_spectrum
  ON casda.data_access_job_spectrum
  USING btree
  (spectrum_id);

CREATE INDEX idx_access_job_spectrum_job
  ON casda.data_access_job_spectrum
  USING btree
  (data_access_job_id);

