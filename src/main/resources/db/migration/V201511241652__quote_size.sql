-- CASDA-5215 - Quote the size column in TAP_SCHEMA.columns as it is a reserved ADQL word.

update casda.tap_columns set column_name = '"size"' where column_name = 'size' and table_name = 'TAP_SCHEMA.columns';
