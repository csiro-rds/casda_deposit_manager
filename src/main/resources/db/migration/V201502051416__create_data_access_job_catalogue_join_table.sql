-- Join table between data access job and catalogue, CASDA-3839

CREATE TABLE casda.data_access_job_catalogue (  
id                   BIGSERIAL PRIMARY KEY,
data_access_job_id   BIGINT references casda.data_access_job(id),
catalogue_id         BIGINT references casda.catalogue(id),
last_modified        TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE INDEX idx_access_job_catalogue_job ON casda.data_access_job_catalogue( data_access_job_id );
CREATE INDEX idx_access_job_catalogue_catalogue ON casda.data_access_job_catalogue( catalogue_id );
  
COMMENT ON TABLE casda.data_access_job_image_cube is 'Join table providing a relationship between Data Access Job and Catalogue';

COMMENT ON COLUMN casda.data_access_job_catalogue.id is 'The primary key';  
COMMENT ON COLUMN casda.data_access_job_catalogue.data_access_job_id is 'The foreign key into the Data Access Job table';
COMMENT ON COLUMN casda.data_access_job_catalogue.catalogue_id is 'The foreign key into the Catalogue table';
COMMENT ON COLUMN casda.data_access_job_catalogue.last_modified is 'When the row was last modified (usually via an insert)';