-- A set of data related to an observation --
-- The spec for this SQL is described in CASDA-2045 --

CREATE TABLE casda.measurement_set (  
id               BIGSERIAL PRIMARY KEY,
observation_id   BIGINT references casda.observation(id),
project_id       BIGINT references casda.project(id),
format           VARCHAR( 255 ),
filename         VARCHAR( 1000 ),
scan_start       DOUBLE PRECISION,     
scan_end         DOUBLE PRECISION,     
field_centre_x   DOUBLE PRECISION, 
field_centre_y   DOUBLE PRECISION, 
coord_system     VARCHAR( 255 ),
field_name       VARCHAR( 255 ),
polarisations    VARCHAR( 255 ),
num_chan         INT,
centre_freq      DOUBLE PRECISION,
centre_width     DOUBLE PRECISION,
wavelength_min   DOUBLE PRECISION,
wavelength_max   DOUBLE PRECISION,
last_modified    TIMESTAMP DEFAULT now()
);

CREATE INDEX idx_measurement_set_observation ON casda.measurement_set( observation_id );
CREATE INDEX idx_measurement_set_project ON casda.measurement_set( project_id );
CREATE INDEX idx_measurement_set_filename ON casda.measurement_set( filename );
CREATE INDEX idx_measurement_set_scan_start ON casda.measurement_set( scan_start );
CREATE INDEX idx_measurement_set_scan_end ON casda.measurement_set( scan_end );
  
COMMENT ON TABLE casda.measurement_set is 'Describes a measurement set to be archived';

COMMENT ON COLUMN casda.measurement_set.id is 'The primary key';
COMMENT ON COLUMN casda.measurement_set.observation_id is 'The foreign key into the Observation table';
COMMENT ON COLUMN casda.measurement_set.project_id is 'Foreign key into the Project table';
COMMENT ON COLUMN casda.measurement_set.format is 'The format of the data archive, eg tar';
COMMENT ON COLUMN casda.measurement_set.filename is 'Full path to the measurement set file';
COMMENT ON COLUMN casda.measurement_set.scan_start is 'Time stamp of first correlator integration /
    for this scan. Unit: Seconds';
COMMENT ON COLUMN casda.measurement_set.scan_end is 'Time stamp of final correlator integration / 
    for this scan. Unit: Seconds';
COMMENT ON COLUMN casda.measurement_set.field_centre_x is 
    'Field direction in the x axis of the coordinate system specified in column coordsystem. Unit: Radians';
COMMENT ON COLUMN casda.measurement_set.field_centre_y is 
    'Field direction in the y axis of the coordinate system specified in column coordsystem. Unit: Radians';
COMMENT ON COLUMN casda.measurement_set.coord_system is 'Coordinate system for the field centre. /
    Could be J2000, Galactic, maybe even AZEL';
COMMENT ON COLUMN casda.measurement_set.field_name is 'An optional field name or identity';
COMMENT ON COLUMN casda.measurement_set.polarisations is 'Polarisations observed/captured';
COMMENT ON COLUMN casda.measurement_set.num_chan is 'Number of spectral channels';
COMMENT ON COLUMN casda.measurement_set.wavelength_min is 
    'Lowest wavelength of the measurement set. Unit: Metres';
COMMENT ON COLUMN casda.measurement_set.wavelength_max is 
    'Highest wavelength of the measurement set. Unit: Metres';
COMMENT ON COLUMN casda.measurement_set.last_modified is 'When the row was last modified (usually via an insert)';