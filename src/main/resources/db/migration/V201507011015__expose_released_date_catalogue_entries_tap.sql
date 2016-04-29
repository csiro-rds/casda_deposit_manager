-- CASDA-4595 expose released date column for catalogue entries in TAP
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (39, 'released_date', 'casda.continuum_component', 'TIMESTAMP', 24, 1, 0, 1, 'Date the catalogue was released', null, 3);
  
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (35, 'released_date', 'casda.continuum_island', 'TIMESTAMP', 24, 1, 0, 1, 'Date the catalogue was released', null, 3);
  
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (49, 'released_date', 'casda.polarisation_component', 'TIMESTAMP', 24, 1, 0, 1, 'Date the catalogue was released', null, 3);