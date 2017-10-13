UPDATE casda.generated_image SET generated_file_type = 'CUTOUT';

ALTER TABLE casda.generated_image ALTER COLUMN generated_file_type SET NOT NULL;