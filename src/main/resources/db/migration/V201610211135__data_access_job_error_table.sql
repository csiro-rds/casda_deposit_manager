--     CASDA-5913 - SODA Async - Add text error file for each criteria combo without a cutout

-- create data access job table
CREATE TABLE casda.data_access_job_error
(
  id bigserial NOT NULL,
  data_access_job_id bigint, 
  error_message character varying(2550), 
  CONSTRAINT data_access_job_error_pkey PRIMARY KEY (id),
  CONSTRAINT data_access_job_error_data_access_job_id_fkey FOREIGN KEY (data_access_job_id)
      REFERENCES casda.data_access_job (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);


COMMENT ON TABLE casda.data_access_job_error IS 'Element table providing a list of error messages for a Data Access Job';
COMMENT ON COLUMN casda.data_access_job_error.id IS 'The primary key';
COMMENT ON COLUMN casda.data_access_job_error.data_access_job_id IS 'The foreign key into the Data Access Job table';
COMMENT ON COLUMN casda.data_access_job_error.error_message IS 'The text of the error message, including the parameter combination';


-- create indexes
CREATE INDEX idx_access_job_error_job
  ON casda.data_access_job_error
  USING btree
  (data_access_job_id);

