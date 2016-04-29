ALTER TABLE casda.catalogue ADD COLUMN released_date timestamp with time zone;
ALTER TABLE casda.image_cube ADD COLUMN released_date timestamp with time zone;
ALTER TABLE casda.measurement_set ADD COLUMN released_date timestamp with time zone;

COMMENT ON COLUMN casda.catalogue.released_date is 'The date that the catalogue data product was released';
COMMENT ON COLUMN casda.image_cube.released_date is 'The date that the image_cube data product was released';
COMMENT ON COLUMN casda.measurement_set.released_date is 'The date that the measurement_set data product was released';

ALTER TABLE casda.evaluation_file DROP COLUMN quality_level;