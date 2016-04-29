-- File Process table and table to store data on started downloading processes

CREATE TABLE casda.file_process (
    id                  BIGSERIAL PRIMARY KEY,
    file_id             VARCHAR(255),
    destination       	VARCHAR(1024),
    expires   			TIMESTAMP,
    pid   			    int,
    tag       	        VARCHAR(255),
    size_kb             BIGINT,
    retry_count         int,
    state       	    VARCHAR(255),
    last_modified       TIMESTAMP DEFAULT now()
);

CREATE INDEX idx_file_process_file_id ON casda.file_process( file_id );
  
COMMENT ON TABLE casda.file_process is 'Database store of processes started to download files from archive';

COMMENT ON COLUMN casda.file_process.id is 'The unique record id';  
COMMENT ON COLUMN casda.file_process.file_id is 'The unique NGAS file id to use for downloading';  
COMMENT ON COLUMN casda.file_process.destination is 'The destination to place the file to';  
COMMENT ON COLUMN casda.file_process.expires is 'The time when the process times out';  
COMMENT ON COLUMN casda.file_process.pid is 'The system process id of this process';  
COMMENT ON COLUMN casda.file_process.tag is 'A unique tag given to this process when it is started';  
COMMENT ON COLUMN casda.file_process.size_kb is 'The size in kb of the file being downloaded';  
COMMENT ON COLUMN casda.file_process.retry_count is 'Number of attempted restarts of the process';  
COMMENT ON COLUMN casda.file_process.state is 'Current state of the process';  
COMMENT ON COLUMN casda.file_process.last_modified is 'Date and time of the latest record update';  

