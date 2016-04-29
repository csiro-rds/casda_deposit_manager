--Add quality level to data products and catalogue rows - CASDA-272

ALTER TABLE casda.catalogue ADD COLUMN quality_level VARCHAR(15) DEFAULT 'NOT_VALIDATED';
COMMENT ON COLUMN casda.catalogue.quality_level is 'Indicator of quality level, updated by validators';
CREATE INDEX idx_catalogue_quality_level ON casda.catalogue( quality_level );

ALTER TABLE casda.continuum_component ADD COLUMN quality_level VARCHAR(15) DEFAULT 'NOT_VALIDATED';
COMMENT ON COLUMN casda.continuum_component.quality_level is 'Indicator of quality level, updated by validators';
CREATE INDEX idx_continuum_component_quality_level ON casda.continuum_component( quality_level );

ALTER TABLE casda.continuum_island ADD COLUMN quality_level VARCHAR(15) DEFAULT 'NOT_VALIDATED';
COMMENT ON COLUMN casda.continuum_island.quality_level is 'Indicator of quality level, updated by validators';
CREATE INDEX idx_continuum_island_quality_level ON casda.continuum_island( quality_level );

ALTER TABLE casda.evaluation_file ADD COLUMN quality_level VARCHAR(15) DEFAULT 'NOT_VALIDATED';
COMMENT ON COLUMN casda.evaluation_file.quality_level is 'Indicator of quality level, updated by validators';
CREATE INDEX idx_evaluation_file_quality_level ON casda.evaluation_file( quality_level );

ALTER TABLE casda.image_cube ADD COLUMN quality_level VARCHAR(15) DEFAULT 'NOT_VALIDATED';
COMMENT ON COLUMN casda.image_cube.quality_level is 'Indicator of quality level, updated by validators';
CREATE INDEX idx_image_cube_quality_level ON casda.image_cube( quality_level );

ALTER TABLE casda.measurement_set ADD COLUMN quality_level VARCHAR(15) DEFAULT 'NOT_VALIDATED';
COMMENT ON COLUMN casda.measurement_set.quality_level is 'Indicator of quality level, updated by validators';
CREATE INDEX idx_measurement_set_quality_level ON casda.measurement_set( quality_level );

ALTER TABLE casda.polarisation_component ADD COLUMN quality_level VARCHAR(15) DEFAULT 'NOT_VALIDATED';
COMMENT ON COLUMN casda.polarisation_component.quality_level is 'Indicator of quality level, updated by validators';
CREATE INDEX idx_polarisation_component_quality_level ON casda.polarisation_component( quality_level );