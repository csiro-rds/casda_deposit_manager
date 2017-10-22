
ALTER TABLE casda.evaluation_file ADD COLUMN released_date timestamp with time zone;

COMMENT ON COLUMN casda.evaluation_file.released_date is 'The date the evaluation file was released.';