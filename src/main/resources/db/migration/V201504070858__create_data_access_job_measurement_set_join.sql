-- Join table between data access job and catalogue, CASDA-4019

CREATE TABLE casda.data_access_job_measurement_set (  
id                   BIGSERIAL PRIMARY KEY,
data_access_job_id   BIGINT references casda.data_access_job(id),
measurement_set_id   BIGINT references casda.measurement_set(id),
last_modified        TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE INDEX idx_access_job_measurement_set_job ON casda.data_access_job_measurement_set( data_access_job_id );
CREATE INDEX idx_access_job_measurement_set_measurement_set ON casda.data_access_job_measurement_set( measurement_set_id );
  
COMMENT ON TABLE casda.data_access_job_measurement_set is 'Join table providing a relationship between Data Access Job and Measurement Set';

COMMENT ON COLUMN casda.data_access_job_measurement_set.id is 'The primary key';  
COMMENT ON COLUMN casda.data_access_job_measurement_set.data_access_job_id is 'The foreign key into the Data Access Job table';
COMMENT ON COLUMN casda.data_access_job_measurement_set.measurement_set_id is 'The foreign key into the Measurement Set table';
COMMENT ON COLUMN casda.data_access_job_measurement_set.last_modified is 'When the row was last modified (usually via an insert)';