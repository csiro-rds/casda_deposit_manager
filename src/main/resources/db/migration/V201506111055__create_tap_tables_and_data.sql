-- This file contains all of the data for the schemas, tables, columns, keys and key_columns tables for VO TAP
-- This is predominantly in the same file because certain data depends on others and so needs to be run in order

-- Comment added 25/5/15
-- Previously, to update the TAP schema we would rename this file with a new timestamp so flyway would re-run it on deploy.
-- With the addition of level 7 data we no longer want drop every table each time, so further TAP schema updates should be 
-- added to their own script from now on.

-- Table definition as per the TAP SCHEMA - http://www.ivoa.net/documents/TAP/

DROP TABLE if exists casda.key_columns;
DROP TABLE if exists casda.keys;
DROP TABLE if exists casda.columns;
DROP TABLE if exists casda.tables;
DROP TABLE if exists casda.schemas;


CREATE TABLE casda.schemas (  
    schema_name         VARCHAR( 64 ) PRIMARY KEY,
    description         VARCHAR( 255 ),
    utype               VARCHAR( 255 )
);

DROP DOMAIN if exists table_type;
CREATE DOMAIN table_type as VARCHAR CHECK(VALUE = 'table' OR VALUE = 'view');

CREATE TABLE casda.tables (  
    table_name          VARCHAR( 64 ) PRIMARY KEY,
    table_type          VARCHAR( 255 ), --casda.table_type - TODO restore to using table_type.  Requires Hibernate Converter for PostGres Enums / Java Enums,           
    schema_name         VARCHAR( 64 ) references casda.schemas( schema_name ),
    description         VARCHAR( 255 ),
    utype               VARCHAR( 255 ),
    db_schema_name      VARCHAR( 255 ),
    db_table_name       VARCHAR( 255 ),
    scs_enabled         BOOLEAN DEFAULT false
);

CREATE TABLE casda.columns (  
    PRIMARY KEY (table_name, column_name),
    UNIQUE (table_name, column_name),
    column_name         VARCHAR( 64 ),
    table_name          VARCHAR( 64 ) references casda.tables( table_name ),
    column_order        INT NOT NULL DEFAULT 0,
    description         VARCHAR( 255 ),
    unit                VARCHAR( 64 ),
    ucd                 VARCHAR( 255 ),
    utype               VARCHAR( 255 ),
    datatype            VARCHAR( 64 ),
    size                INT,
    principal           INT,
    indexed             INT,
    std                 INT,
    scs_verbosity       INT
);

CREATE TABLE casda.keys (
    key_id              VARCHAR( 64 ) PRIMARY KEY,
    from_table          VARCHAR( 64 ),
    target_table        VARCHAR( 64 ),
    description         VARCHAR( 255 ),
    utype               VARCHAR( 255 ),
    FOREIGN KEY (from_table) REFERENCES casda.tables( table_name ),
    FOREIGN KEY (target_table) REFERENCES casda.tables( table_name )
);

CREATE TABLE casda.key_columns (
    id                  INT PRIMARY KEY,
    key_id              VARCHAR( 64 ),
    from_column         VARCHAR( 64 ),
    target_column       VARCHAR( 64 ),
    -- these table columns are NOT in the spec but without them we can't choose a column since
    -- (table_name, column_name) is the column table's PRIMARY KEY so we must have a table
    from_table          VARCHAR( 64 ),
    target_table        VARCHAR( 64 ),
    FOREIGN KEY (key_id) REFERENCES casda.keys( key_id ),
    FOREIGN KEY (from_column, from_table) REFERENCES casda.columns( column_name, table_name ),
    FOREIGN KEY (target_column, target_table) REFERENCES casda.columns( column_name, table_name )
);

-- COMMENT ON SCHEMA tapschema is 'All TAP services must support a set of tables in a schema named TAP_SCHEMA that describe the tables and columns included in the service.';

COMMENT ON TABLE casda.schemas is 'The names of the TAP services schemas';

COMMENT ON COLUMN casda.schemas.schema_name is 'The schema name, possibly qualified.  Is the primary key.';  
COMMENT ON COLUMN casda.schemas.description is 'The brief description of the schema';
COMMENT ON COLUMN casda.schemas.utype is 'The UTYPE if schema corresponds to a data model.  This is meant to express the role of the column in the context of an external data model';

COMMENT ON TABLE casda.tables is 'The names of the TAP services schemas tables';

COMMENT ON COLUMN casda.tables.table_name is 'The table name as it should be used in queries.  Part of the primary key';  
COMMENT ON COLUMN casda.tables.schema_name is 'The schema name from TAP_SCHEMA.schemas.  Part of the primary key.';  
COMMENT ON COLUMN casda.tables.table_type is 'One of: table, view';
COMMENT ON COLUMN casda.tables.description is 'The brief description of the table';
COMMENT ON COLUMN casda.tables.utype is 'The UTYPE if table corresponds to a data model.  This is meant to express the role of the column in the context of an external data model';
COMMENT ON COLUMN casda.tables.db_schema_name is 'The database schema name by which this table must be referenced in SQL.';  
COMMENT ON COLUMN casda.tables.db_table_name is 'The database table name by which this table must be referenced in SQL.';  
COMMENT ON COLUMN casda.tables.scs_enabled is 'Should this table be queryable using the Simple Cone Search service.';  

COMMENT ON COLUMN casda.columns.scs_verbosity is 'The lowest verbosity level (1,2,3) at which this column should be included in a cone search result. Leave null to hide the column from SCS.';  



-- add details on the schemas available through VO TAP
INSERT INTO casda.schemas (schema_name) VALUES ('TAP_SCHEMA');
INSERT INTO casda.schemas (schema_name) VALUES ('ivoa');
INSERT INTO casda.schemas (schema_name, description) VALUES ('casda', 'CSIRO ASKAP Science Data Archive');

-- add details on the tables available through VO TAP

-- TAP tables
INSERT INTO casda.tables (schema_name, table_name, table_type, db_schema_name, db_table_name)
  VALUES ('TAP_SCHEMA', 'TAP_SCHEMA.schemas', 'table', 'casda', 'schemas');
INSERT INTO casda.tables (schema_name, table_name, table_type, db_schema_name, db_table_name)
  VALUES ('TAP_SCHEMA', 'TAP_SCHEMA.tables', 'table', 'casda', 'tables');
INSERT INTO casda.tables (schema_name, table_name, table_type, db_schema_name, db_table_name)
  VALUES ('TAP_SCHEMA', 'TAP_SCHEMA.columns', 'table', 'casda', 'columns');
INSERT INTO casda.tables (schema_name, table_name, table_type, db_schema_name, db_table_name)
  VALUES ('TAP_SCHEMA', 'TAP_SCHEMA.keys', 'table', 'casda', 'keys');
INSERT INTO casda.tables (schema_name, table_name, table_type, db_schema_name, db_table_name)
  VALUES ('TAP_SCHEMA', 'TAP_SCHEMA.key_columns', 'table', 'casda', 'key_columns');

-- IVOA Obscore view
INSERT INTO casda.tables (schema_name, table_name, table_type, db_schema_name, db_table_name, scs_enabled)
  VALUES ('ivoa', 'ivoa.obscore','view','casda','obscore', true);
  
-- CASDA tables
-- casda.catalogue table
INSERT INTO casda.tables (schema_name, table_name, table_type, db_schema_name, db_table_name, description)
  VALUES ('casda', 'casda.catalogue', 'table', 'casda', 'catalogue', 'A collection of objects detected in a particular image. Each catalogue is composed of multiple rows in one of the continuum_component, continuum_island etc tables.');
-- casda.continuum_component table
INSERT INTO casda.tables (schema_name, table_name, table_type, db_schema_name, db_table_name, description, scs_enabled)
  VALUES ('casda', 'casda.continuum_component','table','casda','continuum_component', 'Continuum component data', true);
INSERT INTO casda.tables (schema_name, table_name, table_type, db_schema_name, db_table_name, description, scs_enabled)
  VALUES ('casda', 'casda.continuum_island','table','casda','continuum_island', 'Continuum island data', true);
INSERT INTO casda.tables (schema_name, table_name, table_type, db_schema_name, db_table_name, description, scs_enabled)
  VALUES ('casda', 'casda.polarisation_component', 'table', 'casda', 'polarisation_component', 'Continuum polarisation component data', true);

-- add details on the columns available through VO TAP 
-- columns in the schemas table
INSERT INTO casda.columns (column_order, column_name, table_name,description, datatype,principal, indexed, std, size)
  VALUES (1, 'schema_name', 'TAP_SCHEMA.schemas','Fully qualified schema name','VARCHAR', 1,1,0, 16);
INSERT INTO casda.columns (column_order, column_name, table_name,description, datatype,principal, indexed, std, size)
  VALUES (2, 'description', 'TAP_SCHEMA.schemas','Brief description of schema','VARCHAR', 0,0,0, 256);
INSERT INTO casda.columns (column_order, column_name, table_name,description, datatype,principal, indexed, std, size)
  VALUES (3, 'utype', 'TAP_SCHEMA.schemas','UTYPE if schema corresponds to a data model','VARCHAR', 0,0,0, 64);

-- columns in the tables table
INSERT INTO casda.columns (column_order, column_name, table_name,description, datatype,principal, indexed, std, size)
  VALUES (1, 'table_name', 'TAP_SCHEMA.tables','Fully qualified table name','VARCHAR', 1,1,0, 32);
INSERT INTO casda.columns (column_order, column_name, table_name,description, datatype,principal, indexed, std, size)
  VALUES (2, 'table_type', 'TAP_SCHEMA.tables','One of: table or view','VARCHAR', 0,0,1, 8);
INSERT INTO casda.columns (column_order, column_name, table_name,description, datatype,principal, indexed, std, size)
  VALUES (3, 'schema_name', 'TAP_SCHEMA.tables','Fully qualified schema name','VARCHAR', 1,1,0, 16);
INSERT INTO casda.columns (column_order, column_name, table_name,description, datatype,principal, indexed, std, size)
  VALUES (4, 'description', 'TAP_SCHEMA.tables','Brief description of table','VARCHAR', 0,0,1, 256);
INSERT INTO casda.columns (column_order, column_name, table_name,description, datatype,principal, indexed, std, size)
  VALUES (5, 'utype', 'TAP_SCHEMA.tables','UTYPE if table corresponds to a data model','VARCHAR', 0,0,1, 64);

-- columns in the columns table
INSERT INTO casda.columns (column_order, column_name, table_name,description, datatype,principal, indexed, std, size)
  VALUES (1, 'table_name', 'TAP_SCHEMA.columns','Fully qualified table name','VARCHAR', 1,1,0, 32);
INSERT INTO casda.columns (column_order, column_name, table_name,description, datatype,principal, indexed, std, size)
  VALUES (2, 'column_name', 'TAP_SCHEMA.columns','Column name','VARCHAR', 1,1,0, 64);
INSERT INTO casda.columns (column_order, column_name, table_name,description, datatype,principal, indexed, std, size)
  VALUES (3, 'description', 'TAP_SCHEMA.columns','Brief description of column','VARCHAR', 0,0,0, 256);
INSERT INTO casda.columns (column_order, column_name, table_name,description, datatype,principal, indexed, std, size)
  VALUES (4, 'unit', 'TAP_SCHEMA.columns','Unit in VO std format','VARCHAR', 0,0,0, 64);
INSERT INTO casda.columns (column_order, column_name, table_name,description, datatype,principal, indexed, std, size)
  VALUES (5, 'ucd', 'TAP_SCHEMA.columns','UCD of column if any','VARCHAR', 0,0,0, 128);
INSERT INTO casda.columns (column_order, column_name, table_name,description, datatype,principal, indexed, std, size)
  VALUES (6, 'utype', 'TAP_SCHEMA.columns','UTYPE if table corresponds to a data model','VARCHAR', 0,0,0, 128);
INSERT INTO casda.columns (column_order, column_name, table_name,description, datatype,principal, indexed, std, size)
  VALUES (7, 'datatype', 'TAP_SCHEMA.columns','ADQL datatype as in section 2.5','VARCHAR', 0,0,0, 16);
INSERT INTO casda.columns (column_order, column_name, table_name,description, datatype,principal, indexed, std, size)
  VALUES (8, 'size', 'TAP_SCHEMA.columns','Length of variable length datatypes','INTEGER', 0,0,0, 10);
INSERT INTO casda.columns (column_order, column_name, table_name,description, datatype,principal, indexed, std, size)
  VALUES (9, 'principal', 'TAP_SCHEMA.columns','A principal column (1 true, 0 false)','INTEGER', 0,0,0, 1);
INSERT INTO casda.columns (column_order, column_name, table_name,description, datatype,principal, indexed, std, size)
  VALUES (10, 'indexed', 'TAP_SCHEMA.columns','An indexed column (1 true, 0 false)','INTEGER', 0,0,0, 1);
INSERT INTO casda.columns (column_order, column_name, table_name,description, datatype,principal, indexed, std, size)
  VALUES (11, 'std', 'TAP_SCHEMA.columns','A std column (1 true, 0 false)','INTEGER', 0,0,0, 1);

-- columns in the keys table
INSERT INTO casda.columns (column_order, column_name, table_name,description, datatype,principal, indexed, std, size)
  VALUES (1, 'key_id', 'TAP_SCHEMA.keys','Unique key descriptor','VARCHAR', 1,1,0, 64);
INSERT INTO casda.columns (column_order, column_name, table_name,description, datatype,principal, indexed, std, size)
  VALUES (2, 'from_table', 'TAP_SCHEMA.keys','Fully qualified table nam','VARCHAR', 0,0,0, 32);
INSERT INTO casda.columns (column_order, column_name, table_name,description, datatype,principal, indexed, std, size)
  VALUES (3, 'target_table', 'TAP_SCHEMA.keys','Fully qualified table name','VARCHAR', 0,0,0, 32);
INSERT INTO casda.columns (column_order, column_name, table_name,description, datatype,principal, indexed, std, size)
  VALUES (4, 'description', 'TAP_SCHEMA.keys','Description of this key','VARCHAR', 0,0,0, 256);
INSERT INTO casda.columns (column_order, column_name, table_name,description, datatype,principal, indexed, std, size)
  VALUES (5, 'utype', 'TAP_SCHEMA.keys','Utype of this key','VARCHAR', 0,0,0, 64);

-- columns in the key_columns table
INSERT INTO casda.columns (column_order, column_name, table_name,description, datatype,principal, indexed, std, size)
  VALUES (1, 'key_id', 'TAP_SCHEMA.key_columns','Key identifier from keys table','VARCHAR', 1,1,0, 64);
INSERT INTO casda.columns (column_order, column_name, table_name,description, datatype,principal, indexed, std, size)
  VALUES (2, 'from_column', 'TAP_SCHEMA.key_columns','Key column name in the from_table','VARCHAR', 0,0,0, 64);
INSERT INTO casda.columns (column_order, column_name, table_name,description, datatype,principal, indexed, std, size)
  VALUES (3, 'target_column', 'TAP_SCHEMA.key_columns','Key column name in the target_table','VARCHAR', 0,0,0, 64);
INSERT INTO casda.columns (column_order, column_name, table_name,description, datatype,principal, indexed, std, size)
  VALUES (4, 'id', 'TAP_SCHEMA.key_columns','For internal purposes','INTEGER', 0,1,0, 10);

-- columns in the ivoa.obscore view
-- mandatory columns
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (1, 'ivoa.obscore', 'dataproduct_type', 'Logical data product type from the IVOA controlled list. Catalogues will be null but described in the dataproduct_subtype field.', 'VARCHAR', 255, NULL, 'obscore:Obs.dataProductType', 'meta.id', 1, 1, 1, 3);
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (2, 'ivoa.obscore', 'calib_level', 'Calibration level {0, 1, 2, 3}', 'INTEGER', 15, NULL, 'obscore:Obs.calibLevel', 'meta.code;obs.calib', 1, 1, 1, 3);
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (3, 'ivoa.obscore', 'obs_collection', 'Name of the data collection', 'VARCHAR', 255, NULL, 'obscore:DataID.Collection', 'meta.id', 1, 1, 1, 3);
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (4, 'ivoa.obscore', 'obs_id', 'Observation ID', 'VARCHAR', 255, NULL, 'obscore:DataID.observationID', 'meta.id', 1, 1, 1, 1);
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (5, 'ivoa.obscore', 'obs_publisher_did', 'Dataset identifier given by the publisher', 'VARCHAR', 255, NULL, 'obscore:Curation.PublisherDID', 'meta.ref.url;meta.curation', 1, 1, 1, 3);
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (6, 'ivoa.obscore', 'access_url', 'URL used to access (download) dataset', 'CLOB', 2000, NULL, 'obscore:Access.Reference', 'meta.ref.url', 1, 0, 1, 2);
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (7, 'ivoa.obscore', 'access_format', 'File content format', 'VARCHAR', 255, NULL, 'obscore:Access.Format', 'meta.code.mime', 1, 0, 1, 3);
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (8, 'ivoa.obscore', 'access_estsize', 'Estimated size of dataset in kilobytes', 'BIGINT', 15, 'kbyte', 'obscore:Access.Size', 'phys.size;meta.file', 1, 0, 1, 3);
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (9, 'ivoa.obscore', 'target_name', 'Astronomical object observed, if any', 'VARCHAR', 255, NULL, 'obscore:Target.Name', 'meta.id;src', 1, 0, 1, 2);
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (10, 'ivoa.obscore', 's_ra', 'Central right ascension, ICRS', 'DOUBLE', 15, 'deg', 'obscore:Char.SpatialAxis.Coverage.Location.Coord.Position2D.Value2.C1', 'pos.eq.ra', 1, 0, 1, 1);
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (11, 'ivoa.obscore', 's_dec', 'Central declination, ICRS', 'DOUBLE', 15, 'deg', 'obscore:Char.SpatialAxis.Coverage.Location.Coord.Position2D.Value2.C2', 'pos.eq.dec', 1, 0, 1, 1);
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (12, 'ivoa.obscore', 's_fov', 'Diameter (bounds) of the covered region', 'DOUBLE', 15, 'deg', 'obscore:Char.SpatialAxis.Coverage.Bounds.Extent.diameter', 'phys.angSize;instr.fov', 1, 0, 1, 3);
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (13, 'ivoa.obscore', 's_region', 'Region covered as specified in STC or ADQL', 'REGION', 200, NULL, 'obscore:Char.SpatialAxis.Coverage.Support.Area', 'phys.angArea;obs', 1, 0, 1, 3);
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (14, 'ivoa.obscore', 's_resolution', 'Spatial resolution of data as FWHM', 'DOUBLE', 15, 'arcsec', 'obscore:Char.SpatialAxis.Resolution.refval', 'pos.angResolution', 1, 1, 1, 3);
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (15, 'ivoa.obscore', 't_min', 'Start time in MJD', 'DOUBLE', 15, 'd', 'obscore:Char.TimeAxis.Coverage.Bounds.Limits.Interval.StartTime', 'time.start;obs.exposure', 1, 0, 1, 3);
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (16, 'ivoa.obscore', 't_max', 'Stop time in MJD', 'DOUBLE', 15, 'd', 'obscore:Char.TimeAxis.Coverage.Bounds.Limits.Interval.StopTime', 'time.end;obs.exposure', 1, 0, 1, 3);
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (17, 'ivoa.obscore', 't_exptime', 'Total exposure time', 'DOUBLE', 15, 's', 'obscore:Char.TimeAxis.Coverage.Support.Extent', 'time.duration;obs.exposure', 1, 0, 1, 3);
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (18, 'ivoa.obscore', 't_resolution', 'Temporal resolution FWHM', 'DOUBLE', 15, 's', 'obscore:Char.TimeAxis.Resolution.refval', 'time.resolution', 1, 0, 1, 3);
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (19, 'ivoa.obscore', 'em_min', 'Start in spectral coordinates', 'DOUBLE', 15, 'm', 'obscore:Char.SpectralAxis.Coverage.Bounds.Limits.Interval.LoLim', 'em.wl;stat.min', 1, 0, 1, 3);
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (20, 'ivoa.obscore', 'em_max', 'Stop in spectral coordinates', 'DOUBLE', 15, 'm', 'obscore:Char.SpectralAxis.Coverage.Bounds.Limits.Interval.HiLim', 'em.wl;stat.max', 1, 0, 1, 3);
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (21, 'ivoa.obscore', 'em_res_power', 'Spectral resolving power', 'DOUBLE', 15, NULL, 'obscore:Char.SpectralAxis.Resolution.ResolPower.refVal', 'spect.resolution', 1, 0, 1, 3);
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (22, 'ivoa.obscore', 'o_ucd', 'UCD of observable (e.g. phot. plux. density)', 'VARCHAR', 255, NULL, 'obscore:Char.ObservableAxis.ucd', 'meta.ucd', 1, 0, 1, 3);
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (23, 'ivoa.obscore', 'pol_states', 'List of polarization states or NULL if not applicable', 'VARCHAR', 255, NULL, 'obscore:Char.PolarizationAxis.stateList', 'meta.code;phys.polarization', 1, 0, 1, 3);
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (24, 'ivoa.obscore', 'facility_name', 'Name of the facility used for this observation', 'VARCHAR', 255, NULL, 'obscore:Provenance.ObsConfig.facility.name', 'meta.id;instr.tel', 1, 0, 1, 3);
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (25, 'ivoa.obscore', 'instrument_name', 'Name of the instrument used for this observation', 'VARCHAR', 255, NULL, 'obscore:Provenance.ObsConfig.instrument.name', 'meta.id;instr', 1, 0, 1, 3);
-- Optional ObsCore fields which we are implementing
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (26, 'ivoa.obscore', 'dataproduct_subtype', 'Further description of the type of data product, including where the dataproduct_type is null.', 'VARCHAR', 255, NULL, 'obscore:Obs.dataProductSubtype', 'meta.id', 1, 0, 1, 3);
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (27, 'ivoa.obscore', 'em_ucd', 'Nature of the spectral axis', 'VARCHAR', 255, NULL, 'obscore:Char.SpectralAxis.ucd', 'meta.ucd', 1, 0, 1, 3);
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (28, 'ivoa.obscore', 'em_unit', 'Units along the spectral axis', 'VARCHAR', 255, NULL, 'obscore:Char.SpectralAxis.unit', 'meta.unit', 1, 0, 1, 3);
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (29, 'ivoa.obscore', 'em_resolution', 'Value of Resolution along the spectral axis', 'DOUBLE', 15, 'm', 'obscore:Char.SpectralAxis.Resolution.refval.value', 'spect.resolution;stat.mean', 1, 0, 1, 3);
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (30, 'ivoa.obscore', 's_resolution_min', 'Resolution min value on spatial axis (FHWM of PSF)', 'DOUBLE', 15, 'arcsec', 'obscore:Char.SpatialAxis.Resolution.Bounds.Limits.Interval.LoLim', 'pos.angResolution;stat.min', 1, 0, 1, 3);
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (31, 'ivoa.obscore', 's_resolution_max', 'Resolution max value on spatial axis', 'DOUBLE', 15, 'arcsec', 'obscore:Char.SpatialAxis.Resolution.Bounds.Limits.Interval.HiLim', 'pos.angResolution;stat.max', 1, 0, 1, 3);
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (32, 'ivoa.obscore', 's_ucd', 'Ucd for the nature of the spatial axis (pos or u,v data)', 'VARCHAR', 255, NULL, 'obscore:Char.SpatialAxis.ucd', 'meta.ucd', 1, 0, 1, 3);
INSERT INTO casda.columns (column_order, table_name, column_name, description, datatype, size, unit, utype, ucd, principal, indexed, std, scs_verbosity)
  VALUES (33, 'ivoa.obscore', 's_unit', 'Unit used for spatial axis', 'VARCHAR', 255, NULL, 'obscore:Char.SpatialAxis.unit', 'meta.unit', 1, 0, 1, 3);

-- columns in the catalogue table 
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description)
  VALUES (1, 'id', 'casda.catalogue', 'BIGINT', 19, 1, 0, 1, 'Primary key');
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description)
  VALUES (2, 'observation_id', 'casda.catalogue', 'BIGINT', 19, 1, 1, 1, 'Foreign key, observation id');
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description)
  VALUES (3, 'project_id', 'casda.catalogue', 'BIGINT', 19, 1, 0, 1, 'Foreign key, project id');
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description)
  VALUES (4, 'format', 'casda.catalogue', 'VARCHAR', 255, 1, 0, 1, 'The format of the metadata, eg VOTable');
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd)
  VALUES (5, 'filename', 'casda.catalogue', 'VARCHAR', 255, 1, 1, 1, 'Full path to the catalogue file', 'meta.file');
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd)
  VALUES (6, 'freq_ref', 'casda.catalogue', 'REAL', 10, 1, 0, 1, 'Frequency at which fluxes are determined', 'em.freq;phot.flux');
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description)
  VALUES (7, 'image_id', 'casda.catalogue', 'BIGINT', 19, 1, 0, 1, 'Foreign key, Image id that the detection came from');
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd)
  VALUES (8, 'time_obs', 'casda.catalogue', 'TIMESTAMP', 24, 1, 0, 1, 'Time of observation. Full UT time, including seconds.', 'time.start');
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd)
  VALUES (9, 'time_obs_mjd', 'casda.catalogue', 'DOUBLE', 19, 1, 0, 1, 'Time of observation. Modified Julian Date.', 'time.start');
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd)
  VALUES (10, 'quality_level', 'casda.catalogue', 'VARCHAR', 15, 1, 1, 1, 'Indicator of quality level.', 'meta.code.qual');
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd)
  VALUES (11, 'released_date', 'casda.catalogue', 'TIMESTAMP', 24, 1, 0, 1, 'Date the catalogue was released', null);

-- columns in the continuum_component table
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (1, 'id', 'casda.continuum_component', 'BIGINT', 19, 1, 1, 1, 'Primary key', 'meta.record', 1);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, scs_verbosity)
  VALUES (2, 'catalogue_id', 'casda.continuum_component', 'BIGINT', 19, 1, 1, 1, 'Catalogue identifier', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, scs_verbosity)
  VALUES (3, 'sbid', 'casda.continuum_component', 'INTEGER', 15, 1, 1, 1, 'Scheduling Block identifier', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, scs_verbosity)
  VALUES (4, 'project_id', 'casda.continuum_component', 'BIGINT', 19, 1, 1, 1, 'Project identifier', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (5, 'island_id', 'casda.continuum_component', 'VARCHAR', 255, 1, 0, 1, 'Island identifier', 'meta.id.parent', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (6, 'component_id', 'casda.continuum_component', 'VARCHAR', 256, 1, 1, 1, 'Component identifier', 'meta.id;meta.main', 2);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (7, 'component_name', 'casda.continuum_component', 'VARCHAR', 32, 1, 1, 1, 'Component name', 'meta.id', 2);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (8, 'ra_hms_cont', 'casda.continuum_component', 'VARCHAR', 16, 1, 1, 1, 'J2000 right ascension (hh:mm:ss.sss)', 'pos.eq.ra', 'h:m:s', 3);  
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (9, 'dec_dms_cont', 'casda.continuum_component', 'VARCHAR', 16, 1, 1, 1, 'J2000 declination (dd:mm:ss.ss)', 'pos.eq.dec', 'deg:arcmin:arcsec', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (10, 'ra_deg_cont', 'casda.continuum_component', 'DOUBLE', 12, 1, 1, 1, 'J2000 right ascension in decimal degrees', 'pos.eq.ra;meta.main', 'deg', 1);  
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (11, 'dec_deg_cont', 'casda.continuum_component', 'DOUBLE', 13, 1, 1, 1, 'J2000 declination in decimal degrees', 'pos.eq.dec;meta.main', 'deg', 1);  
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (12, 'ra_err', 'casda.continuum_component', 'REAL', 11, 1, 0, 1, 'Error in Right Ascension', 'stat.error;pos.eq.ra', 'arcsec', 3);  
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (13, 'dec_err', 'casda.continuum_component', 'REAL', 11, 1, 0, 1, 'Error in Declination', 'stat.error;pos.eq.dec', 'arcsec', 3);  
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (14, 'freq', 'casda.continuum_component', 'REAL', 11, 1, 1, 1, 'Frequency', 'em.freq', 'MHz', 3);  
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (15, 'flux_peak', 'casda.continuum_component', 'REAL', 11, 1, 1, 1, 'Peak flux density', 'phot.flux.density;stat.max;em.radio;stat.fit', 'mJy/beam', 3);  
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (16, 'flux_peak_err', 'casda.continuum_component', 'REAL', 14, 1, 0, 1, 'Error in peak flux density', 'stat.error;phot.flux.density;stat.max;em.radio;stat.fit', 'mJy/beam', 3); 
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (17, 'flux_int', 'casda.continuum_component', 'REAL', 9, 1, 1, 1, 'Integrated flux density', 'phot.flux.density;em.radio;stat.fit', 'mJy', 2);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (18, 'flux_int_err', 'casda.continuum_component', 'REAL', 13, 1, 0, 1, 'Error in integrated flux density', 'stat.error;phot.flux.density;em.radio;stat.fit', 'mJy', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (19, 'maj_axis', 'casda.continuum_component', 'REAL', 9, 1, 0, 1, 'FWHM major axis before deconvolution', 'phys.angSize.smajAxis;em.radio;stat.fit', 'arcsec', 3); 
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (20, 'min_axis', 'casda.continuum_component', 'REAL', 9, 1, 0, 1, 'FWHM minor axis before deconvolution', 'phys.angSize.sminAxis;em.radio;stat.fit', 'arcsec', 3);  
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (21, 'pos_ang', 'casda.continuum_component', 'REAL', 8, 1, 0, 1, 'Position angle before deconvolution', 'phys.angSize;pos.posAng;em.radio;stat.fit', 'deg', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (22, 'maj_axis_err', 'casda.continuum_component', 'REAL', 13, 1, 0, 1, 'Error in major axis before deconvolution', 'stat.error;phys.angSize.smajAxis;em.radio', 'arcsec', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (23, 'min_axis_err', 'casda.continuum_component', 'REAL', 13, 1, 0, 1, 'Error in minor axis before deconvolution', 'stat.error;phys.angSize.sminAxis;em.radio', 'arcsec', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (24, 'pos_ang_err', 'casda.continuum_component', 'REAL', 12, 1, 0, 1, 'Error in position angle before deconvolution', 'stat.error;phys.angSize;pos.posAng;em.radio', 'deg', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (25, 'maj_axis_deconv', 'casda.continuum_component', 'REAL', 16, 1, 0, 1, 'FWHM major axis after deconvolution', 'phys.angSize.smajAxis;em.radio;askap:meta.deconvolved', 'arcsec', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (26, 'min_axis_deconv', 'casda.continuum_component', 'REAL', 16, 1, 0, 1, 'FWHM minor axis after deconvolution', 'phys.angSize.sminAxis;em.radio;askap:meta.deconvolved', 'arcsec', 3);  
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (27, 'pos_ang_deconv', 'casda.continuum_component', 'REAL', 15, 1, 0, 1, 'Position angle after deconvolution', 'phys.angSize;pos.posAng;em.radio;askap:meta.deconvolved', 'deg', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (28, 'chi_squared_fit', 'casda.continuum_component', 'REAL', 17, 1, 0, 1, 'Chi-squared value of Gaussian fit', 'stat.fit.chi2', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (29, 'rms_fit_gauss', 'casda.continuum_component', 'REAL', 14, 1, 0, 1, 'RMS residual of Gaussian fit', 'stat.stdev;stat.fit', 'mJy/beam', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (30, 'spectral_index', 'casda.continuum_component', 'REAL', 15, 1, 1, 1, 'Spectral index (First Taylor term)', 'spect.index;em.radio', 3);  
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (31, 'spectral_curvature', 'casda.continuum_component', 'REAL', 19, 1, 0, 1, 'Spectral curvature (Second Taylor term)', 'askap:spect.curvature;em.radio', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (32, 'rms_image', 'casda.continuum_component', 'REAL', 11, 1, 0, 1, 'rms noise level in image', 'stat.stdev;phot.flux.density', 'mJy/beam', 3);  
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (33, 'flag_c1', 'casda.continuum_component', 'INTEGER', 1, 1, 1, 1, 'Source has siblings', 'meta.code', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (34, 'flag_c2', 'casda.continuum_component', 'INTEGER', 1, 1, 1, 1, 'Component parameters are initial estimate, not from fit', 'meta.code', 3);  
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (35, 'flag_c3', 'casda.continuum_component', 'INTEGER', 1, 1, 1, 1, 'Placeholder flag3', 'meta.code', 3);  
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (36, 'flag_c4', 'casda.continuum_component', 'INTEGER', 1, 1, 1, 1, 'Placeholder flag4', 'meta.code', 3);  
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (37, 'comment', 'casda.continuum_component', 'VARCHAR', 1000, 1, 0, 1, 'Comment', 'meta.note', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (38, 'quality_level', 'casda.continuum_component', 'VARCHAR', 15, 1, 1, 1, 'Indicator of quality level.', 'meta.code.qual', 3);

-- add foreign keys information
-- for catalogue table to the continuum_component table
INSERT INTO casda.keys (key_id, from_table, target_table, description)
  VALUES (1, 'casda.continuum_component', 'casda.catalogue', 'Foreign key from catalogue to continuum_component table');  
  
INSERT INTO casda.key_columns (id, key_id, from_column, target_column, from_table, target_table)
  VALUES (1, 
    (SELECT key_id FROM casda.keys where from_table = 'casda.continuum_component' and target_table = 'casda.catalogue'),
    'catalogue_id', 'id', 'casda.continuum_component', 'casda.catalogue'
  );
  
  
-- columns in the continuum_island table
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (1, 'id', 'casda.continuum_island', 'BIGINT', 19, 1, 1, 1, 'Primary key', 'meta.record', 1);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, scs_verbosity)
  VALUES (2, 'catalogue_id', 'casda.continuum_island', 'BIGINT', 19, 1, 1, 1, 'Catalogue identifier', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, scs_verbosity)
  VALUES (3, 'sbid', 'casda.continuum_island', 'INTEGER', 15, 1, 1, 1, 'Scheduling Block identifier', 2);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, scs_verbosity)
  VALUES (4, 'project_id', 'casda.continuum_island', 'BIGINT', 19, 1, 1, 1, 'Project identifier', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (5, 'island_id', 'casda.continuum_island', 'VARCHAR', 255, 1, 1, 1, 'Island identifier', 'meta.id;meta.main', 2);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (6, 'island_name', 'casda.continuum_island', 'VARCHAR', 15, 1, 1, 1, 'Island name', 'meta.id', 2);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (7, 'n_components', 'casda.continuum_island', 'INTEGER', 15, 1, 0, 1, 'Number of discrete components extracted from the island', 'meta.number', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (8, 'ra_hms_cont', 'casda.continuum_island', 'VARCHAR', 16, 1, 1, 1, 'J2000 right ascension (hh:mm:ss.sss)', 'pos.eq.ra', 'h:m:s', 3);  
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (9, 'dec_dms_cont', 'casda.continuum_island', 'VARCHAR', 16, 1, 1, 1, 'J2000 declination (dd:mm:ss.ss)', 'pos.eq.dec', 'deg:arcmin:arcsec', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (10, 'ra_deg_cont', 'casda.continuum_island', 'DOUBLE', 19, 1, 1, 1, 'J2000 right ascension in decimal degrees', 'pos.eq.ra;meta.main', 'deg', 1);  
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (11, 'dec_deg_cont', 'casda.continuum_island', 'DOUBLE', 19, 1, 1, 1, 'J2000 declination in decimal degrees', 'pos.eq.dec;meta.main', 'deg', 1);  
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (12, 'freq', 'casda.continuum_island', 'REAL', 10, 1, 1, 1, 'Frequency', 'em.freq', 'MHz', 3);  
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (13, 'maj_axis', 'casda.continuum_island', 'REAL', 10, 1, 0, 1, 'Major axis determined from detected pixels', 'phys.angSize.smajAxis;em.radio', 'arcsec', 3); 
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (14, 'min_axis', 'casda.continuum_island', 'REAL', 10, 1, 0, 1, 'Minor axis determined from detected pixels', 'phys.angSize.sminAxis;em.radio', 'arcsec', 3);  
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (15, 'pos_ang', 'casda.continuum_island', 'REAL', 10, 1, 0, 1, 'Position angle of major axis (East of North)', 'phys.AngSize;pos.posAng;em.radio', 'deg', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (16, 'flux_int', 'casda.continuum_island', 'REAL', 10, 1, 1, 1, 'Integrated flux density', 'phot.flux.density.integrated;em.radio', 'mJy', 2);  
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity)
  VALUES (17, 'flux_peak', 'casda.continuum_island', 'REAL', 10, 1, 1, 1, 'Peak flux density', 'phot.flux.density;stat.max;em.radio', 'mJy/beam', 3);  
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (18, 'x_min', 'casda.continuum_island', 'INTEGER', 15, 1, 0, 1, 'Minimum x pixel', 'pos.cartesian.x;stat.min', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (19, 'x_max', 'casda.continuum_island', 'INTEGER', 15, 1, 0, 1, 'Maximum x pixel', 'pos.cartesian.x;stat.max', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (20, 'y_min', 'casda.continuum_island', 'INTEGER', 15, 1, 0, 1, 'Minimum y pixel', 'pos.cartesian.y;stat.min', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (21, 'y_max', 'casda.continuum_island', 'INTEGER', 15, 1, 0, 1, 'Maximum y pixel', 'pos.cartesian.y;stat.max', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (22, 'n_pix', 'casda.continuum_island', 'INTEGER', 15, 1, 0, 1, 'Number of pixels above threshold', 'phys.angArea;instr.pixel;meta.number', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (23, 'x_ave', 'casda.continuum_island', 'REAL', 10, 1, 0, 1, 'Average x pixel', 'pos.cartesian.x;stat.mean', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (24, 'y_ave', 'casda.continuum_island', 'REAL', 10, 1, 0, 1, 'Average y pixel', 'pos.cartesian.y;stat.mean', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (25, 'x_cen', 'casda.continuum_island', 'REAL', 10, 1, 0, 1, 'Flux weighted centroid x pixel', 'pos.cartesian.x;askap:stat.centroid', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (26, 'y_cen', 'casda.continuum_island', 'REAL', 10, 1, 0, 1, 'Flux weighted centroid y pixel', 'pos.cartesian.y;askap:stat.centroid', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (27, 'x_peak', 'casda.continuum_island', 'INTEGER', 15, 1, 0, 1, 'x pixel corresponding to peak flux density', 'pos.cartesian.x;phot.flux;stat.max', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (28, 'y_peak', 'casda.continuum_island', 'INTEGER', 15, 1, 0, 1, 'y pixel corresponding to peak flux density', 'pos.cartesian.y;phot.flux;stat.max', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (29, 'flag_c1', 'casda.continuum_island', 'INTEGER', 1, 1, 1, 1, 'Placeholder flag1', 'meta.code', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (30, 'flag_c2', 'casda.continuum_island', 'INTEGER', 1, 1, 1, 1, 'Placeholder flag2', 'meta.code', 3);  
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (31, 'flag_c3', 'casda.continuum_island', 'INTEGER', 1, 1, 1, 1, 'Placeholder flag3', 'meta.code', 3);  
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (32, 'flag_c4', 'casda.continuum_island', 'INTEGER', 1, 1, 1, 1, 'Placeholder flag4', 'meta.code', 3);  
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (33, 'comment', 'casda.continuum_island', 'VARCHAR', 1000, 1, 0, 1, 'Comment', 'meta.note', 3);
INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, scs_verbosity)
  VALUES (34, 'quality_level', 'casda.continuum_island', 'VARCHAR', 15, 1, 1, 1, 'Indicator of quality level.', 'meta.code.qual', 3);

-- add foreign keys information
-- for catalogue table to the continuum_island table
INSERT INTO casda.keys (key_id, from_table, target_table, description)
  VALUES (2, 'casda.continuum_island', 'casda.catalogue', 'Foreign key from catalogue to continuum_island table');  
  
INSERT INTO casda.key_columns (id, key_id, from_column, target_column, from_table, target_table)
  VALUES (2, 
    (SELECT key_id FROM casda.keys where from_table = 'casda.continuum_island' and target_table = 'casda.catalogue'),
    'catalogue_id', 'id', 'casda.continuum_island', 'casda.catalogue'
  );
-- for continuum_island table to continuum_component table
INSERT INTO casda.keys (key_id, from_table, target_table, description)
  VALUES (3, 'casda.continuum_component', 'casda.continuum_island', 'Foreign key from continuum_island to continuum_component table');  
  
INSERT INTO casda.key_columns (id, key_id, from_column, target_column, from_table, target_table)
  VALUES (3, 
    (SELECT key_id FROM casda.keys where from_table = 'casda.continuum_component' and target_table = 'casda.continuum_island'),
    'island_id', 'island_id', 'casda.continuum_component', 'casda.continuum_island'
  );
  
-- columns in the polarisation_component table

INSERT INTO casda.columns (column_order, column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit, scs_verbosity ) 
values   
(1, 'id', 'casda.polarisation_component', 'BIGINT', 19, 1, 1, 1, 'Primary key', 'meta.record', null, 1),
(2, 'catalogue_id', 'casda.polarisation_component', 'BIGINT', 19, 1, 1, 1, 'Catalogue identifier', null, null, 3),
(3, 'sbid', 'casda.polarisation_component', 'INTEGER', 15, 1, 1, 1, 'Scheduling Block identifier', null, null, 3),
(4, 'project_id', 'casda.polarisation_component', 'BIGINT', 19, 1, 1, 1, 'Project identifier', null, null, 3),
(5, 'component_id', 'casda.polarisation_component', 'VARCHAR', 256, 1, 1, 1, 'Component identifier', 'meta.id;meta.main', null, 2),
(6, 'component_name', 'casda.polarisation_component', 'VARCHAR', 32, 1, 1, 1, 'Component name', 'meta.id', null, 2),
(7, 'ra_deg_cont', 'casda.polarisation_component', 'DOUBLE', 12, 1, 1, 1, 'J2000 right ascension in decimal degrees', 'pos.eq.ra;meta.main', 'deg', 1),
(8, 'dec_deg_cont', 'casda.polarisation_component', 'DOUBLE', 13, 1, 1, 1, 'J2000 declination in decimal degrees', 'pos.eq.dec;meta.main', 'deg', 1),
(9, 'flux_i_median', 'casda.polarisation_component', 'DOUBLE', 19, 1, 1, 1, 'Band-median value for Stokes I spectrum', 'phot.flux.density;em.radio', 'mJy/beam', 2),
(10, 'flux_q_median', 'casda.polarisation_component', 'DOUBLE', 19, 1, 1, 1, 'Band-median value for Stokes Q spectrum', 'phot.flux.density;em.radio;askap:phys.polarization.stokes.Q', 'mJy/beam', 2),
(11, 'flux_u_median', 'casda.polarisation_component', 'DOUBLE', 19, 1, 1, 1, 'Band-median value for Stokes U spectrum', 'phot.flux.density;em.radio;askap:phys.polarization.stokes.U', 'mJy/beam', 2),   
(12, 'flux_v_median', 'casda.polarisation_component', 'DOUBLE', 19, 1, 1, 1, 'Band-median value for Stokes V spectrum', 'phot.flux.density;em.radio;askap:phys.polarization.stokes.V', 'mJy/beam', 2),
(13, 'rms_i', 'casda.polarisation_component', 'DOUBLE', 19, 0, 0, 1, 'Band-median sensitivity for Stokes I spectrum', 'stat.stdev;phot.flux.density', 'mJy/beam', 3),
(14, 'rms_q', 'casda.polarisation_component', 'DOUBLE', 19, 0, 0, 1, 'Band-median sensitivity for Stokes Q spectrum', 'stat.stdev;phot.flux.density;askap:phys.polarization.stokes.Q', 'mJy/beam', 3),
(15, 'rms_u', 'casda.polarisation_component', 'DOUBLE', 19, 0, 0, 1, 'Band-median sensitivity for Stokes U spectrum', 'stat.stdev;phot.flux.density;askap:phys.polarization.stokes.U', 'mJy/beam', 3),  
(16, 'rms_v', 'casda.polarisation_component', 'DOUBLE', 19, 0, 0, 1, 'Band-median sensitivity for Stokes V spectrum', 'stat.stdev;phot.flux.density;askap:phys.polarization.stokes.V', 'mJy/beam', 3),
(17, 'co_1', 'casda.polarisation_component', 'DOUBLE', 19, 0, 0, 1, 'First order coefficient for polynomial fit to Stokes I spectrum', 'stat.fit.param;spect.continuum', null, 3),
(18, 'co_2', 'casda.polarisation_component', 'DOUBLE', 19, 0, 0, 1, 'Second order coefficient for polynomial fit to Stokes I spectrum', 'stat.fit.param;spect.continuum', null, 3),
(19, 'co_3', 'casda.polarisation_component', 'DOUBLE', 19, 0, 0, 1, 'Third order coefficient for polynomial fit to Stokes I spectrum', 'stat.fit.param;spect.continuum', null, 3),
(20, 'co_4', 'casda.polarisation_component', 'DOUBLE', 19, 0, 0, 1, 'Fourth order coefficient for polynomial fit to Stokes I spectrum ', 'stat.fit.param;spect.continuum', null, 3),
(21, 'co_5', 'casda.polarisation_component', 'DOUBLE', 19, 0, 0, 1, 'Fifth order coefficient for polynomial fit to Stokes I spectrum', 'stat.fit.param;spect.continuum', null, 3),
(22, 'lambda_ref_sq', 'casda.polarisation_component', 'DOUBLE', 19, 1, 1, 1, 'Reference wavelength squared', 'askap:em.wl.squared', 'm2', 2),
(23, 'rmsf_fwhm', 'casda.polarisation_component', 'DOUBLE', 19, 0, 0, 1, 'Full-width at half maximum of the rotation measure spread function', 'phys.polarization.rotMeasure;askap:phys.polarization.rmsfWidth', 'mJy/beam', 3),
(24, 'pol_peak', 'casda.polarisation_component', 'DOUBLE', 19, 1, 1, 1, 'Peak polarised intensity in the Faraday Dispersion Function', 'phot.flux.density;phys.polarization.rotMeasure;stat.max', 'mJy/beam', 2),
(25, 'pol_peak_debias', 'casda.polarisation_component', 'DOUBLE', 19, 1, 1, 1, 'Effective peak polarised intensity after correction for bias ', 'phot.flux.density;phys.polarization.rotMeasure;stat.max;askap:meta.corrected', 'mJy/beam', 2),
(26, 'pol_peak_err', 'casda.polarisation_component', 'DOUBLE', 19, 0, 0, 1, 'Uncertainty in pol_peak', 'stat.error;phot.flux.density;phys.polarization.rotMeasure;stat.max', 'mJy/beam', 3),
(27, 'pol_peak_fit', 'casda.polarisation_component', 'DOUBLE', 19, 1, 1, 1, 'Peak polarised intensity from a three-point parabolic fit ', 'phot.flux.density;phys.polarization.rotMeasure;stat.max;stat.fit', 'mJy/beam', 2),
(28, 'pol_peak_fit_debias', 'casda.polarisation_component', 'DOUBLE', 19, 1, 1, 1, 'Peak polarised intensity, corrected for bias, from a three-point parabolic fit ', 'phot.flux.density;phys.polarization.rotMeasure;stat.max;stat.fit;askap:meta.corrected', 'mJy/beam', 2),
(29, 'pol_peak_fit_err', 'casda.polarisation_component', 'DOUBLE', 19, 0, 0, 1, 'Uncertainty in pol_peak_fit ', 'stat.error;phot.flux.density;phys.polarization.rotMeasure;stat.max;stat.fit', 'mJy/beam', 3),
(30, 'pol_peak_fit_snr', 'casda.polarisation_component', 'DOUBLE', 19, 0, 0, 1, 'Signal-to-noise ratio of the peak polarisation', 'stat.snr;phot.flux.density;phys.polarization.rotMeasure;stat.max;stat.fit', null, 3),
(31, 'pol_peak_fit_snr_err', 'casda.polarisation_component', 'DOUBLE', 19, 0, 0, 1, 'Uncertainty in pol_peak_fit_snr', 'stat.error;stat.snr;phot.flux.density;phys.polarization.rotMeasure;stat.max;stat.fit', null, 3),
(32, 'fd_peak', 'casda.polarisation_component', 'DOUBLE', 19, 1, 1, 1, 'Faraday Depth from the channel with the peak of the Faraday Dispersion Function', 'phys.polarization.rotMeasure ', 'rad/m2', 2),
(33, 'fd_peak_err', 'casda.polarisation_component', 'DOUBLE', 19, 0, 0, 1, 'Uncertainty in far_depth_peak', 'stat.error;phys.polarization.rotMeasure ', 'rad/m2', 3),
(34, 'fd_peak_fit', 'casda.polarisation_component', 'DOUBLE', 19, 1, 1, 1, 'Faraday Depth from fit to peak in Faraday Dispersion Function', 'phys.polarization.rotMeasure;stat.fit', 'rad/m2', 2),
(35, 'fd_peak_fit_err', 'casda.polarisation_component', 'DOUBLE', 19, 0, 0, 1, 'uncertainty in fd_peak_fit', 'stat.error;phys.polarization.rotMeasure;stat.fit', 'rad/m2', 3),
(36, 'pol_ang_ref', 'casda.polarisation_component', 'DOUBLE', 19, 0, 0, 1, 'Polarisation angle at the reference wavelength', 'askap:phys.polarization.angle', 'deg', 3),
(37, 'pol_ang_ref_err', 'casda.polarisation_component', 'DOUBLE', 19, 0, 0, 1, 'Uncertainty in pol_ang_ref', 'stat.error;askap:phys.polarization.angle', 'deg', 3),
(38, 'pol_ang_zero', 'casda.polarisation_component', 'DOUBLE', 19, 0, 0, 1, 'Polarisation angle de-rotated to zero wavelength', 'askap:phys.polarization.angle;askap:meta.corrected', 'deg', 3),
(39, 'pol_ang_zero_err', 'casda.polarisation_component', 'DOUBLE', 19, 0, 0, 1, 'Uncertainty in pol_ang_zero', 'stat.error;askap:phys.polarization.angle;askap:meta.corrected', 'deg', 3),
(40, 'pol_frac', 'casda.polarisation_component', 'DOUBLE', 19, 1, 1, 1, 'Fractional polarisation', 'phys.polarization', null, 2),
(41, 'pol_frac_err', 'casda.polarisation_component', 'DOUBLE', 19, 0, 0, 1, 'Uncertainty in fractional polarisation', 'stat.error;phys.polarization', null, 3),
(42, 'complex_1', 'casda.polarisation_component', 'DOUBLE', 19, 0, 0, 1, 'Statistical measure of polarisation complexity', 'stat.value;phys.polarization', null, 3),
(43, 'complex_2', 'casda.polarisation_component', 'DOUBLE', 19, 0, 0, 1, 'Statistical measure of polarisation complexity after removal of a thin-screen model. ', 'stat.value;phys.polarization', null, 3),
(44, 'flag_p1', 'casda.polarisation_component', 'BOOLEAN', 1, 0, 0, 1, 'True if pol_peak_fit is above a threshold value otherwise pol_peak_fit is an upper limit.', 'meta.code',null, 3),
(45, 'flag_p2', 'casda.polarisation_component', 'BOOLEAN', 1, 0, 0, 1, 'True if FDF peak is close to edge', 'meta.code', null, 3),
(46, 'flag_p3', 'casda.polarisation_component', 'VARCHAR', 1, 0, 0, 1, 'Placeholder flag', 'meta.code', null, 3),
(47, 'flag_p4', 'casda.polarisation_component', 'VARCHAR', 1, 0, 0, 1, 'Placeholder flag', 'meta.code', null, 3),
(48, 'quality_level', 'casda.polarisation_component', 'VARCHAR', 15, 1, 1, 1, 'Indicator of quality level.', 'meta.code.qual', null, 3);

-- add foreign keys information
-- for catalogue table to the polarisation_component table
INSERT INTO casda.keys (key_id, from_table, target_table, description)
  VALUES (4, 'casda.polarisation_component', 'casda.catalogue', 'Foreign key from catalogue to polarisation_component table');  
  
INSERT INTO casda.key_columns (id, key_id, from_column, target_column, from_table, target_table)
  VALUES (4, 
    (SELECT key_id FROM casda.keys where from_table = 'casda.polarisation_component' and target_table = 'casda.catalogue'),
    'catalogue_id', 'id', 'casda.polarisation_component', 'casda.catalogue'
  );
  
-- for polarisation_component table to continuum_component table
INSERT INTO casda.keys (key_id, from_table, target_table, description)
  VALUES (5, 'casda.continuum_component', 'casda.polarisation_component', 'Foreign key from polarisation_component to continuum_component table');  
  
INSERT INTO casda.key_columns (id, key_id, from_column, target_column, from_table, target_table)
  VALUES (5, 
    (SELECT key_id FROM casda.keys where from_table = 'casda.continuum_component' and target_table = 'casda.polarisation_component'),
    'component_id', 'component_id', 'casda.continuum_component', 'casda.polarisation_component'
  );
    
  
-- ATLAS information, CASDA-3752
INSERT INTO casda.tables 
(schema_name, table_name, table_type, db_schema_name, db_table_name, description) 
VALUES ('casda', 'casda.atlas_continuum_source', 'table', 'casda', 'atlas_continuum_source', 
'Deep ATLAS radio observations of the CDFS-SWIRE field' ) ;

INSERT INTO casda.columns (column_order, column_name, table_name, description, unit, ucd, utype, datatype, size, principal, indexed, std ) 
values 
(1, 'source_ident', 'casda.atlas_continuum_source', 'Component number', null, 'meta.id;meta.main', null, 'VARCHAR', 4, 1, 1, 0),
(2, 'source_name', 'casda.atlas_continuum_source', 'Designation for the radio component', null, 'meta.id', null, 'VARCHAR', 26, 1, 1, 0),
(3, 'cid', 'casda.atlas_continuum_source', ' Component number corresponding to Table 4', null, 'meta.id.part', null, 'VARCHAR', 19, 1, 0, 0),
(4, 'swire_name', 'casda.atlas_continuum_source', 'SWIRE designated name', null, 'meta.id.assoc', null, 'VARCHAR', 19, 1, 1, 0),
(5, 'ra_h', 'casda.atlas_continuum_source', 'Hour of Right Ascension (J2000)', 'h', 'arith', null, 'INTEGER', 4, 1, 0, 0),
(6, 'ra_m', 'casda.atlas_continuum_source', 'Minute of Right Ascension (J2000)', 'm', 'arith', null, 'INTEGER', 4, 1, 0, 0),
(7, 'ra_s', 'casda.atlas_continuum_source', ' Second of Right Ascension (J2000)', 's', 'arith', null, 'REAL', 4, 1, 0, 0),
(8, 'dec_sign', 'casda.atlas_continuum_source', 'Sign of the Declination (J2000)', null, 'arith', null, 'VARCHAR', 1, 1, 0, 0),
(9, 'dec_deg', 'casda.atlas_continuum_source', 'Degree of Declination (J2000)', 'deg', 'arith', null, 'INTEGER', 4, 1, 0, 0),
(10, 'dec_arcmin', 'casda.atlas_continuum_source', ' Arcminute of Declination (J2000)', 'arcmin', 'arith', null, 'INTEGER', 4, 1, 0, 0),
(11, 'dec_arcsec', 'casda.atlas_continuum_source', 'Arcsecond of Declination (J2000)', 'arcsec', 'arith', null, 'REAL', 4, 1, 0, 0),
(12, 'ra_hms_cont', 'casda.atlas_continuum_source', 'J2000 right ascension (hh:mm:ss.sss)', 'h:m:s', 'pos.eq.ra', null, 'VARCHAR', 20, 1, 1, 0),
(13, 'dec_dms_cont', 'casda.atlas_continuum_source', 'J2000 declination (dd:mm:ss.ss)', 'deg:arcmin:arcsec', 'pos.eq.dec', null, 'VARCHAR', 20, 1, 1, 0),
(14, 'ra_deg_cont', 'casda.atlas_continuum_source', 'J2000 right ascension in decimal degrees', 'deg', 'pos.eq.ra;meta.main', null, 'DOUBLE', 8, 1, 1, 0),
(15, 'dec_deg_cont', 'casda.atlas_continuum_source', 'J2000 declination in decimal degrees', 'deg', 'pos.eq.dec;meta.main', null, 'DOUBLE', 8, 1, 1, 0),
(16, 'flux_20cm', 'casda.atlas_continuum_source', 'Total 20 cm flux density', 'mJy', 'phot.flux.density;em.radio', null, 'REAL', 4, 1, 1, 0),
(17, 'flux_3_6micron', 'casda.atlas_continuum_source', 'The 3.6 micron flux density', 'microJy', 'phot.flux.density;em.ir', null, 'REAL', 4, 1, 1, 0),
(18, 'flux_4_5micron', 'casda.atlas_continuum_source', 'The 4.5 micron flux density', 'microJy', 'phot.flux.density;em.ir', null, 'REAL', 4, 1, 1, 0),
(19, 'flux_5_8micron', 'casda.atlas_continuum_source', 'The 5.8 micron flux density', 'microJy', 'phot.flux.density;em.ir', null, 'REAL', 4, 1, 1, 0),
(20, 'flux_8_0micron', 'casda.atlas_continuum_source', 'The 8.0 micron flux density', 'microJy', 'phot.flux.density;em.ir', null, 'REAL', 4, 1, 1, 0),
(21, 'flux_24micron', 'casda.atlas_continuum_source', 'The 24 micron flux density', 'microJy', 'phot.flux.density;em.ir', null, 'REAL', 4, 1, 1, 0),
(22, 'umag', 'casda.atlas_continuum_source', 'SDSS u band (Vega) magnitude', 'mag', 'phot.mag;em.opt', null, 'REAL', 4, 1, 1, 0),
(23, 'gmag', 'casda.atlas_continuum_source', 'SDSS g band (Vega) magnitude', 'mag', 'phot.mag;em.opt', null, 'REAL', 4, 1, 1, 0),
(24, 'rmag', 'casda.atlas_continuum_source', 'SDSS r band (Vega) magnitude', 'mag', 'phot.mag;em.opt', null, 'REAL', 4, 1, 1, 0),
(25, 'imag', 'casda.atlas_continuum_source', 'SDSS I band (Vega) magnitude', 'mag', 'phot.mag;em.opt', null, 'REAL', 4, 1, 1, 0),
(26, 'zmag', 'casda.atlas_continuum_source', 'SDSS z band (Vega) magnitude', 'mag', 'phot.mag;em.opt', null, 'REAL', 4, 1, 1, 0),
(27, 'redshift', 'casda.atlas_continuum_source', 'Spectroscopic redshift', null, 'src.redshift', null, 'REAL', 4, 1, 1, 0),
(28, 'type', 'casda.atlas_continuum_source', 'Identification type given in Table 5', null, 'meta.code', null, 'INTEGER', 4, 1, 1, 0),
(29, 'class', 'casda.atlas_continuum_source', 'Classification based on Section 3.4 criteria', null, 'meta.code.class', null, 'VARCHAR', 3, 1, 1, 0),
(30, 'basis_class', 'casda.atlas_continuum_source', ' Basis for class', null, 'meta.note', null, 'VARCHAR', 3, 1, 0, 0),
(31, 'comment', 'casda.atlas_continuum_source', 'Additional comments', 'none', 'meta.note', null, 'VARCHAR', 232, 1, 0, 0) ; 
  
