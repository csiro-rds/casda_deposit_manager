-- CASDA-6013 - Update FITS based tables to allow the record to be linked to either an observation or a level 7 collection

ALTER TABLE casda.image_cube ALTER COLUMN observation_id DROP NOT NULL;
ALTER TABLE casda.spectrum ALTER COLUMN observation_id DROP NOT NULL;
ALTER TABLE casda.spectrum ALTER COLUMN image_cube_id DROP NOT NULL;
ALTER TABLE casda.moment_map ALTER COLUMN observation_id DROP NOT NULL;
ALTER TABLE casda.moment_map ALTER COLUMN image_cube_id DROP NOT NULL;
ALTER TABLE casda.thumbnail ALTER COLUMN observation_id DROP NOT NULL;
ALTER TABLE casda.encapsulation_file ALTER COLUMN observation_id DROP NOT NULL;


insert into casda.spectrum_type values (0, 'Unknown');
