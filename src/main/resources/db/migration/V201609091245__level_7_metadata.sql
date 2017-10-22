--     CASDA-5811 - Additional VO metadata for versioned level 7 catalogues

-- Update TAP metadata and level 7 linkages to reflect versioning of level 7 catalogue tables. 

-- Update the catalogue linkage
update casda.catalogue 
set entries_table_name = entries_table_name || '_v01'
where entries_table_name is not null
 and entries_table_name not similar to '%_v(0|1|2|3|4|5|6|7|8|9)+';


-- Update the level 7 collection entry to link to the common id
update casda.level7_collection set dc_common_id = dap_collection_id;


-- Then update existing level 7 table registration to include new version metadata and to refer to versioned table names 
update casda.tap_tables 
set db_table_name = db_table_name || '_v01', params = regexp_replace(params, '$', ' | This version : v1 | Latest version : v1')
where schema_name not in ('casda', 'ivoa', 'TAP_SCHEMA', 'public');

