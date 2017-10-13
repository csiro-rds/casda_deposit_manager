-- CASDA-5667 - Correct typo in image type name. 
-- This script assumes no data has been assigned the erroneous name yet.

update casda.image_type set type_name = 'cont_sensitivity_T1' where type_name = 'cont_senstivity_T1';
