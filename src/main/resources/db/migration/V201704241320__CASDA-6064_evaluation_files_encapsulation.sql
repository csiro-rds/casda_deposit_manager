ALTER TABLE casda.evaluation_file ADD COLUMN encapsulation_file_id bigint;

COMMENT ON COLUMN casda.evaluation_file.encapsulation_file_id IS 'The id of the encapsulation file which houses this evaluation file';

ALTER TABLE casda.evaluation_file ADD CONSTRAINT encapsulation_file_id_fkey FOREIGN KEY (encapsulation_file_id) 
	REFERENCES casda.encapsulation_file (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;
	
CREATE TABLE casda.data_access_job_encapsulation_file (  
id                   BIGSERIAL PRIMARY KEY,
data_access_job_id   BIGINT references casda.data_access_job(id),
encapsulation_file_id   BIGINT references casda.encapsulation_file(id),
last_modified        TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE INDEX idx_access_job_encapsulation_file_job ON casda.data_access_job_encapsulation_file( data_access_job_id );
CREATE INDEX idx_access_job_encapsulation_file_encapsulation_file ON casda.data_access_job_encapsulation_file( encapsulation_file_id );
  
COMMENT ON TABLE casda.data_access_job_encapsulation_file is 'Join table providing a relationship between Data Access Job and Encapsulation File';

COMMENT ON COLUMN casda.data_access_job_encapsulation_file.id is 'The primary key';  
COMMENT ON COLUMN casda.data_access_job_encapsulation_file.data_access_job_id is 'The foreign key into the Data Access Job table';
COMMENT ON COLUMN casda.data_access_job_encapsulation_file.encapsulation_file_id is 'The foreign key into the Encapsulation File table';
COMMENT ON COLUMN casda.data_access_job_encapsulation_file.last_modified is 'When the row was last modified (usually via an insert)';