--changed default value for t-mix & t_max as part of CASDA-4970
update casda.image_cube set t_min = null where t_min = 0;
update casda.image_cube set t_max = null where t_max = 0;
--CASDA-4999
update casda.image_cube set t_exptime = integration_time;