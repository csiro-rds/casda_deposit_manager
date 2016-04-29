-- Image Cutout table for data access jobs, CASDA-4853

CREATE TABLE casda.image_cutout (  
id                   BIGSERIAL PRIMARY KEY,
data_access_job_id   BIGINT references casda.data_access_job(id),
image_cube_id        BIGINT references casda.image_cube(id),
bounds               VARCHAR(2000),
filesize             BIGINT,
last_modified        TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE INDEX idx_image_cutout_data_access_job ON casda.image_cutout( data_access_job_id );
CREATE INDEX idx_image_cutout_image_cube ON casda.image_cutout( image_cube_id );
  
COMMENT ON TABLE casda.image_cutout is 'Table for image cutouts requested in Data Access Jobs';

COMMENT ON COLUMN casda.image_cutout.id is 'The primary key';  
COMMENT ON COLUMN casda.image_cutout.data_access_job_id is 'The foreign key into the Data Access Job table';
COMMENT ON COLUMN casda.image_cutout.image_cube_id is 'The foreign key into the Image Cube table';
COMMENT ON COLUMN casda.image_cutout.bounds is 'Details of the requested bounds for the image cutout';
COMMENT ON COLUMN casda.image_cutout.filesize is 'The size of the image cutout in kilobytes';
COMMENT ON COLUMN casda.image_cutout.last_modified is 'When the row was last modified (usually via an insert)';