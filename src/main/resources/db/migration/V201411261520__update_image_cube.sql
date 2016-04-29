-- Update columns in Image Cube table

ALTER TABLE casda.image_cube ADD COLUMN channel_width DOUBLE PRECISION;
ALTER TABLE casda.image_cube ADD COLUMN num_chan INT;
ALTER TABLE casda.image_cube ADD COLUMN centre_freq DOUBLE PRECISION;
ALTER TABLE casda.image_cube ADD COLUMN polarisation_states VARCHAR( 255 );
ALTER TABLE casda.image_cube ADD COLUMN stokes_parameters VARCHAR( 255 );
ALTER TABLE casda.image_cube ADD COLUMN num_pixel BIGINT;
ALTER TABLE casda.image_cube ADD COLUMN cell_size DOUBLE PRECISION;
ALTER TABLE casda.image_cube ADD COLUMN integration_time DOUBLE PRECISION;

COMMENT ON COLUMN casda.image_cube.channel_width IS 'Width of each frequency channel in kHz.';
COMMENT ON COLUMN casda.image_cube.num_chan IS 'Number of frequency channels.';
COMMENT ON COLUMN casda.image_cube.centre_freq IS 'The central frequency of the observation in MHz.';
COMMENT ON COLUMN casda.image_cube.polarisation_states IS 'String list of the polarisations captured in the observation.';
COMMENT ON COLUMN casda.image_cube.num_pixel IS 'Number of pixels in image.';
COMMENT ON COLUMN casda.image_cube.stokes_parameters IS 'list of stokes parameters recorded in the image.';
COMMENT ON COLUMN casda.image_cube.cell_size IS 'Size of each cell in the image in square degrees.';
COMMENT ON COLUMN casda.image_cube.integration_time IS 'The duration of the observation.';

