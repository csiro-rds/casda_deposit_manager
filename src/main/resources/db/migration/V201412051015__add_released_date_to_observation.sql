-- CASDA-871 Automated Release Processing
-- CASDA-3578 Update database schema (add release date to observation table)

ALTER TABLE casda.observation ADD COLUMN released_date TIMESTAMP;
COMMENT ON COLUMN casda.observation.released_date is 'The date that the data products of this observation were released';
