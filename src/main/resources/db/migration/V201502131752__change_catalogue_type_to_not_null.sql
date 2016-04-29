UPDATE casda.catalogue SET catalogue_type='continuum_component' WHERE catalogue_type IS NULL;

ALTER TABLE casda.catalogue ALTER COLUMN catalogue_type SET NOT NULL;
