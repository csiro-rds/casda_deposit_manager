-- Cached Files table and table to store job ids that are using the cached files CASDA-792

CREATE TABLE casda.cached_file (
    id                  BIGSERIAL PRIMARY KEY,
    file_id             VARCHAR(255),
    path             	VARCHAR(255),
    unlock   			TIMESTAMP,
    size_kb                BIGINT,
    last_modified       TIMESTAMP DEFAULT now()
);

CREATE TABLE casda.cached_file_data_access_jobs (  
	cached_file_id      BIGSERIAL,
	data_access_jobs_id BIGSERIAL
);

CREATE INDEX idx_cached_file_unlock ON casda.cached_file( unlock );
CREATE INDEX idx_cached_file_file_id ON casda.cached_file( file_id );
CREATE INDEX idx_cached_file_data_access_jobs_file_id ON casda.cached_file_data_access_jobs( cached_file_id );
CREATE INDEX idx_cached_file_data_access_jobs_job_id ON casda.cached_file_data_access_jobs( data_access_jobs_id );
  
COMMENT ON TABLE casda.cached_file is 'Database store of files currently availble in the filesystem cache';
COMMENT ON TABLE casda.cached_file_data_access_jobs is 'Download jobs using the cached file';

COMMENT ON COLUMN casda.cached_file.size_kb is 'The size in kb of the cached file';  
COMMENT ON COLUMN casda.cached_file.file_id is 'The unique id of the file used to retrieve from the archive';  
COMMENT ON COLUMN casda.cached_file.path is 'The path to the file in the filesystem cache';  
COMMENT ON COLUMN casda.cached_file.unlock is 'The time when the cached file is no longer needed';  

