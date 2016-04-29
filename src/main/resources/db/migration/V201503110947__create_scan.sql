-- A scan in a measurement set (aka visibility) --
-- The spec for this SQL is described in CASDA-3937 --

CREATE TABLE casda.scan (  
id                  BIGSERIAL PRIMARY KEY,
measurement_set_id  BIGINT references casda.measurement_set(id),
scan_id             INT NOT NULL,
scan_start          TIMESTAMP WITH TIME ZONE,     
scan_end            TIMESTAMP WITH TIME ZONE,     
field_centre_x      DOUBLE PRECISION, 
field_centre_y      DOUBLE PRECISION, 
coord_system        VARCHAR( 255 ),
field_name          VARCHAR( 255 ),
polarisations       VARCHAR( 255 ),
num_channels        INT,
centre_freq         DOUBLE PRECISION,
channel_width       DOUBLE PRECISION,
last_modified       TIMESTAMP WITH TIME ZONE DEFAULT now(),
unique (measurement_set_id, scan_id)
);

CREATE INDEX idx_scan_measurement_set ON casda.scan( measurement_set_id );
CREATE INDEX idx_scan_start ON casda.scan( scan_start );
CREATE INDEX idx_scan_end ON casda.scan( scan_end );
  
COMMENT ON TABLE casda.scan is 'Describes a scan for a measurement set';

COMMENT ON COLUMN casda.scan.id is 'The primary key';
COMMENT ON COLUMN casda.scan.measurement_set_id is 'The foreign key into the measurement_set table';
COMMENT ON COLUMN casda.scan.scan_id is 'The natural key used to order the scans';
COMMENT ON COLUMN casda.scan.scan_start is 'Time stamp of first correlator integration /
    for this scan.';
COMMENT ON COLUMN casda.scan.scan_end is 'Time stamp of final correlator integration / 
    for this scan.';
COMMENT ON COLUMN casda.scan.field_centre_x is 
    'Field direction in the x axis of the coordinate system specified in column coordsystem. Unit: Radians';
COMMENT ON COLUMN casda.scan.field_centre_y is 
    'Field direction in the y axis of the coordinate system specified in column coordsystem. Unit: Radians';
COMMENT ON COLUMN casda.scan.coord_system is 'Coordinate system for the field centre. /
    Could be J2000, Galactic, maybe even AZEL';
COMMENT ON COLUMN casda.scan.field_name is 'An optional field name or identity';
COMMENT ON COLUMN casda.scan.polarisations is 'Polarisations observed/captured';
COMMENT ON COLUMN casda.scan.num_channels is 'Number of spectral channels';
COMMENT ON COLUMN casda.scan.centre_freq is 'Frequency of centre of the band. Unit: Hz';
COMMENT ON COLUMN casda.scan.channel_width is 'Spectral channel width. Unit: Hz';
COMMENT ON COLUMN casda.scan.last_modified is 'When the row was last modified (usually via an insert)';