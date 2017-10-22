ALTER TABLE casda.image_cube ADD COLUMN b_unit CHARACTER VARYING(255);
ALTER TABLE casda.image_cube ADD COLUMN b_type CHARACTER VARYING(255);
COMMENT ON COLUMN casda.image_cube.b_unit IS 'The physical units in which the quantities in the array, after application of BSCALE and BZERO, are expressed.';
COMMENT ON COLUMN casda.image_cube.b_type IS 'The type of beam';

ALTER TABLE casda.spectrum ADD COLUMN b_unit CHARACTER VARYING(255);
ALTER TABLE casda.spectrum ADD COLUMN b_type CHARACTER VARYING(255);
COMMENT ON COLUMN casda.spectrum.b_unit IS 'The physical units in which the quantities in the array, after application of BSCALE and BZERO, are expressed.';
COMMENT ON COLUMN casda.spectrum.b_type IS 'The type of beam';

ALTER TABLE casda.moment_map ADD COLUMN b_unit CHARACTER VARYING(255);
ALTER TABLE casda.moment_map ADD COLUMN b_type CHARACTER VARYING(255);
COMMENT ON COLUMN casda.moment_map.b_unit IS 'The physical units in which the quantities in the array, after application of BSCALE and BZERO, are expressed.';
COMMENT ON COLUMN casda.moment_map.b_type IS 'The type of beam';