-- CASDA-4968 Remove redundant polarisation columns

ALTER TABLE casda.image_cube DROP COLUMN polarisations;
ALTER TABLE casda.image_cube DROP COLUMN polarisation_states;
