-- CASDA-812 - Search data products by frequency range

-- Store the wavelength range for measurement set and catalogues. 
-- Wavelength is used here for compatibility with the VO ObsCore schema. 
-- This will be converted to a frequency range for use when searching. 
ALTER TABLE casda.measurement_set ADD COLUMN em_min DOUBLE PRECISION;
ALTER TABLE casda.measurement_set ADD COLUMN em_max DOUBLE PRECISION;
COMMENT ON COLUMN casda.measurement_set.em_min is 'Shortest wavelength observed, in metres.';
COMMENT ON COLUMN casda.measurement_set.em_max is 'Longest wavelength observed, in metres.';

ALTER TABLE casda.catalogue ADD COLUMN em_min DOUBLE PRECISION;
ALTER TABLE casda.catalogue ADD COLUMN em_max DOUBLE PRECISION;
COMMENT ON COLUMN casda.catalogue.em_min is 'Shortest wavelength recorded, in metres.';
COMMENT ON COLUMN casda.catalogue.em_max is 'Longest wavelength recorded, in metres.';
