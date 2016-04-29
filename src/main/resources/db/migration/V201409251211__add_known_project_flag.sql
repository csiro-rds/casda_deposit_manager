-- Adds a flag to the project table indicating that the project is known to DAP 
-- see CASDA-931 OPAL Data Sync

ALTER TABLE casda.project ADD known_project BOOLEAN DEFAULT FALSE;

CREATE INDEX idx_project_known_project ON casda.project( known_project );

COMMENT ON COLUMN casda.project.known_project is 'A flag indicating that the project is known to DAP (an OPAL data sync has been performed)';