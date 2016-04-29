-- CASDA-4487 renamed collection_id column to dap_collection_id, for level 7 collection
ALTER TABLE casda.level7_collection RENAME COLUMN collection_id TO dap_collection_id; 