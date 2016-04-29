-- CASDA-4968 Change image_cube stokes_parameter column to obscore's pol_states' format

UPDATE casda.image_cube SET stokes_parameters = REPLACE(stokes_parameters, 'UNDEFINED,', '') WHERE stokes_parameters like '%UNDEFINED,%';
UPDATE casda.image_cube SET stokes_parameters = REPLACE(stokes_parameters, ',UNDEFINED', '') WHERE stokes_parameters like '%,UNDEFINED%';
UPDATE casda.image_cube SET stokes_parameters = REPLACE(stokes_parameters, 'UNDEFINED', '') WHERE stokes_parameters like '%UNDEFINED%';
UPDATE casda.image_cube SET stokes_parameters = '/' || REPLACE(stokes_parameters, ',', '/') || '/' WHERE stokes_parameters is not null and stokes_parameters not like '/%/';
UPDATE casda.image_cube SET stokes_parameters = '//' WHERE stokes_parameters is null;
ALTER TABLE casda.image_cube ALTER COLUMN stokes_parameters SET NOT NULL;
ALTER TABLE casda.image_cube ALTER COLUMN stokes_parameters SET DEFAULT '//';

COMMENT ON COLUMN casda.image_cube.stokes_parameters is 'List of Stokes parameters (I, Q, U, and/or V) \
recorded in the image (values are delimited by ''/''s and are surrounded with a leading and trailing ''/'')';
