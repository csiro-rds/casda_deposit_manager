-- Add principal investigator to projects table

ALTER TABLE casda.project ADD COLUMN principal_first_name VARCHAR( 255 );
COMMENT ON COLUMN casda.project.principal_first_name is 'First name of the principal investigator for the project';

ALTER TABLE casda.project ADD COLUMN principal_last_name VARCHAR( 255 );
COMMENT ON COLUMN casda.project.principal_last_name is 'Last name of the principal investigator for the project';