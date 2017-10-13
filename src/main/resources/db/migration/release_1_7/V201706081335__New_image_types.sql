-- From demo: T1 and T2 images do not need to be part of the coverage map 
-- as the T0 will have the same footprint
UPDATE casda.image_type SET include_coverage = False
WHERE type_name IN ('cont_restored_T1', 'cont_restored_T2');


-- CASDA-6125 - Add new image types
insert into casda.image_type values (49,'cont_noise_T0', False);
insert into casda.image_type values (50,'cont_noise_3d', False);
insert into casda.image_type values (51,'cont_noise_4d', False);
insert into casda.image_type values (52,'spectral_noise_3d', False);
insert into casda.image_type values (53,'spectral_noise_4d', False);
insert into casda.image_type values (54,'spectral_noise_2d', False);
insert into casda.image_type values (55,'spectral_sensitivity_3d', False);
insert into casda.image_type values (56,'spectral_sensitivity_4d', False);
insert into casda.image_type values (57,'cont_weight_T0', False);
insert into casda.image_type values (58,'cont_weight_T1', False);
insert into casda.image_type values (59,'cont_weight_T2', False);
insert into casda.image_type values (60,'cont_weight_3d', False);
insert into casda.image_type values (61,'cont_weight_4d', False);
insert into casda.image_type values (62,'spectral_weight_3d', False);
insert into casda.image_type values (63,'spectral_weight_4d', False);

-- New image types from Matt
insert into casda.image_type values (64,'cont_components_T0', False);
insert into casda.image_type values (65,'cont_fitresidual_T0', False);


-- New set of quality flags (validation checkboxes) provided by the ACES team
update casda.quality_flag set active = FALSE, last_modified = current_timestamp;
insert into casda.quality_flag (display_order, code, label, active) values (1, 'INT_MOS', 'Interleaved mosaic', TRUE);
insert into casda.quality_flag (display_order, code, label, active) values (2, 'EXT_POS_REF', 'External position reference', TRUE);
insert into casda.quality_flag (display_order, code, label, active) values (3, 'BASE_FLAG', 'Baselines flagged', TRUE);
insert into casda.quality_flag (display_order, code, label, active) values (4, 'BEAM_FLAG', 'Beams flagged', TRUE);
insert into casda.quality_flag (display_order, code, label, active) values (5, 'TRUNC', 'Observation truncated', TRUE);
insert into casda.quality_flag (display_order, code, label, active) values (6, 'RFI_STR', 'Strong RFI', TRUE);
insert into casda.quality_flag (display_order, code, label, active) values (7, 'SRC_STR', 'Strong sources', TRUE);
insert into casda.quality_flag (display_order, code, label, active) values (8, 'EXT_SRC', 'Extended sources', TRUE);
insert into casda.quality_flag (display_order, code, label, active) values (9, 'NO_SI', 'No spectral indices', TRUE);