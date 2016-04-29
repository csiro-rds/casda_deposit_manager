-- CASDA-4487 add table name column to catalogue, for level 7 collection
ALTER TABLE casda.catalogue ADD COLUMN entries_table_name VARCHAR(255);
COMMENT ON COLUMN casda.catalogue.entries_table_name is 'The qualified database table name for a level 7 catalogue''s entries';

ALTER TABLE casda.catalogue ADD CONSTRAINT unique_entries_table_name UNIQUE (entries_table_name);