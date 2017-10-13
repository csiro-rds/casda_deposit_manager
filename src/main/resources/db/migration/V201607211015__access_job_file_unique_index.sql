-- CASDA-5433 - Duplicate access job file entries cause server crash
-- Ensure duplicates are not allowed 
ALTER TABLE casda.cached_file_data_access_jobs ADD CONSTRAINT unique_cached_file_da_job_row 
	UNIQUE (cached_file_id, data_access_jobs_id);

-- Switch back from big serial to big int by deleting the serial sequences.
DROP SEQUENCE casda.cached_file_data_access_jobs_cached_file_id_seq CASCADE;
DROP SEQUENCE casda.cached_file_data_access_jobs_data_access_jobs_id_seq CASCADE;
