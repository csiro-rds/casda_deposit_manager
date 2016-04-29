-- add tap entries for released date and quality level

INSERT INTO casda.tap_columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (34, 'ivoa.obscore', 'released_date', 'The date that this data product was released', 'TIMESTAMP', 24, NULL, NULL, NULL, 1, 1, 1, 3);
  
INSERT INTO casda.tap_columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (35, 'ivoa.obscore', 'quality_level', 'Indicator of quality level, updated by validators', 'VARCHAR', 15, NULL, NULL, 'meta.code.qual', 1, 1, 1, 3);
