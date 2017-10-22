-- Queryable view of the metric values for use in TAP queries
-- The spec for this SQL is described in CASDA-6087 --

CREATE OR REPLACE VIEW casda.observation_validation_metric AS 
 SELECT observation_id, sbid, project_id, opal_code, metric_name, description, metric_value, status, vmv.last_modified
 FROM casda.validation_metric_value vmv
 INNER JOIN casda.validation_metric vm ON vmv.metric_id = vm.id
 INNER JOIN casda.observation obs ON vmv.observation_id = obs.id
 INNER JOIN casda.project p ON vmv.project_id = p.id
 WHERE obs.deposit_state::text = 'DEPOSITED'::text;

COMMENT ON VIEW casda.observation_validation_metric IS 'View of the validation metric value for a specific observation project block';

  
-- Create the TAP metadata for the observation_validation_metric view
DELETE FROM casda.tap_columns WHERE table_name = 'casda.observation_validation_metric';
DELETE FROM casda.tap_tables WHERE table_name = 'casda.observation_validation_metric';

INSERT INTO casda.tap_tables (schema_name, table_name, table_type, db_schema_name, db_table_name, description, scs_enabled)
  VALUES ('casda', 'casda.observation_validation_metric','view','casda','observation_validation_metric', 'A validation metric value for a specific observation project block', false);
  
INSERT INTO casda.tap_columns (column_order, table_name, column_name, db_column_name, unit, utype, ucd, description, datatype, size, principal, indexed, std, scs_verbosity ) 
values 
(1, 'casda.observation_validation_metric', 'observation_id','observation_id', NULL,    NULL,   NULL, 'Foreign key, observation id', 'BIGINT', 19, 1, 1, 0, 1),
(2, 'casda.observation_validation_metric', 'sbid',          'sbid',           NULL,    NULL,   NULL, 'This uniquely identifies the observation in the telescope operating system and central processor sub-systems', 'VARCHAR', 255, 1, 0, 0, 2),
(3, 'casda.observation_validation_metric', 'project_id',    'project_id',     NULL,    NULL,   NULL, 'Foreign key, project id', 'BIGINT', 15, 1, 1, 0, 1),
(4, 'casda.observation_validation_metric', 'opal_code',     'opal_code',      NULL,    NULL,   NULL, 'OPAL ID of the project', 'VARCHAR', 255, 1, 0, 0, 2),
(5, 'casda.observation_validation_metric', 'metric_name',   'metric_name',    NULL,    NULL,   'meta.name', 'The name of the metric', 'VARCHAR', 100, 1, 0, 0, 1),
(6, 'casda.observation_validation_metric', 'description',   'description',    NULL,    NULL,   NULL, 'The description or explanation of the metric', 'VARCHAR', 256, 0, 0, 0, 2),
(7, 'casda.observation_validation_metric', 'metric_value',  'metric_value',   NULL,    NULL,   NULL, 'The value of the metric for this observation project block.', 'VARCHAR', 255, 1, 0, 0, 1),
(8, 'casda.observation_validation_metric', 'status',        'status',         NULL,    NULL, 'meta.number', 'The status of the metric. 1 for good/green, 2 for caution/yellow or 3 for bad/red', 'INTEGER', 15, 1, 0, 0, 1);


