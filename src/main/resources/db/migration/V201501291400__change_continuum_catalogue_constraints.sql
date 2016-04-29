ALTER TABLE casda.continuum_component DROP CONSTRAINT IF EXISTS unique_continuum_component_name;

ALTER TABLE casda.continuum_component ADD CONSTRAINT unique_continuum_component_id UNIQUE (component_id);

