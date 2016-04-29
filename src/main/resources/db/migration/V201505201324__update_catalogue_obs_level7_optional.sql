-- CASDA-4330

-- Set observation to optional
ALTER TABLE casda.catalogue ALTER COLUMN observation_id DROP NOT NULL;
COMMENT ON COLUMN casda.catalogue.observation_id is 'The foreign key into the Observation table. If null, level7_collection_id must be populated';

-- Add optional level 7 collection to catalogue
ALTER TABLE casda.catalogue ADD COLUMN level7_collection_id BIGINT references casda.level7_collection(id);
COMMENT ON COLUMN casda.catalogue.level7_collection_id is 'The foreign key into the Level7 Collection table. If null, observation_id must be populated';
CREATE INDEX idx_catalogue_level7_collection_id ON casda.catalogue(level7_collection_id);