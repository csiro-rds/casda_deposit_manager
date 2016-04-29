-- This file is automaticaly run by Hibernate when it is configuring.
-- See "Initialize a database using Hibernate" on 
-- http://docs.spring.io/spring-boot/docs/current/reference/html/howto-database-initialization.html
-- for more details.
--
-- This file is for TEST PURPOSES ONLY.

-- Rather than dropping the VIEW, we need to drop the table that gets created during auto-database creation
DROP TABLE IF EXISTS casda.observation_project CASCADE;

-- This is the same view definition as in casda_deposit_manager but has to be on one line (I kid you not).
CREATE VIEW casda.observation_project (observation_id, project_id) AS SELECT DISTINCT observation_id, project_id FROM (SELECT observation_id, project_id FROM casda.image_cube UNION ALL SELECT observation_id, project_id FROM casda.catalogue UNION ALL SELECT observation_id, project_id FROM casda.measurement_set) AS observation_project_view;
