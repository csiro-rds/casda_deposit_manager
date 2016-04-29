-- Add fields to the image_cube table to hold the metadata from the FITS files.
-- see CASDA-767 - Extract metadata for file-based data products

-- Note: nulls are allowed in all fields as these will be filled in after the initial row is created.  

ALTER TABLE casda.image_cube ADD COLUMN filesize BIGINT;
ALTER TABLE casda.image_cube ADD COLUMN target_name VARCHAR( 255 );
ALTER TABLE casda.image_cube ADD COLUMN ra_deg DOUBLE PRECISION;
ALTER TABLE casda.image_cube ADD COLUMN dec_deg DOUBLE PRECISION;
ALTER TABLE casda.image_cube ADD COLUMN s_fov DOUBLE PRECISION;
ALTER TABLE casda.image_cube ADD COLUMN s_region GEOMETRY;
ALTER TABLE casda.image_cube ADD COLUMN s_resolution DOUBLE PRECISION;
ALTER TABLE casda.image_cube ADD COLUMN t_min DOUBLE PRECISION;
ALTER TABLE casda.image_cube ADD COLUMN t_max DOUBLE PRECISION;
ALTER TABLE casda.image_cube ADD COLUMN t_exptime DOUBLE PRECISION;
ALTER TABLE casda.image_cube ADD COLUMN em_min DOUBLE PRECISION;
ALTER TABLE casda.image_cube ADD COLUMN em_max DOUBLE PRECISION;
ALTER TABLE casda.image_cube ADD COLUMN em_res_power DOUBLE PRECISION;
ALTER TABLE casda.image_cube ADD COLUMN polarisations VARCHAR( 255 );
ALTER TABLE casda.image_cube ADD COLUMN em_resolution DOUBLE PRECISION;
ALTER TABLE casda.image_cube ADD COLUMN s_resolution_min DOUBLE PRECISION;
ALTER TABLE casda.image_cube ADD COLUMN s_resolution_max DOUBLE PRECISION;

COMMENT ON COLUMN casda.image_cube.filesize is 'The size of the image cube in kilobytes';
COMMENT ON COLUMN casda.image_cube.target_name is 'Astronomical object observed, if any';
COMMENT ON COLUMN casda.image_cube.ra_deg is 'Central right ascension, ICRS, decimal degrees';
COMMENT ON COLUMN casda.image_cube.dec_deg is 'Central declination, ICRS, decimal degrees';
COMMENT ON COLUMN casda.image_cube.s_fov is 'Diameter of the image, in degrees';
COMMENT ON COLUMN casda.image_cube.s_region is 'Spatial region covered by the image.';
COMMENT ON COLUMN casda.image_cube.s_resolution is 'Spatial resolution of data as FWHM. Arcsecs.';
COMMENT ON COLUMN casda.image_cube.t_min is 'Image exposure Start Time/Date in Modified Julian Date format';
COMMENT ON COLUMN casda.image_cube.t_max is 'Image exposure Stop Time/Date in Modified Julian Date format';
COMMENT ON COLUMN casda.image_cube.t_exptime is 'Total exposure time in seconds';
COMMENT ON COLUMN casda.image_cube.em_min is 'Shortest wavelength observed, in metres.';
COMMENT ON COLUMN casda.image_cube.em_max is 'Longest wavelength observed, in metres.';
COMMENT ON COLUMN casda.image_cube.em_res_power is 'Spectral resolving power, which is not used in radio astronomy. Suggest em_resolution instead.';
COMMENT ON COLUMN casda.image_cube.polarisations is 'List of the polarisation states recorded.';
COMMENT ON COLUMN casda.image_cube.em_resolution is 'Value of Resolution along the spectral axis';
COMMENT ON COLUMN casda.image_cube.s_resolution_min is 'Resolution min value on spatial axis (FHWM of PSF) in arcsec';
COMMENT ON COLUMN casda.image_cube.s_resolution_max is 'Resolution max value on spatial axis (FHWM of PSF) in arcsec';


