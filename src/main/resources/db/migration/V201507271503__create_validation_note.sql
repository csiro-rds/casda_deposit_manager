-- create validation_note table

DROP TABLE IF EXISTS casda.validation_note;

CREATE TABLE casda.validation_note (
    id                    BIGSERIAL PRIMARY KEY,
    sbid                  INTEGER references casda.observation(sbid),
    project_id            BIGINT references casda.project(id),
    person_id             VARCHAR( 255 ) NOT NULL,
    person_name           VARCHAR( 255 ) NOT NULL,
    created               TIMESTAMP WITH TIME ZONE NOT NULL,
    content               VARCHAR( 2500 ) NOT NULL,
    last_modified         TIMESTAMP WITH TIME ZONE DEFAULT now()
    
);

CREATE INDEX idx_validation_foreign_sbid ON casda.validation_note(sbid);
CREATE INDEX idx_validation_foreign_project_id ON casda.validation_note(project_id);
CREATE INDEX idx_validation_created ON casda.validation_note(created);

COMMENT ON COLUMN casda.validation_note.id is 'The primary key'; 
COMMENT ON COLUMN casda.validation_note.sbid is 'The foreign key into the Observation table (using the sbid column)';
COMMENT ON COLUMN casda.validation_note.project_id is 'The foreign key into the Project table';
COMMENT ON COLUMN casda.validation_note.person_id is 'The id of the author of the validation note';
COMMENT ON COLUMN casda.validation_note.person_name is 'The name of author of the validation note';
COMMENT ON COLUMN casda.validation_note.created is 'The creation date of the validation note';
COMMENT ON COLUMN casda.validation_note.content is 'The content of the validation note';
COMMENT ON COLUMN casda.validation_note.last_modified is 'When the row was last modified (usually via an insert)';
