-- Add constraint to sbid column - CASDA-4250
ALTER TABLE casda.observation ALTER COLUMN sbid SET NOT NULL;