-- Queryable view of the metric values for use in TAP queries
-- The spec for this SQL is described in CASDA-6087 --

CREATE OR REPLACE VIEW casda.observation_evaluation_file AS 
 SELECT efv.observation_id, efv.format, efv.filename, efv.filesize, project_id,
	'#{baseUrl}datalink/links?ID=evaluation-'||efv.id AS access_url, efv.released_date
 FROM casda.evaluation_file efv
 INNER JOIN casda.encapsulation_file encf ON encf.id = efv.encapsulation_file_id
 INNER JOIN casda.observation obs ON efv.observation_id = obs.id
 INNER JOIN casda.project p ON efv.project_id = p.id
 WHERE obs.deposit_state::text = 'DEPOSITED'::text;

COMMENT ON VIEW casda.observation_evaluation_file IS 'For each validation file, a file element will be present with the content from this table.';
COMMENT ON COLUMN casda.evaluation_file.encapsulation_file_id IS 'The foreign key into the Encapsulation File Table.';
COMMENT ON COLUMN casda.evaluation_file.project_id IS 'The foreign key into the Project Table.';

  
-- Create the TAP metadata for the observation_evaluation_file view
DELETE FROM casda.tap_columns WHERE table_name = 'casda.observation_evaluation_file';
DELETE FROM casda.tap_tables WHERE table_name = 'casda.observation_evaluation_file';

INSERT INTO casda.tap_tables (schema_name, table_name, table_type, db_schema_name, db_table_name, description, scs_enabled)
  VALUES ('casda', 'casda.observation_evaluation_file','view','casda','observation_evaluation_file', 'Evaluation File', false);
  
INSERT INTO casda.tap_columns (column_order, table_name, column_name, db_column_name, unit, utype, ucd, description, datatype, size, principal, indexed, std, scs_verbosity ) 
values 
(1, 'casda.observation_evaluation_file', 'observation_id','observation_id', NULL,    NULL,   NULL, 'Foreign key, observation id', 'BIGINT', 19, 1, 1, 0, 1),
(2, 'casda.observation_evaluation_file', 'format', 'format', NULL,    NULL,   NULL, 'The format of the validation file, eg pdf', 'VARCHAR', 255, 1, 0, 0, 1),
(3, 'casda.observation_evaluation_file', 'filename', 'filename', NULL,    NULL,   NULL, 'Path to the evaluation file (relative to the observation folder).', 'VARCHAR', 1000, 1, 0, 0, 1),
(4, 'casda.observation_evaluation_file', 'filesize', 'filesize', NULL,    NULL,   NULL, 'The size of the evaluation file in kilobytes', 'BIGINT', 15, 1, 0, 0, 1),
(5, 'casda.observation_evaluation_file', 'project_id', 'project_id',     NULL,    NULL,   NULL, 'Foreign key, project id', 'BIGINT', 15, 1, 1, 0, 1),
(6, 'casda.observation_evaluation_file', 'access_url', 'access_url',     NULL,    NULL,   'meta.ref.url', 'Evaulation file download link', 'CLOB', 255, 1, 0, 0, 1),
(7, 'casda.observation_evaluation_file', 'released_date', 'released_date',     NULL,    NULL,   NULL, 'The date the evaluation file was released.', 'TIMESTAMP', 24, 1, 0, 0, 1);