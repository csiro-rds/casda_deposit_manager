--drop unused table
DROP TABLE IF EXISTS casda.continuum;

--add table to house validation errors for levl 7 image collection, if any
CREATE TABLE casda.validation_error
(
  id bigserial NOT NULL,
  level7_collection_id bigserial NOT NULL,
  error text,
  CONSTRAINT validation_error_pkey PRIMARY KEY (id),
  CONSTRAINT validation_error_fkey FOREIGN KEY (level7_collection_id)
      REFERENCES casda.level7_collection (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);
COMMENT ON TABLE casda.validation_error IS 'Lists validation errors for level 7 collections';
COMMENT ON COLUMN casda.validation_error.id IS 'The error Id';
COMMENT ON COLUMN casda.validation_error.level7_collection_id IS 'The level 7 collection which caused this error';
COMMENT ON COLUMN casda.validation_error.error IS 'The error thrown by validation';

--add table to house sbids for level 7  collections
CREATE TABLE casda.level7_collection_sbid
(
  level7_collection_id bigserial NOT NULL, -- The primary key
  sbid integer, -- The foreign key into the Observation table (using the sbid column)  
  CONSTRAINT level7_collection_sbid_pkey PRIMARY KEY (level7_collection_id, sbid),
  CONSTRAINT level7_collection_sbid_unique UNIQUE (level7_collection_id, sbid),
  CONSTRAINT level7_collectionr_sbid_fkey FOREIGN KEY (level7_collection_id)
      REFERENCES casda.level7_collection (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);
COMMENT ON TABLE casda.level7_collection_sbid IS 'Multiple Scheduling Blocks associated with a level 7 collection';
COMMENT ON COLUMN casda.level7_collection_sbid.level7_collection_id IS 'The level 7 collection Id';
COMMENT ON COLUMN casda.level7_collection_sbid.sbid IS 'sbid(s) associated with the level 7 collection';