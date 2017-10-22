-- CASDA-3358  - Expose Observation and Project tables for querying by VO TAP

INSERT INTO casda.tap_tables (schema_name, table_name, table_type, db_schema_name, db_table_name, description, scs_enabled)
VALUES ('casda', 'casda.observation', 'table', 'casda', 'observation', 'Describes an observation (e.g. start and end times, which telescope made the observation etc)', false);

INSERT INTO casda.tap_columns (column_order, column_name, db_column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit)
VALUES 
(1,'id', 'id', 'casda.observation', 'BIGINT', 19, 1, 1, 1, 'The primary key', 'meta.record', null),
(2,'obs_start', 'obs_start', 'casda.observation', 'TIMESTAMP', 24, 1, 1, 1, 'Observation Start Time/Date', 'time.start;obs.exposure', null),
(3,'obs_end', 'obs_end', 'casda.observation', 'TIMESTAMP', 24, 1, 1, 1, 'Observation Stop Time/Date', 'time.end;obs.exposure', null),
(4,'obs_start_mjd', 'obs_start_mjd', 'casda.observation', 'DOUBLE', 19, 0, 0, 1, 'Observation Start Time/Date in Modified Julian Date format', 'time.start;obs.exposure', 'd'),
(5,'obs_end_mjd', 'obs_end_mjd', 'casda.observation', 'DOUBLE', 19, 0, 0, 1, 'Observation Stop Time/Date in Modified Julian Date format', 'time.end;obs.exposure', 'd'),
(6,'telescope', 'telescope', 'casda.observation', 'VARCHAR', 255, 1, 1, 1, 'The telescope name', 'meta.id;instr.tel', null),
(7,'sbid', 'sbid', 'casda.observation', 'INTEGER', 15, 1, 1, 1, 'This uniquely identifies the observation in the telescope operating system and central processor sub-systems', 'meta.id;meta.main', null),
(8,'obs_program', 'obs_program', 'casda.observation', 'VARCHAR', 255, 1, 1, 1, 'A collection of scheduling blocks', 'meta.id', null),
(9,'deposit_state', 'deposit_state', 'casda.observation', 'VARCHAR', 255, 0, 0, 1, 'Deposit state of observation as a whole', 'meta.code.status', null)
;

-- Project already exists, just need to add the PI details

INSERT INTO casda.tap_columns (column_order, column_name, db_column_name, table_name, datatype, size, principal, indexed, std, description, ucd, unit)
VALUES 
--(1,'id', 'id', 'casda.project', 'BIGINT', 19, 1, 0, 1, 'The primary key', 'meta.record', null),
--(2,'opal_code', 'opal_code', 'casda.project', 'VARCHAR', 255, 1, 0, 1, 'OPAL ID of the project', 'meta.id;meta.main', null),
--(3,'short_name', 'short_name', 'casda.project', 'VARCHAR', 255, 1, 0, 1, 'The short (human-friendly) name of the project', 'meta.id', null),
(4,'principal_first_name', 'principal_first_name', 'casda.project', 'VARCHAR', 255, 1, 0, 1, 'First name of the principal investigator for the project', 'meta.id.PI', null),
(5,'principal_last_name', 'principal_last_name', 'casda.project', 'VARCHAR', 255, 1, 0, 1, 'Last name of the principal investigator for the project', 'meta.id.PI;meta.main', null)
;

-- Add foreign keys

INSERT INTO casda.tap_keys (key_id, from_table, target_table, description )
VALUES 
('observation_catalogue', 'casda.catalogue', 'casda.observation', 'Foreign key from catalogue to observation table'),
('observation_continuum_component', 'casda.continuum_component', 'casda.observation', 'Foreign key from continuum_component to observation table'),
('observation_continuum_island', 'casda.continuum_island', 'casda.observation', 'Foreign key from continuum_island to observation table'),
('observation_polarisation_component', 'casda.polarisation_component', 'casda.observation', 'Foreign key from polarisation_component to observation table'),
('observation_spectral_line_absorption', 'casda.spectral_line_absorption', 'casda.observation', 'Foreign key from spectral_line_absorption to observation table'),
('observation_spectral_line_emission', 'casda.spectral_line_emission', 'casda.observation', 'Foreign key from spectral_line_emission to observation table')
;


INSERT INTO casda.tap_key_columns (id, key_id, from_column, target_column, from_table, target_table )
VALUES 
((SELECT max(id) + 1 from casda.tap_key_columns), 'observation_catalogue', 'observation_id', 'id', 'casda.catalogue', 'casda.observation'),
((SELECT max(id) + 2 from casda.tap_key_columns), 'observation_continuum_component', 'first_sbid', 'sbid', 'casda.continuum_component', 'casda.observation'),
((SELECT max(id) + 3 from casda.tap_key_columns), 'observation_continuum_island', 'first_sbid', 'sbid', 'casda.continuum_island', 'casda.observation'),
((SELECT max(id) + 4 from casda.tap_key_columns), 'observation_polarisation_component', 'first_sbid', 'sbid', 'casda.polarisation_component', 'casda.observation'),
((SELECT max(id) + 5 from casda.tap_key_columns), 'observation_spectral_line_absorption', 'first_sbid', 'sbid', 'casda.spectral_line_absorption', 'casda.observation'),
((SELECT max(id) + 6 from casda.tap_key_columns), 'observation_spectral_line_emission', 'first_sbid', 'sbid', 'casda.spectral_line_emission', 'casda.observation')
;
