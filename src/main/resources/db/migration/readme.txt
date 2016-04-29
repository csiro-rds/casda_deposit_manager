This folder contains database migration scripts to be applied on startup of the project.
Each script is to be named 

Vyyyymmddhhmi__name.sql

Where yyymmmddhhmi is the date time string when the script was created. This defines the order in which FlyWay runs 
the scripts.

Note: If the script is created before but committed after a test release it will need to be renamed to be later than 
the time of the test release.  

See http://flywaydb.org/getstarted/firststeps/api.html for an intro on using the migration scripts.