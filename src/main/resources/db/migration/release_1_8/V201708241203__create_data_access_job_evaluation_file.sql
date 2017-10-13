-- Join table between data access job and catalogue, CASDA-4019

CREATE TABLE casda.data_access_job_evaluation_file (  
id                   BIGSERIAL PRIMARY KEY,
data_access_job_id   BIGINT references casda.data_access_job(id),
evaluation_file_id   BIGINT references casda.evaluation_file(id),
last_modified        TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE INDEX idx_access_job_evaluation_file_job ON casda.data_access_job_evaluation_file( data_access_job_id );
CREATE INDEX idx_access_job_evaluation_file_evaluation_file_id ON casda.data_access_job_evaluation_file( evaluation_file_id );


COMMENT ON TABLE casda.data_access_job_evaluation_file is 'Join table providing a relationship between Data Access Job and an Evaluation File';
COMMENT ON COLUMN casda.data_access_job_evaluation_file.id is 'The primary key';  
COMMENT ON COLUMN casda.data_access_job_evaluation_file.data_access_job_id is 'The foreign key into the Data Access Job table';
COMMENT ON COLUMN casda.data_access_job_evaluation_file.evaluation_file_id is 'The foreign key into the Evaluation File table';
COMMENT ON COLUMN casda.data_access_job_evaluation_file.last_modified is 'When the row was last modified (usually via an insert)';