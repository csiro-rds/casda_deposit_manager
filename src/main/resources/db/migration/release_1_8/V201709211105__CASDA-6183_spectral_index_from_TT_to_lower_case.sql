UPDATE casda.tap_columns SET column_name = 'spectral_index_from_tt', description = 'Spectral index and curvature from fit to Taylor term images' 
WHERE table_name = 'casda.continuum_component' and db_column_name = 'flag_c3';

UPDATE casda.tap_columns SET column_order = 32 
WHERE table_name = 'casda.continuum_island' and column_name = 'beam_area';