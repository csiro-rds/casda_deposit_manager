-- Increase the max length of the island and component ids

ALTER TABLE casda.continuum_component ALTER COLUMN island_id TYPE character varying(255);
ALTER TABLE casda.continuum_component ALTER COLUMN component_id TYPE character varying(256);

ALTER TABLE casda.continuum_island ALTER COLUMN island_id TYPE character varying(255);
