-- CASDA-5917 - Add new spectral image types

-- Note this will be run manually in prod

insert into casda.image_type values (45, 'spectral_psfnat_3d');
insert into casda.image_type values (46, 'spectral_psfprecon_3d');
insert into casda.image_type values (47, 'spectral_psfnat_4d');
insert into casda.image_type values (48, 'spectral_psfprecon_4d');
