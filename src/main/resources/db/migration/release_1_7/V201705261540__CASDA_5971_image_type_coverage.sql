-- Only some image types should be included when generating multi-order coverage maps (MOCs) and Hierarchical 
-- Progressive Survey HiPS map

ALTER TABLE casda.image_type ADD COLUMN include_coverage BOOLEAN default False NOT NULL;

COMMENT ON COLUMN casda.image_type.include_coverage IS 'Should images of this type be included when generating coverage maps.';

UPDATE casda.image_type SET include_coverage = True
WHERE type_name IN ('cont_restored_T0', 'cont_restored_T1', 'cont_restored_T2', 'cont_restored_3d', 'cont_restored_4d', 
		'spectral_restored_3d', 'spectral_restored_4d');
