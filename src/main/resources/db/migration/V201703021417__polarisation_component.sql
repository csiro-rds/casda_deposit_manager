-- CASDA-5973 - Update polarisation and emission metadata to match catalogues coming in from ASKAPsoft

 
update casda.tap_columns set column_name = 'flag_is_detection' where table_name = 'casda.polarisation_component' and column_name = 'flag_p1'; 
update casda.tap_columns set column_name = 'flag_edge' where table_name = 'casda.polarisation_component' and column_name = 'flag_p2'; 

update casda.tap_columns set column_name = 'flux_I_median' where table_name = 'casda.polarisation_component' and column_name = 'flux_i_median';
update casda.tap_columns set column_name = 'flux_Q_median' where table_name = 'casda.polarisation_component' and column_name = 'flux_q_median';
update casda.tap_columns set column_name = 'flux_U_median' where table_name = 'casda.polarisation_component' and column_name = 'flux_u_median';
update casda.tap_columns set column_name = 'flux_V_median' where table_name = 'casda.polarisation_component' and column_name = 'flux_v_median';

update casda.tap_columns set column_name = 'rms_I' where table_name = 'casda.polarisation_component' and column_name = 'rms_i';
update casda.tap_columns set column_name = 'rms_Q' where table_name = 'casda.polarisation_component' and column_name = 'rms_q';
update casda.tap_columns set column_name = 'rms_U' where table_name = 'casda.polarisation_component' and column_name = 'rms_u';
update casda.tap_columns set column_name = 'rms_V' where table_name = 'casda.polarisation_component' and column_name = 'rms_v';

update casda.tap_columns set column_name = 'flux_voxel_stddev' where table_name = 'casda.spectral_line_emission' and column_name = 'flux_voxel_stdev';
update casda.tap_columns set db_column_name = 'flux_voxel_stddev' where table_name = 'casda.spectral_line_emission' and db_column_name = 'flux_voxel_stdev';


