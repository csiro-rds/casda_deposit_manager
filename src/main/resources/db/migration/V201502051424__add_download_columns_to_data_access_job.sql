-- Add columns about download mode and format to data access job, CASDA-3839

ALTER TABLE casda.data_access_job ADD COLUMN download_mode VARCHAR(30);
COMMENT ON COLUMN casda.data_access_job.download_mode is 'Requested download mode, eg Web';

ALTER TABLE casda.data_access_job ADD COLUMN download_format VARCHAR(30);
COMMENT ON COLUMN casda.data_access_job.download_format is 'Requested download format, eg CSV_INDIVIDUAL, CSV_GROUP';
