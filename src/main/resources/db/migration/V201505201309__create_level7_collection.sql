DROP TABLE IF EXISTS casda.level7_collection;

CREATE TABLE casda.level7_collection (
    id                    BIGSERIAL PRIMARY KEY,
    collection_id         BIGINT,
    project_id            BIGINT references casda.project(id),
    deposit_state         VARCHAR( 255 ) NOT NULL DEFAULT 'UNDEPOSITED',
    checkpoint_state_type VARCHAR( 255 ) NOT NULL DEFAULT 'UNDEPOSITED',
    deposit_started       TIMESTAMP WITH TIME ZONE NOT NULL,
    deposit_state_changed TIMESTAMP WITH TIME ZONE,
    deposit_failure_count INT DEFAULT 0,
    last_modified         TIMESTAMP WITH TIME ZONE DEFAULT now()
    
);

ALTER TABLE casda.level7_collection ADD CONSTRAINT unique_level7_collection_id UNIQUE (collection_id);

CREATE INDEX idx_level7_collection_id ON casda.level7_collection(collection_id);
CREATE INDEX idx_level7_foreign_project_id ON casda.level7_collection(project_id);
CREATE INDEX idx_level7_deposit_state ON casda.level7_collection(deposit_state);

COMMENT ON COLUMN casda.level7_collection.id is 'The primary key'; 
COMMENT ON COLUMN casda.level7_collection.collection_id is 'The data collection id from the Data Access Portal';
COMMENT ON COLUMN casda.level7_collection.project_id is 'The foreign key into the Project table';
COMMENT ON COLUMN casda.level7_collection.deposit_state is 'Deposit state of level 7 collection as a whole';
COMMENT ON COLUMN casda.level7_collection.checkpoint_state_type is 'Checkpoint state of level 7 collection as a whole';
COMMENT ON COLUMN casda.level7_collection.deposit_started is 'The datetime that the deposit of this level 7 collection started';
COMMENT ON COLUMN casda.level7_collection.deposit_state_changed 
  is 'The datetime that the deposit state of the level 7 collection last changed';
COMMENT ON COLUMN casda.level7_collection.deposit_failure_count is 'Number of times the level 7 collection deposit has failed';
COMMENT ON COLUMN casda.level7_collection.last_modified is 'When the row was last modified (usually via an insert)';








