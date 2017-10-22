-- make the cutout table more generic so generated spectra can share

ALTER TABLE casda.image_cutout RENAME TO generated_image;
ALTER TABLE casda.generated_image ADD generated_file_type VARCHAR(50);
  
COMMENT ON TABLE casda.generated_image is 'Table for generated images (cutouts & generated spectra) requested in Data Access Jobs';
COMMENT ON COLUMN casda.generated_image.generated_file_type is 'The type of generated file, either spectra or cutout';