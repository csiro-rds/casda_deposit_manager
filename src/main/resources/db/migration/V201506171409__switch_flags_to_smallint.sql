-- CASDA-4522 - smallint database fields are now expected to have a TAP type of SMALLINT, not INTEGER

update columns set datatype = 'SMALLINT' where column_name like 'flag_c%' and datatype = 'INTEGER';