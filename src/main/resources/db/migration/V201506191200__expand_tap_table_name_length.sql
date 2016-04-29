-- CASDA-4547 - Expand Obscore field definitions and sorage to cope wiht tables up to 63 character long

update casda.columns set size = 70 where column_name = 'table_name' and table_name IN ('TAP_SCHEMA.tables', 'TAP_SCHEMA.columns');
update casda.columns set size = 70 where column_name IN ('from_table', 'target_table') and table_name = 'TAP_SCHEMA.keys';

-- Expand all table fields from 64 to 70 character to alow for a maximum length table name with the casda. prefix included 
ALTER TABLE casda.tables ALTER COLUMN table_name TYPE character varying(70);
ALTER TABLE casda.columns ALTER COLUMN table_name TYPE character varying(70);
ALTER TABLE casda.keys ALTER COLUMN from_table TYPE character varying(70);
ALTER TABLE casda.keys ALTER COLUMN target_table TYPE character varying(70);
ALTER TABLE casda.key_columns ALTER COLUMN from_table TYPE character varying(70);
ALTER TABLE casda.key_columns ALTER COLUMN target_table TYPE character varying(70);

