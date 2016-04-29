-- creating a new column for the data access job params
ALTER TABLE casda.data_access_job ADD COLUMN params TEXT;
COMMENT ON COLUMN casda.data_access_job.params is 'The job parameters requested by the user';