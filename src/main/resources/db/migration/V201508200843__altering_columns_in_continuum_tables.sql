update casda.columns set unit = null where column_name = 'ra_hms_cont' and table_name = 'casda.continuum_component';
update casda.columns set unit = null where column_name = 'dec_dms_cont' and table_name = 'casda.continuum_component';
update casda.columns set unit = null where column_name = 'ra_hms_cont' and table_name = 'casda.continuum_island';
update casda.columns set unit = null where column_name = 'dec_dms_cont' and table_name = 'casda.continuum_island';