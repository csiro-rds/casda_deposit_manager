--step 1 create table
CREATE TABLE casda.spectrum_type (
	id 				  int PRIMARY KEY,
	type_name         VARCHAR( 100 ) unique
);

COMMENT ON COLUMN casda.spectrum_type.id is 'The primary key of the spectrum type'; 
COMMENT ON COLUMN casda.spectrum_type.type_name is 'The name of the type of spectrum contained in the FITS file'; 

--step2 populate table
insert into casda.spectrum_type values (1, 'spectral_peak_restored');
insert into casda.spectrum_type values (2, 'spectral_integrated_restored');
insert into casda.spectrum_type values (3, 'spectral_noise_restored');
insert into casda.spectrum_type values (4, 'cont_restored_i');
insert into casda.spectrum_type values (5, 'cont_restored_q');
insert into casda.spectrum_type values (6, 'cont_restored_u');
insert into casda.spectrum_type values (7, 'cont_restored_v');
insert into casda.spectrum_type values (8, 'cont_restored_p');
insert into casda.spectrum_type values (9, 'cont_noise_i');
insert into casda.spectrum_type values (10, 'cont_noise_q');
insert into casda.spectrum_type values (11, 'cont_noise_u');
insert into casda.spectrum_type values (12, 'cont_noise_v');
insert into casda.spectrum_type values (13, 'cont_noise_qu');
insert into casda.spectrum_type values (14, 'cont_model_i');
insert into casda.spectrum_type values (15, 'cont_fdf');
insert into casda.spectrum_type values (16, 'cont_rmsf');

--TODO step 3 this table will be joined to the new spectra table later.