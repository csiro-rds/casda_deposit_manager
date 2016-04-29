-- creating a new column for the data access job error message
ALTER TABLE casda.data_access_job ADD COLUMN error_message varchar(255);
COMMENT ON COLUMN casda.data_access_job.error_message is 'The user readable error message describing why the job failed.';
