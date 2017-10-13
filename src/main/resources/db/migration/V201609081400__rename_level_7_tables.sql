--     CASDA-5811 - Additional VO metadata for versioned level 7 catalogues

-- First rename the level 7 database tables to have a version
do
$$
declare 
rec record;
begin
for rec in 
SELECT 'ALTER TABLE  casda.'||t.original||' RENAME to '||t.newname||';'  cmd from (
  SELECT  db_table_name original, db_table_name || '_v01' newname
  FROM    casda.tap_tables
  where   schema_name not in ('casda', 'ivoa', 'TAP_SCHEMA', 'public')
          and db_table_name not similar to '%_v(0|1|2|3|4|5|6|7|8|9)+')t
loop
  execute rec.cmd ;
end loop;
end;
$$

