ALTER TABLE casda.encapsulation_file ADD COLUMN file_pattern character varying(1000) NOT NULL;

COMMENT ON COLUMN casda.encapsulation_file.file_pattern is 'the pattern which describes all the smaller files encapsulated in this file';