-- This file contains the observation_project view 
--
-- To update this view (which should only be necessary if there is a new data product type:
-- * rename this file with a new timestamp so flyway will re-run it on deploy
-- * make required changes to this file.

DROP VIEW IF EXISTS casda.observation_project;

CREATE VIEW casda.observation_project (observation_id, project_id) AS
SELECT DISTINCT observation_id, project_id
FROM
    (SELECT observation_id, project_id FROM casda.image_cube
    UNION ALL
    SELECT observation_id, project_id FROM casda.catalogue
    UNION ALL
    SELECT observation_id, project_id FROM casda.measurement_set) AS observation_project_view;
  
COMMENT ON VIEW casda.observation_project is 'Derived join table between observation and project.'; 
