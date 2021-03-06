update casda.image_cube set image_type = 'Unknown';

delete from casda.image_type where type_name != 'Unknown';

insert into casda.image_type values (1, 'cont_restored_T0');
insert into casda.image_type values (2, 'cont_restored_T1');
insert into casda.image_type values (3, 'cont_restored_T2');
insert into casda.image_type values (4, 'cont_residual_T0');
insert into casda.image_type values (5, 'cont_residual_T1');
insert into casda.image_type values (6, 'cont_residual_T2');
insert into casda.image_type values (7, 'cont_cleanmodel_T0');
insert into casda.image_type values (8, 'cont_cleanmodel_T1');
insert into casda.image_type values (9, 'cont_cleanmodel_T2');
insert into casda.image_type values (10, 'cont_psfnat_T0');
insert into casda.image_type values (11, 'cont_psfnat_T1');
insert into casda.image_type values (12, 'cont_psfnat_T2');
insert into casda.image_type values (13, 'cont_psfprecon_T0');
insert into casda.image_type values (14, 'cont_psfprecon_T1');
insert into casda.image_type values (15, 'cont_psfprecon_T2');
insert into casda.image_type values (16, 'cont_sensitivity_T0');
insert into casda.image_type values (17, 'cont_senstivity_T1');
insert into casda.image_type values (18, 'cont_sensitivity_T2');
insert into casda.image_type values (19, 'cont_mask_T0');
insert into casda.image_type values (20, 'cont_restored_3d');
insert into casda.image_type values (21, 'cont_residual_3d');
insert into casda.image_type values (22, 'cont_cleanmodel_3d');
insert into casda.image_type values (23, 'cont_psfnat_3d');
insert into casda.image_type values (24, 'cont_psfprecon_3d');
insert into casda.image_type values (25, 'cont_sensitivity_3d');
insert into casda.image_type values (26, 'cont_restored_4d');
insert into casda.image_type values (27, 'cont_residual_4d');
insert into casda.image_type values (28, 'cont_cleanmodel_4d');
insert into casda.image_type values (29, 'cont_psfnat_4d');
insert into casda.image_type values (30, 'cont_psfprecon_4d');
insert into casda.image_type values (31, 'cont_sensitivity_4d');
insert into casda.image_type values (32, 'spectral_restored_3d');
insert into casda.image_type values (33, 'spectral_residual_3d');
insert into casda.image_type values (34, 'spectral_dirty_3d');
insert into casda.image_type values (35, 'spectral_cleanmodel_3d');
insert into casda.image_type values (36, 'spectral_mask_3d');
insert into casda.image_type values (37, 'spectral_restored_4d');
insert into casda.image_type values (38, 'spectral_residual_4d');
insert into casda.image_type values (39, 'spectral_dirty_4d');
insert into casda.image_type values (40, 'spectral_cleanmodel_4d');
insert into casda.image_type values (41, 'spectral_mask_4d');
insert into casda.image_type values (42, 'spectral_restored_mom0');
insert into casda.image_type values (43, 'spectral_restored_mom1');
insert into casda.image_type values (44, 'spectral_restored_mom2');
