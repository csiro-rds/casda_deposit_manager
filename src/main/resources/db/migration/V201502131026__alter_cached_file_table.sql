-- Increase max size of the path field to 1024

ALTER TABLE casda.cached_file ALTER COLUMN path TYPE character varying(1024);
