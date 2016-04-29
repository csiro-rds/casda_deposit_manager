-- Join table between data access job and image cube, CASDA-785

CREATE TABLE casda.data_access_job_image_cube (  
id                   BIGSERIAL PRIMARY KEY,
data_access_job_id   BIGINT references casda.data_access_job(id),
image_cube_id        BIGINT references casda.image_cube(id),
last_modified  TIMESTAMP DEFAULT now()
);

CREATE INDEX idx_access_job_img_cube_job ON casda.data_access_job_image_cube( data_access_job_id );
CREATE INDEX idx_access_job_img_cube_img_cube ON casda.data_access_job_image_cube( image_cube_id );
  
COMMENT ON TABLE casda.data_access_job_image_cube is 'Join table providing a relationship between Data Access Job and Image Cube';

COMMENT ON COLUMN casda.data_access_job_image_cube.id is 'The primary key';  
COMMENT ON COLUMN casda.data_access_job_image_cube.data_access_job_id is 'The foreign key into the Data Access Job table';
COMMENT ON COLUMN casda.data_access_job_image_cube.image_cube_id is 'The foreign key into the Image Cube table';
COMMENT ON COLUMN casda.data_access_job_image_cube.last_modified is 'When the row was last modified (usually via an insert)';