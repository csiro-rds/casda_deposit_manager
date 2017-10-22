-- Represents a value that may be measured for use in validation. This is effectively the list of sighted metrics.
-- The spec for this SQL is described in CASDA-6087 --

CREATE TABLE casda.validation_metric (  
id               BIGSERIAL PRIMARY KEY,
metric_name      VARCHAR(100) NOT NULL,
description      VARCHAR(256),
last_modified    TIMESTAMP DEFAULT now()
);

CREATE INDEX idx_val_metric_metric_id ON casda.validation_metric( metric_name );

COMMENT ON TABLE casda.validation_metric is 'Represents a value that may be measured for use in validation.'; 
  
COMMENT ON COLUMN casda.validation_metric.id is 'The primary key';
COMMENT ON COLUMN casda.validation_metric.metric_name is 'The name of the metric - duplicates may occur when the description changes.';
COMMENT ON COLUMN casda.validation_metric.description is 'The description or explanation of the metric';
COMMENT ON COLUMN casda.validation_metric.last_modified is 'When the row was last modified (usually via an insert)';  


-- Represents an individual metric value for an observation project block

CREATE TABLE casda.validation_metric_value (  
id               BIGSERIAL PRIMARY KEY,
observation_id   BIGINT references casda.observation(id) NOT NULL,
project_id       BIGINT references casda.project(id) NOT NULL,
evaluation_file_id BIGINT references casda.evaluation_file(id) NOT NULL,
metric_id        BIGINT references casda.validation_metric(id) NOT NULL,
metric_value     VARCHAR( 255 ) NOT NULL,
status           SMALLINT,
last_modified    TIMESTAMP DEFAULT now()
);

CREATE INDEX idx_val_metric_value_observation ON casda.validation_metric_value( observation_id );
CREATE INDEX idx_val_metric_value_project ON casda.validation_metric_value( project_id );
CREATE INDEX idx_val_metric_value_evaluation_file ON casda.validation_metric_value( evaluation_file_id );
CREATE INDEX idx_val_metric_value_metric_id ON casda.validation_metric_value( metric_id );

COMMENT ON TABLE casda.validation_metric_value is 'An individual metric value for an observation project block, used in assessing the quality of the observation.'; 
  
COMMENT ON COLUMN casda.validation_metric_value.id is 'The primary key';
COMMENT ON COLUMN casda.validation_metric_value.observation_id is 'The foreign key into the Observation table';
COMMENT ON COLUMN casda.validation_metric_value.project_id is 'The foreign key into the Project table';
COMMENT ON COLUMN casda.validation_metric_value.evaluation_file_id is 'The foreign key into the evaluation_file table';
COMMENT ON COLUMN casda.validation_metric_value.metric_id is 'The foreign key into the validation_metric table';
COMMENT ON COLUMN casda.validation_metric_value.metric_value is 'The value of the metric for this observation project block.';
COMMENT ON COLUMN casda.validation_metric_value.status is 'The status of the metric. 1 for good/green, 2 for caution/yellow or 3 for bad/red';
COMMENT ON COLUMN casda.validation_metric_value.last_modified is 'When the row was last modified (usually via an insert)';


-- Add project to evaluation files
ALTER TABLE casda.evaluation_file ADD COLUMN project_id BIGINT references casda.project(id);

COMMENT ON COLUMN casda.evaluation_file.encapsulation_file_id IS 'The foreign key into the Encapsulation File Table.';
COMMENT ON COLUMN casda.evaluation_file.project_id IS 'The foreign key into the Project Table.';

