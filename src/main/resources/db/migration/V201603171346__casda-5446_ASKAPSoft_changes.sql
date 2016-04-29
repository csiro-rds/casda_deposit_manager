update casda.tap_columns set column_name = 'has_siblings' where column_name = 'flag_c1' and table_name = 'casda.continuum_component';
update casda.tap_columns set column_name = 'fit_is_estimate' where column_name = 'flag_c2' and table_name = 'casda.continuum_component';
alter table casda.continuum_component rename flag_c1 to has_siblings;
alter table casda.continuum_component rename flag_c2 to fit_is_estimate;

COMMENT ON COLUMN casda.continuum_component.has_siblings IS 'Source has siblings';
COMMENT ON COLUMN casda.continuum_component.fit_is_estimate IS 'Component parameters are initial estimate, not from fit';
--ra_deg_cont & dec_deg_cont are already doubles in both tables