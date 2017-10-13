-- CASDA-5973 - Update polarisation and emission catalogue tables to match catalogues coming in from ASKAPsoft

ALTER TABLE casda.polarisation_component ALTER COLUMN component_id TYPE character varying(256);

ALTER TABLE casda.spectral_line_absorption ALTER COLUMN object_id TYPE character varying(256);
ALTER TABLE casda.spectral_line_emission ALTER COLUMN object_id TYPE character varying(256);
ALTER TABLE casda.spectral_line_absorption ALTER COLUMN cont_component_id TYPE character varying(256);
