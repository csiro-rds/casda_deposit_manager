ALTER TABLE casda.catalogue ADD COLUMN freq_ref REAL;
ALTER TABLE casda.catalogue ADD COLUMN image_id BIGINT;
ALTER TABLE casda.catalogue ADD COLUMN time_obs TIMESTAMP;
ALTER TABLE casda.catalogue ADD COLUMN time_obs_mjd DOUBLE PRECISION;

ALTER TABLE casda.catalogue ADD FOREIGN KEY (image_id) REFERENCES casda.image_cube(id);

COMMENT ON COLUMN casda.catalogue.image_id is 'identifies the image that the detection came from.';
COMMENT ON COLUMN casda.catalogue.freq_ref is 'Frequency at which fluxes are determined.';
COMMENT ON COLUMN casda.catalogue.time_obs_mjd is 'Modified Julian Date.  At time of observation.';
COMMENT ON COLUMN casda.catalogue.time_obs is 'Time of observation.  Full UT time, including seconds. .';
