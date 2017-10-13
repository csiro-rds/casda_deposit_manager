-- CASDA-5806 - Deposit new Lvl 7 catalogue version

ALTER TABLE casda.level7_collection ADD COLUMN dc_common_id INT;
COMMENT ON COLUMN casda.level7_collection.dc_common_id is 'The base collection id shared by all versions of a particular data collection.';

-- Add a flag to track which verison of a catalogue is current
ALTER TABLE casda.catalogue ADD COLUMN level7_active BOOLEAN;
COMMENT ON COLUMN casda.catalogue.level7_active is 'Is this level 7 catalogue the current or active version of the catalogue.';

update casda.catalogue set level7_active = 't' where level7_collection_id is not null;

