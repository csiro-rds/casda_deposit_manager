-- CASDA-3358  - Expose Observation and Project tables for querying by VO TAP

-- Two tables had their first_sbids advewrtised as BIGINTs when they were only INTEGERS 
UPDATE casda.tap_columns set datatype = 'INTEGER', size = 15 where column_name = 'first_sbid' and datatype = 'BIGINT';
