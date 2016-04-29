--Add db_column_name column to tap_columns and copy columns names to db_column_name column
ALTER TABLE casda.tap_columns ADD COLUMN db_column_name VARCHAR(64) NOT NULL DEFAULT('');
COMMENT ON COLUMN casda.tap_columns.db_column_name IS 'The database column name by which this table must be referenced in SQL.';
COMMENT ON COLUMN casda.tap_columns.column_name IS 'The column name as it should be used in queries.  Part of or same as primary key';


UPDATE casda.tap_columns SET db_column_name = column_name;

--Add other_sbids column to catalougue tables
ALTER TABLE casda.spectral_line_emission ADD COLUMN other_sbids VARCHAR(500);
ALTER TABLE casda.continuum_component ADD COLUMN other_sbids VARCHAR(500);
ALTER TABLE casda.continuum_island ADD COLUMN other_sbids VARCHAR(500);
ALTER TABLE casda.polarisation_component ADD COLUMN other_sbids VARCHAR(500);
ALTER TABLE casda.spectral_line_absorption ADD COLUMN other_sbids VARCHAR(500);
COMMENT ON COLUMN casda.spectral_line_emission.other_sbids IS 'Ids of other schedulings blocks from which this dataproduct had been originated.';
COMMENT ON COLUMN casda.continuum_component.other_sbids IS 'Ids of other schedulings blocks from which this dataproduct had been originated.';
COMMENT ON COLUMN casda.continuum_island.other_sbids IS 'Ids of other schedulings blocks from which this dataproduct have had originated.';
COMMENT ON COLUMN casda.polarisation_component.other_sbids IS 'Ids of other schedulings blocks from which this dataproduct had been originated.';
COMMENT ON COLUMN casda.spectral_line_absorption.other_sbids IS 'Ids of other schedulings blocks from which this dataproduct had been originated.';

--Update sbid column comment as its is now the First SBID
COMMENT ON COLUMN casda.spectral_line_emission.sbid IS 'The id of the first scheduling block from which this data product had been originated.';
COMMENT ON COLUMN casda.continuum_component.sbid IS 'The id of the first scheduling block from which this data product had been originated.';
COMMENT ON COLUMN casda.continuum_island.sbid IS 'The id of the first scheduling block from which this data product had been originated.';
COMMENT ON COLUMN casda.polarisation_component.sbid IS 'The id of the first scheduling block from which this data product had been originated.';
COMMENT ON COLUMN casda.spectral_line_absorption.sbid IS 'The id of the first scheduling block from which this data product had been originated.';


--update newly added other_sbids column to tap
INSERT INTO casda.tap_columns (column_order, table_name, column_name, db_column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (3, 'casda.spectral_line_emission', 'other_sbids', 'other_sbids','Other schedulings blocks from which this data product originated', 'VARCHAR', 500, NULL, NULL, NULL, 1, 1, 1, 3);    
INSERT INTO casda.tap_columns (column_order, table_name, column_name, db_column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (3, 'casda.continuum_component', 'other_sbids', 'other_sbids', 'Other schedulings blocks from which this data product originated', 'VARCHAR', 500, NULL, NULL, NULL, 1, 1, 1, 3);  
INSERT INTO casda.tap_columns (column_order, table_name, column_name, db_column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (3, 'casda.continuum_island', 'other_sbids', 'other_sbids', 'Other schedulings blocks from which this data product originated', 'VARCHAR', 500, NULL, NULL, NULL, 1, 1, 1, 3);  
INSERT INTO casda.tap_columns (column_order, table_name, column_name, db_column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (3, 'casda.polarisation_component', 'other_sbids', 'other_sbids', 'Other schedulings blocks from which this data product originated', 'VARCHAR', 500, NULL, NULL, NULL, 1, 1, 1, 3);  
INSERT INTO casda.tap_columns (column_order, table_name, column_name, db_column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (3, 'casda.spectral_line_absorption', 'other_sbids', 'other_sbids', 'other Scheduling Block IDs', 'VARCHAR', 500, NULL, NULL, NULL, 1, 1, 1, 3);
  
--Update db_column_name for the sbod in tap
 UPDATE casda.tap_columns set column_name = 'first_sbid' where db_column_name = 'sbid';