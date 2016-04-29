-- CASDA-4595 add released date to tables representing catalogue rows

ALTER TABLE casda.continuum_component ADD COLUMN released_date timestamp with time zone;
ALTER TABLE casda.polarisation_component ADD COLUMN released_date timestamp with time zone;
ALTER TABLE casda.continuum_island ADD COLUMN released_date timestamp with time zone;

COMMENT ON COLUMN casda.continuum_component.released_date is 'The date that the catalogue data product was released';
COMMENT ON COLUMN casda.polarisation_component.released_date is 'The date that the catalogue data product was released';
COMMENT ON COLUMN casda.continuum_island.released_date is 'The date that the parent catalogue data product was released';

-- update the data in existing tables
UPDATE casda.continuum_component 
  SET released_date = (SELECT c.released_date FROM casda.catalogue c WHERE c.id = catalogue_id);
  
UPDATE casda.continuum_island 
  SET released_date = (SELECT c.released_date FROM casda.catalogue c WHERE c.id = catalogue_id);
  
UPDATE casda.polarisation_component 
  SET released_date = (SELECT c.released_date FROM casda.catalogue c WHERE c.id = catalogue_id);