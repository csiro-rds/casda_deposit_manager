-- CASDA-4968 Remove not null constraint from image_cube stokes_parameters

ALTER TABLE casda.image_cube ALTER COLUMN stokes_parameters DROP NOT NULL;
