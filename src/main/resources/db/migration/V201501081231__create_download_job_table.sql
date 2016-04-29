-- Download job details, CASDA-785

CREATE TABLE casda.data_access_job (
    id                  BIGSERIAL PRIMARY KEY,
    request_id          VARCHAR(255),
    created_timestamp   TIMESTAMP,
    available_timestamp TIMESTAMP,
    expired_timestamp   TIMESTAMP,
    status              VARCHAR(15),
    size_kb             BIGINT,
    user_ident          VARCHAR(255), 
    user_login_system   VARCHAR(255),
    user_name           VARCHAR(255),
    user_email          VARCHAR(255),
    last_modified       TIMESTAMP DEFAULT now()
);

-- Create these indexes in lieu of them being primary keys
ALTER TABLE casda.data_access_job ADD CONSTRAINT unique_data_access_request_id UNIQUE (request_id);

CREATE INDEX idx_request_id ON casda.data_access_job(request_id);

COMMENT ON COLUMN casda.data_access_job.id is 'The primary key'; 
COMMENT ON COLUMN casda.data_access_job.request_id is 'The unique identifier for the request, used as a parameter in the url to access this job';
COMMENT ON COLUMN casda.data_access_job.created_timestamp is 'When the job was created';
COMMENT ON COLUMN casda.data_access_job.available_timestamp is 'When the files were ready for download by the user';
COMMENT ON COLUMN casda.data_access_job.expired_timestamp is 'When the job expired';
COMMENT ON COLUMN casda.data_access_job.status is 'The status of the job';
COMMENT ON COLUMN casda.data_access_job.size_kb is 'The size of the job in kilobytes';
COMMENT ON COLUMN casda.data_access_job.user_ident is 'The requester''s DAP ident';
COMMENT ON COLUMN casda.data_access_job.user_login_system is 'The requester''s login system in DAP';
COMMENT ON COLUMN casda.data_access_job.user_name is 'The requester''s full name';
COMMENT ON COLUMN casda.data_access_job.user_email is 'The requester''s email address';
COMMENT ON COLUMN casda.data_access_job.last_modified is 'When the row was last modified (usually via an insert)';