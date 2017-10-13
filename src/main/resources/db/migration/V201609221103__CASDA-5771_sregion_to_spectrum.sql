ALTER TABLE casda.spectrum ADD COLUMN s_region geometry;
ALTER TABLE casda.spectrum ADD COLUMN s_region_poly spoly;

COMMENT ON COLUMN casda.spectrum.s_region is 'Spatial region covered by the image.';
COMMENT ON COLUMN casda.spectrum.s_region_poly is 'Spatial region covered by the image.';