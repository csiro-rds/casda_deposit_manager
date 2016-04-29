update casda.tap_tables set description = 'HI absorption source detection catalogue' where table_name = 'casda.spectral_line_absorption';
update casda.tap_tables set description = 'HI emission source detection catalogue' where table_name = 'casda.spectral_line_emission';

update casda.tap_columns set datatype = 'SHORT' where column_name in ('flag_s1', 'flag_s2', 'flag_s3') and table_name in ('casda.spectral_line_absorption', 'casda.spectral_line_emission');