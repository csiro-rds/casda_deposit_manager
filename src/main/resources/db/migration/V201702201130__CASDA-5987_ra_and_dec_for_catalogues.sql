ALTER TABLE casda.catalogue ADD COLUMN ra_deg double precision;
ALTER TABLE casda.catalogue ADD COLUMN dec_deg double precision;
ALTER TABLE casda.catalogue ADD COLUMN s_region_poly SPOLY;

COMMENT ON COLUMN casda.moment_map.ra_deg IS 'Minimum right ascension, ICRS, decimal degrees';
COMMENT ON COLUMN casda.moment_map.dec_deg IS 'Maximum right ascension, ICRS, decimal degrees';
COMMENT ON COLUMN casda.image_cube.s_region_poly is 'Spatial region covered by the catalogue.';