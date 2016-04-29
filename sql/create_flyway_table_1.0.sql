-- Schema: casda

-- DROP SCHEMA casda;

CREATE SCHEMA casda
  AUTHORIZATION casdbusr;

-- Create FlyWay database migration management table and populate it with inital data.
-- No further scripts should be used after this. All scripts should be placed in the 
-- src/main/resources/db/migration folder (or its subfolders)

-- Table: casda.schema_version

-- DROP TABLE casda.schema_version;

CREATE TABLE casda.schema_version
(
  version_rank integer NOT NULL,
  installed_rank integer NOT NULL,
  version character varying(50) NOT NULL,
  description character varying(200) NOT NULL,
  type character varying(20) NOT NULL,
  script character varying(1000) NOT NULL,
  checksum integer,
  installed_by character varying(100) NOT NULL,
  installed_on timestamp without time zone NOT NULL DEFAULT now(),
  execution_time integer NOT NULL,
  success boolean NOT NULL,
  CONSTRAINT schema_version_pk PRIMARY KEY (version)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE casda.schema_version
  OWNER TO casdbusr;

-- Index: casda.schema_version_ir_idx

-- DROP INDEX casda.schema_version_ir_idx;

CREATE INDEX schema_version_ir_idx
  ON casda.schema_version
  USING btree
  (installed_rank);

-- Index: casda.schema_version_s_idx

-- DROP INDEX casda.schema_version_s_idx;

CREATE INDEX schema_version_s_idx
  ON casda.schema_version
  USING btree
  (success);

-- Index: casda.schema_version_vr_idx

-- DROP INDEX casda.schema_version_vr_idx;

CREATE INDEX schema_version_vr_idx
  ON casda.schema_version
  USING btree
  (version_rank);

-- Initial row, required for initOnMigrate=false

INSERT INTO casda.schema_version(
            version_rank, installed_rank, version, description, type, script, 
            checksum, installed_by, installed_on, execution_time, success)
    VALUES (1, 1, '1', '<< Flyway Init>>', 'INIT', '<< Flyway init >>', 
            null, 'CASDA', '08-08-2014', 0, TRUE);