-- changes required by new metadata spec, see CASDA-3937

-- elements in the obscore view depend on the measurement_set table, so need to drop it
-- and then it will be recreated with another migration
DROP VIEW IF EXISTS casda.obscore;

ALTER TABLE casda.measurement_set DROP COLUMN scan_start;
ALTER TABLE casda.measurement_set DROP COLUMN scan_end;
ALTER TABLE casda.measurement_set DROP COLUMN field_centre_x;
ALTER TABLE casda.measurement_set DROP COLUMN field_centre_y;
ALTER TABLE casda.measurement_set DROP COLUMN coord_system;
ALTER TABLE casda.measurement_set DROP COLUMN field_name;
ALTER TABLE casda.measurement_set DROP COLUMN polarisations;
ALTER TABLE casda.measurement_set DROP COLUMN num_chan;
ALTER TABLE casda.measurement_set DROP COLUMN centre_freq;
ALTER TABLE casda.measurement_set DROP COLUMN centre_width;
ALTER TABLE casda.measurement_set DROP COLUMN wavelength_min;
ALTER TABLE casda.measurement_set DROP COLUMN wavelength_max;
