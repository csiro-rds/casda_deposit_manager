INSERT INTO casda.tables (schema_name, table_name, table_type, db_schema_name, db_table_name)
  VALUES ('casda', 'casda.project', 'table', 'casda', 'project');
  
INSERT INTO casda.columns (column_order, column_name, table_name,description, datatype, principal, indexed, std, size)
  VALUES (1, 'id', 'casda.project','Primary key','BIGINT', 1,0,1, 19);
INSERT INTO casda.columns (column_order, column_name, table_name,description, datatype,principal, indexed, std, size)
  VALUES (2, 'opal_code', 'casda.project','OPAL ID of the project','VARCHAR', 1,0,1, 255);
INSERT INTO casda.columns (column_order, column_name, table_name,description, datatype,principal, indexed, std, size)
  VALUES (3, 'short_name', 'casda.project','The short name of the project','VARCHAR', 1,0,1, 255);

  
INSERT INTO casda.keys (key_id, from_table, target_table, description)
  VALUES ('project_continuum_component', 'casda.continuum_component', 'casda.project', 'Foreign key from project to continuum_component table');  
  
INSERT INTO casda.key_columns (id, key_id, from_column, target_column, from_table, target_table)
  VALUES ((SELECT max(id) FROM casda.key_columns)+1, 'project_continuum_component', 'project_id', 'id', 'casda.continuum_component', 'casda.project'
  );
  
INSERT INTO casda.keys (key_id, from_table, target_table, description)
  VALUES ('project_continuum_island', 'casda.continuum_island', 'casda.project', 'Foreign key from project to continuum_island table');  
  
INSERT INTO casda.key_columns (id, key_id, from_column, target_column, from_table, target_table)
  VALUES ((SELECT max(id) FROM casda.key_columns)+1,'project_continuum_island', 'project_id', 'id', 'casda.continuum_island', 'casda.project'
  );
  
INSERT INTO casda.keys (key_id, from_table, target_table, description)
  VALUES ('project_polarisation_component', 'casda.polarisation_component', 'casda.project', 'Foreign key from project to polarisation_component table');  
  
INSERT INTO casda.key_columns (id, key_id, from_column, target_column, from_table, target_table)
  VALUES ((SELECT max(id) FROM casda.key_columns)+1,'project_polarisation_component', 'project_id', 'id', 'casda.polarisation_component', 'casda.project'
  );
  
INSERT INTO casda.keys (key_id, from_table, target_table, description)
  VALUES ('project_catalogue', 'casda.catalogue', 'casda.project', 'Foreign key from project to catalogue table');  
  
INSERT INTO casda.key_columns (id, key_id, from_column, target_column, from_table, target_table)
  VALUES ((SELECT max(id) FROM casda.key_columns)+1,'project_catalogue', 'project_id', 'id', 'casda.catalogue', 'casda.project'
  );
