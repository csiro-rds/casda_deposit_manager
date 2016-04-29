-- creating a new column for spatial region, using pg_sphere spherical polygon type SPOLY
-- data migration from the existing s_region column will be done manually and later the
-- s_region column will be removed. 
ALTER TABLE casda.image_cube ADD COLUMN s_region_poly SPOLY;
COMMENT ON COLUMN casda.image_cube.s_region_poly is 'Spatial region covered by the image.';