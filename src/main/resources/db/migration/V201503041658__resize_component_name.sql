-- Increase the max length of the component name to match the component table.

ALTER TABLE casda.polarisation_component ALTER COLUMN component_name TYPE character varying(32);
