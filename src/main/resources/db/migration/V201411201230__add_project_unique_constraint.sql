-- project is unique as it is a lookup table 
ALTER TABLE casda.project ADD CONSTRAINT unique_project_opal_code UNIQUE (opal_code);