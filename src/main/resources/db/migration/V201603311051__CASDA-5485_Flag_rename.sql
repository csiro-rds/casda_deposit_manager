alter table casda.continuum_island rename flag_c1 to flag_i1;
alter table casda.continuum_island rename flag_c2 to flag_i2;
alter table casda.continuum_island rename flag_c3 to flag_i3;
alter table casda.continuum_island rename flag_c4 to flag_i4;

update casda.tap_columns set column_name = 'flag_i1' where column_name = 'flag_c1' and table_name = 'casda.continuum_island';
update casda.tap_columns set column_name = 'flag_i2' where column_name = 'flag_c2' and table_name = 'casda.continuum_island';
update casda.tap_columns set column_name = 'flag_i3' where column_name = 'flag_c3' and table_name = 'casda.continuum_island';
update casda.tap_columns set column_name = 'flag_i4' where column_name = 'flag_c4' and table_name = 'casda.continuum_island';