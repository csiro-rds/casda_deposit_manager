--step 1 create table
CREATE TABLE casda.image_type (
	id 				  int PRIMARY KEY,
	type_name         VARCHAR( 100 ) unique
);

COMMENT ON COLUMN casda.image_type.id is 'The primary key of the image type'; 
COMMENT ON COLUMN casda.image_type.type_name is 'The name of the type of image contained in the FITS file'; 

--step2 populate table
insert into casda.image_type values (0, 'Unknown');
insert into casda.image_type values (1, 'Continuum - restored');
insert into casda.image_type values (2, 'Continuum - residual');
insert into casda.image_type values (3, 'Continuum - model');
insert into casda.image_type values (4, 'Continuum - point spread function');
insert into casda.image_type values (5, 'Continuum - sensitivity');
insert into casda.image_type values (6, 'Continuum - mask');
insert into casda.image_type values (7, 'Polarisation - restored');
insert into casda.image_type values (8, 'Polarisation - residual');
insert into casda.image_type values (9, 'Polarisation - model');
insert into casda.image_type values (10, 'Polarisation - point spread function');
insert into casda.image_type values (11, 'Polarisation - sensitivity');
insert into casda.image_type values (12, 'Polarisation - mask');
insert into casda.image_type values (13, 'Polarisation - spectra');
insert into casda.image_type values (14, 'Spectral line - restored');
insert into casda.image_type values (15, 'Spectral line - residual');
insert into casda.image_type values (16, 'Spectral line - uncleaned');
insert into casda.image_type values (17, 'Spectral line - model');
insert into casda.image_type values (18, 'Spectral line - mask');
insert into casda.image_type values (19, 'Spectral line - postage stamp');
insert into casda.image_type values (20, 'Spectral line - moment map');
insert into casda.image_type values (21,'Spectral line - spectra');

--Step3 alter image_cube.image_type to be foreign key to type_code
alter table casda.image_cube add constraint imtypefk foreign key (image_type) references casda.image_type (type_name);