-- creating a new column for the image cube dimensions
ALTER TABLE casda.image_cube ADD COLUMN dimensions TEXT;
COMMENT ON COLUMN casda.image_cube.dimensions is 'The image cube dimensions, json output from WCS lib';