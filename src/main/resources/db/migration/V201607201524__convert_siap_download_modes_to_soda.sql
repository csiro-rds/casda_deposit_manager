update casda.data_access_job set download_mode = 'SODA_SYNC' where download_mode = 'SIAP_SYNC';
update casda.data_access_job set download_mode = 'SODA_ASYNC' where download_mode = 'SIAP_ASYNC';
