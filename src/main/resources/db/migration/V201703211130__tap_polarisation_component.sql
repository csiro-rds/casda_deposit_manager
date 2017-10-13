-- CASDA-5973 - Update polarisation and emission metadata to match catalogues coming in from ASKAPsoft
 
update casda.tap_columns set datatype = 'INTEGER', size = 15
where table_name = 'casda.polarisation_component' 
  and column_name in ('flag_is_detection', 'flag_edge', 'flag_p3', 'flag_p4');
  