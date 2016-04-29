alter table casda.image_cube add column image_type varchar(50);
update casda.image_cube set image_type = 'Unknown' where image_type is null;
alter table casda.image_cube alter column image_type set not null;

COMMENT ON COLUMN casda.image_cube.image_type is 'The type of image contained in the FITS file';