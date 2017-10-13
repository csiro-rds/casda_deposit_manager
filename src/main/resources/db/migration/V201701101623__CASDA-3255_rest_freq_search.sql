ALTER TABLE casda.spectrum ADD COLUMN rest_frequency DOUBLE PRECISION;
ALTER TABLE casda.moment_map ADD COLUMN rest_frequency DOUBLE PRECISION;
ALTER TABLE casda.image_cube ADD COLUMN rest_frequency DOUBLE PRECISION;

COMMENT ON COLUMN casda.image_cube.rest_frequency is 'The rest frequnecy of the image cube';
COMMENT ON COLUMN casda.spectrum.rest_frequency is 'The rest frequnecy of the spectrum';
COMMENT ON COLUMN casda.moment_map.rest_frequency is 'The rest frequnecy of the moment map';