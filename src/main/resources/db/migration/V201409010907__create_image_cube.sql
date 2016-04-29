-- Path: /dataset/images/image/ --
-- The spec for this SQL is described in CASDA-2045 --

CREATE TABLE casda.image_cube (  
id               BIGSERIAL PRIMARY KEY,
observation_id   BIGINT references casda.observation(id),
project_id       BIGINT references casda.project,
format           VARCHAR( 255 ),
filename         VARCHAR( 1000 ),
last_modified    TIMESTAMP DEFAULT now()
);

CREATE INDEX idx_image_cube_observation ON casda.image_cube( observation_id );
CREATE INDEX idx_image_cube_project ON casda.image_cube( project_id );
CREATE INDEX idx_image_cube_filename ON casda.image_cube( filename );

COMMENT ON TABLE casda.image_cube is 'Describes each image cube to be archived ';
  
COMMENT ON COLUMN casda.image_cube.id is 'The primary key';
COMMENT ON COLUMN casda.image_cube.observation_id is 'The foreign key into the parent Observation';
COMMENT ON COLUMN casda.image_cube.project_id is 'The foreign key into the Project table';
COMMENT ON COLUMN casda.image_cube.format is 'format of the image. Eg fits';
COMMENT ON COLUMN casda.image_cube.filename is 'Full path to the image file. \
    Eg  /scratch/askap/12345/image.i.clean.restored.fits';
COMMENT ON COLUMN casda.image_cube.last_modified is 'When the row was last modified (usually via an insert)';    
