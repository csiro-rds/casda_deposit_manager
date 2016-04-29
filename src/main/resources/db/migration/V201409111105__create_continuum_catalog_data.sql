-- Continuum Catalog Data - https://wiki.csiro.au/display/CASDA/ASKAP+Science+Catalogue+Requirements

CREATE TABLE casda.continuum (
	id					BIGSERIAL PRIMARY KEY,
	cont_name			VARCHAR(15),
	catalogue_id		BIGINT references casda.catalogue(id),
	obj_id				VARCHAR(8),
	obj_name			VARCHAR(15),
	flux_int			double precision,
	flux_int_err		REAL,
	flux_peak			double precision,
	flux_peak_err		REAL,
	spectral_index_1	REAL,
	spectral_index_2	REAL,
	noise_rms			REAL,
	x_ave				REAL,
	x_cen				REAL,
	x_max				REAL,
	x_min				REAL,
	x_peak				REAL,
	y_ave				REAL,
	y_cen				REAL,
	y_max				REAL,
	y_min				REAL,
	y_peak				REAL,
	bmaj				REAL,
	bmin				REAL,
	pa					double precision,
	flag_c1				SMALLINT,
	flag_c2				SMALLINT,
	flag_c3				SMALLINT,
	flag_c4				SMALLINT,
	dec_deg_cont		double precision,
	dec_deg_cont_err	REAL,
	dec_dms_cont		VARCHAR(16),
	ra_deg_cont			double precision,
	ra_deg_cont_err		REAL,
	ra_hms_cont			VARCHAR(16),
	last_modified		TIMESTAMP DEFAULT now()
);

-- Create these indexes in lieu of them being primary keys
ALTER TABLE casda.continuum ADD CONSTRAINT unique_continuum_cont_name UNIQUE (cont_name);
ALTER TABLE casda.continuum ADD CONSTRAINT unique_continuum_obj_name UNIQUE (obj_name);

CREATE INDEX idx_flux_int ON casda.continuum(flux_int);
CREATE INDEX idx_flux_peak ON casda.continuum(flux_peak);
CREATE INDEX idx_spectral_index_1 ON casda.continuum(spectral_index_1);
CREATE INDEX idx_flag_c1 ON casda.continuum(flag_c1);
CREATE INDEX idx_dec_dms_cont ON casda.continuum(dec_dms_cont);
CREATE INDEX idx_ra_hms_cont ON casda.continuum(ra_hms_cont);

COMMENT ON COLUMN casda.continuum.id is 'The primary key';  
COMMENT ON COLUMN casda.continuum.obj_id is 'Ident for objects.  Note that for EMU the values of OBJ_ID and CONT_ID are the same';
COMMENT ON COLUMN casda.continuum.obj_name is 'Object name.  Provisional field name - does EMU need a separate field name to other surveys [?].  Use standard name format. Prefix may vary. ';
COMMENT ON COLUMN casda.continuum.flux_int is 'Integrated flux density.  "i.e. Total flux density For given frequency [?]"';
COMMENT ON COLUMN casda.continuum.flux_int_err is 'Error in integrated flux density.  For given frequency [?]';
COMMENT ON COLUMN casda.continuum.flux_peak is 'Peak Flux Density.  For given frequency [?]';
COMMENT ON COLUMN casda.continuum.flux_peak_err is 'Error in peak flux density.  For given frequency [?]';
COMMENT ON COLUMN casda.continuum.spectral_index_1 is 'Spectral lindex.  First Taylor term';
COMMENT ON COLUMN casda.continuum.spectral_index_2 is 'Spectral lindex slope.  Second Taylor term';
COMMENT ON COLUMN casda.continuum.noise_rms is 'rms noise level in image.  measure of image quality';
COMMENT ON COLUMN casda.continuum.x_ave is 'average pixel position.  ';
COMMENT ON COLUMN casda.continuum.x_cen is 'pixel position of centroid.  ';
COMMENT ON COLUMN casda.continuum.x_max is 'max x-pix position.  ';
COMMENT ON COLUMN casda.continuum.x_min is 'min x-pix position.  ';
COMMENT ON COLUMN casda.continuum.x_peak is 'pixel position of peak.  ';
COMMENT ON COLUMN casda.continuum.y_ave is 'average pixel position .  ';
COMMENT ON COLUMN casda.continuum.y_cen is 'pixel position of centroid.  ';
COMMENT ON COLUMN casda.continuum.y_max is 'max y-pix position.  ';
COMMENT ON COLUMN casda.continuum.y_min is 'min y-pix position.  ';
COMMENT ON COLUMN casda.continuum.y_peak is 'pixel position of peak.  ';
COMMENT ON COLUMN casda.continuum.bmaj is 'Major axis size.  Major axis at FWHM';
COMMENT ON COLUMN casda.continuum.bmin is 'Minor axis size.  Minor axis at FWHM';
COMMENT ON COLUMN casda.continuum.pa is 'Position Angle of Gaussian component.  BPA';
COMMENT ON COLUMN casda.continuum.flag_c1 is 'Placeholder field for continuum flag [discuss all flag fields with MW].  Placeholder field for continuum flag [discuss all flag fields with MW]';
COMMENT ON COLUMN casda.continuum.flag_c2 is 'Placeholder field for continuum flag.  Placeholder field for continuum flag';
COMMENT ON COLUMN casda.continuum.flag_c3 is 'Placeholder field for continuum flag.  Placeholder field for continuum flag';
COMMENT ON COLUMN casda.continuum.flag_c4 is 'Placeholder field for continuum flag.  Placeholder field for continuum flag';
COMMENT ON COLUMN casda.continuum.dec_deg_cont is 'Error in DEC_DEG_CONT.  ';
COMMENT ON COLUMN casda.continuum.dec_deg_cont_err is 'Declination (Gaussian?).  Dec in decimal degrees; Method: Gaussian fit [check]; Precision: Float or double? [check]';
COMMENT ON COLUMN casda.continuum.dec_dms_cont is 'Declination .  Dec in degrees, arcmin, arcsec format; Method: Gaussian fit [check]';
COMMENT ON COLUMN casda.continuum.ra_deg_cont is 'Right Ascension (Gaussian?).  RA in decimal degrees; Method: Gaussian fit [check]; Precision: Float or double? [check]';
COMMENT ON COLUMN casda.continuum.ra_deg_cont_err is 'Error in RA_DEG_CONT.  ';
COMMENT ON COLUMN casda.continuum.ra_hms_cont is 'Right Ascension .  RA in hours, mins, secs format;  Method: Gaussian fit [check]';
COMMENT ON COLUMN casda.continuum.last_modified is 'When the row was last modified (usually via an insert)';
