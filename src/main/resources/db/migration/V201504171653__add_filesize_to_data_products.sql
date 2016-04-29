--  CASDA-4215
--  Store file size on deposit for all data products


ALTER TABLE casda.measurement_set ADD COLUMN filesize BIGINT;
COMMENT ON COLUMN casda.measurement_set.filesize is 'The size of the measurement set in kilobytes';

ALTER TABLE casda.evaluation_file ADD COLUMN filesize BIGINT;
COMMENT ON COLUMN casda.evaluation_file.filesize is 'The size of the evaluation file in kilobytes';

ALTER TABLE casda.catalogue ADD COLUMN filesize BIGINT;
COMMENT ON COLUMN casda.catalogue.filesize is 'The size of the original catalogue file in kilobytes';
