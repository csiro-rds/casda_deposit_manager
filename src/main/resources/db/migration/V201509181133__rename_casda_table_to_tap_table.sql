-- Rename key_columns to tap_key_columns
ALTER TABLE casda.key_columns RENAME TO tap_key_columns;

-- Rename keys to tap_keys
ALTER TABLE casda.keys RENAME TO tap_keys;

-- Rename columns to tap_columns
ALTER TABLE casda.columns RENAME TO tap_columns;

-- Rename tables to tap_tables
ALTER TABLE casda.tables RENAME TO tap_tables;

-- Rename schemas to tap_schemas
ALTER TABLE casda.schemas RENAME TO tap_schemas;

-- Migrate the TAP metadata to point to the new names 
update casda.tap_tables set db_table_name = 'tap_key_columns' where db_table_name = 'key_columns';
update casda.tap_tables set db_table_name = 'tap_keys' where db_table_name = 'keys';
update casda.tap_tables set db_table_name = 'tap_columns' where db_table_name = 'columns';
update casda.tap_tables set db_table_name = 'tap_tables' where db_table_name = 'tables';
update casda.tap_tables set db_table_name = 'tap_schemas' where db_table_name = 'schemas';