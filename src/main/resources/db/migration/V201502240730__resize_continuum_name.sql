-- Increase the max length of the component name to allow the full ATLAS name to be stored

ALTER TABLE casda.continuum_component ALTER COLUMN component_name TYPE character varying(32);
