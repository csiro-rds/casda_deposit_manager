-- CASDA-4028
-- add details to track downloading the file from NGAS via the job manager.

-- add download_job_id
ALTER TABLE casda.cached_file ADD COLUMN download_job_id VARCHAR(255); 
-- add download_job_retry_count
ALTER TABLE casda.cached_file ADD COLUMN download_job_retry_count int;
-- add file_available_flag
ALTER TABLE casda.cached_file ADD COLUMN file_available_flag BOOLEAN;
-- add file_type
ALTER TABLE casda.cached_file ADD COLUMN file_type VARCHAR(50);

CREATE INDEX idx_cached_file_download_job_id ON casda.cached_file( download_job_id );
  
COMMENT ON COLUMN casda.cached_file.download_job_id is 'The job id for downloading the file from NGAS';  
COMMENT ON COLUMN casda.cached_file.download_job_retry_count is 'Number of attempted restarts of the download via the JobManager';  
COMMENT ON COLUMN casda.cached_file.file_available_flag is 'Flag to indicate whether the file is currently available in the cache';
COMMENT ON COLUMN casda.cached_file.file_type is 'Type of the file in the cache, eg CATALOGUE, IMAGE_CUBE, MEASUREMENT_SET';

-- set file_available_flag to true for all existing cached file records
UPDATE casda.cached_file set file_available_flag = TRUE;
-- update the value of the cached file type
UPDATE casda.cached_file set file_type = 'CATALOGUE' where path like '%Catalogue%';
UPDATE casda.cached_file set file_type = 'IMAGE_CUBE' where path like '%image_cubes%';
UPDATE casda.cached_file set file_type = 'MEASUREMENT_SET' where path like '%measurement_sets%';