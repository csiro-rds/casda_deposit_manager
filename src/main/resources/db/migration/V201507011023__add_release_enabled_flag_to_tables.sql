-- CASDA-4595 add release enabled flag to TAP tables and update data
ALTER TABLE casda.tables ADD COLUMN release_required BOOLEAN DEFAULT false;

COMMENT ON COLUMN casda.tables.release_required is 'Should unauthorised access to this table be restricted to released data, ie has a populated released_date column';

UPDATE casda.tables SET release_required = true
WHERE table_name in ('casda.catalogue', 'casda.continuum_component', 'casda.continuum_island', 'casda.polarisation_component')
