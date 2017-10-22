ALTER TABLE casda.image_cube ADD COLUMN level7_collection_id bigint;
ALTER TABLE casda.image_cube ADD CONSTRAINT level7_collection_id_fkey FOREIGN KEY (level7_collection_id) 
	REFERENCES casda.level7_collection (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;  
COMMENT ON COLUMN casda.image_cube.level7_collection_id IS 'The foreign key into the Level7 Collection table.';


ALTER TABLE casda.spectrum ADD COLUMN level7_collection_id bigint;
ALTER TABLE casda.spectrum ADD CONSTRAINT level7_collection_id_fkey FOREIGN KEY (level7_collection_id) 
	REFERENCES casda.level7_collection (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;  
COMMENT ON COLUMN casda.spectrum.level7_collection_id IS 'The foreign key into the Level7 Collection table.';


ALTER TABLE casda.moment_map ADD COLUMN level7_collection_id bigint;
ALTER TABLE casda.moment_map ADD CONSTRAINT level7_collection_id_fkey FOREIGN KEY (level7_collection_id) 
	REFERENCES casda.level7_collection (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;  
COMMENT ON COLUMN casda.moment_map.level7_collection_id IS 'The foreign key into the Level7 Collection table.';


ALTER TABLE casda.thumbnail ADD COLUMN level7_collection_id bigint;
ALTER TABLE casda.thumbnail ADD CONSTRAINT level7_collection_id_fkey FOREIGN KEY (level7_collection_id) 
	REFERENCES casda.level7_collection (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;  
COMMENT ON COLUMN casda.thumbnail.level7_collection_id IS 'The foreign key into the Level7 Collection table.';


ALTER TABLE casda.encapsulation_file ADD COLUMN level7_collection_id bigint;
ALTER TABLE casda.encapsulation_file ADD CONSTRAINT level7_collection_id_fkey FOREIGN KEY (level7_collection_id) 
	REFERENCES casda.level7_collection (id) MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;  
COMMENT ON COLUMN casda.encapsulation_file.level7_collection_id IS 'The foreign key into the Level7 Collection table.';


ALTER TABLE casda.level7_collection ADD COLUMN spectrum_path CHARACTER VARYING(255);
COMMENT ON COLUMN casda.level7_collection.spectrum_path IS 'The path of any spectrum files contained in this collection';

ALTER TABLE casda.level7_collection ADD COLUMN image_cube_path CHARACTER VARYING(255);
COMMENT ON COLUMN casda.level7_collection.image_cube_path IS 'The path of any image cube files contained in this collection';

ALTER TABLE casda.level7_collection ADD COLUMN moment_map_path CHARACTER VARYING(255);
COMMENT ON COLUMN casda.level7_collection.moment_map_path IS 'The path of any moment map files contained in this collection';