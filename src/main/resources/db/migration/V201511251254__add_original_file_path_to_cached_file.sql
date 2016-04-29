-- creating a new column for a reference to the original file, for image cutouts
ALTER TABLE casda.cached_file ADD COLUMN original_file_path VARCHAR(1024);
COMMENT ON COLUMN casda.cached_file.original_file_path is 'Path to the original image file to perform cutout on, may be in the cache or on disk in ngas';