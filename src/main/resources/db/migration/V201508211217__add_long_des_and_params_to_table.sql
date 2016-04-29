ALTER TABLE casda.tables ADD COLUMN description_long text ;
COMMENT ON COLUMN casda.tables.description_long is 'Long description of the table';

ALTER TABLE casda.tables ADD COLUMN params text;
COMMENT ON COLUMN casda.tables.params is 'Params of a catalogue provided in deposit file';