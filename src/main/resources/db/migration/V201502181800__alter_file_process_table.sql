-- Changes required to modify FileProcess for use with JobManager

ALTER TABLE casda.file_process DROP COLUMN expires;
ALTER TABLE casda.file_process DROP COLUMN pid;
ALTER TABLE casda.file_process DROP COLUMN tag;
ALTER TABLE casda.file_process DROP COLUMN state;

ALTER TABLE casda.file_process ADD COLUMN job_id VARCHAR(255);
COMMENT ON COLUMN casda.file_process.job_id is 'The JobManager id of file processing job';  
