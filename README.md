CASDA Data Deposit
================

This web application provides various CASDA data deposit web services as well as managing the deposit of observations into the archive.  

The web services facilitate the following

* Synchronising Projects between DAP and CASDA - the OpalProjectController provides methods to list new (ie: unknown) projects and to flag existing projects as known.
* Synchronising released Observations between DAP and CASA - the ObservationController provides methods to list released or unreleased Observations and to flag an Observation as released.

The depositing of observations runs using an asynchronous task that starts on application startup and which uses the DepositManagerService.progressObservations(). The processing of data products within an observation is driven by ObservationDepositingDepositState.progress() in the casda_commons project.

The DepositManagerService is responsible for managing the data deposit 'workflow' described [here] (https://jira.csiro.au/browse/CASDA-832)  The service has a 'run-loop' that polls the RTC for new observations to import, and also attempts to 'progress' the deposit of all un-deposited observations into the archive.

The RTC 'notifies' us of the existence of a new observation to archive by writing a `READY` file to the observation folder. The RTC poller looks for `READY` files within the observation folders and uses the `observation_import` data_deposit command line tool to import the observation metadata file (thereby creating initial corresponding records in the CASDA database).

Observations logically comprise a set of 'artefacts' (ie: files) that need to be deposited into the archive.  An Observation and its artefacts (collectively known as 'depositables') go through a sequence of states to become deposited.  The actions required to progress a depositable are performed by data_deposit command-line tools, namely:

* `fits_import` - extracts image metadata from image cubes
* `catalogue_import` - extracts catalogue entries from a catalogue file
* `stage_artefact` - copies artefacts from the RTC onto a 'staging' area on NGAS ready for the artefact to be 'registered' with NGAS
* `register_artefact` - takes an artefact in the NGAS 'staging' area and asks NGAS to put it under its management
* `rtc_notify` - 'notifies' the RTC that the deposit has completed (by writing a DONE file)


Setting up
----------

This project assumes that Eclipse is being used.  Having checked out the project from Stash into the standard project location (ie: 'C:\Projects\Casda'), you can import it into Eclipse as a Gradle project.  You will then need to right-click on the project and do a Gradle -> Refresh All.  

Please then follow the instructions at [https://wiki.csiro.au/display/CASDA/CASDA+Application+setup+-+common] (https://wiki.csiro.au/display/CASDA/CASDA+Application+setup+-+common) to ensure that the code templates and code formatting rules are properly configured.

Make sure you have Postgres installed locally, see [https://wiki.csiro.au/display/CASDA/Development+Environment+setup](https://wiki.csiro.au/display/CASDA/Development+Environment+setup).

Use the pgAdmin console to create the `casdbusr` user and a `casda` database as follows:

    * Right-click on Login Roles node and select New Login Role
        - Enter the user name of 'casdbusr' in the Properties tab
        - Enter a password of 'password' on the Definition tab
        - Click OK
    * Right-click on the Databases node and select New Database
        - Name the database 'casda' and change the owner to 'casdbusr'
    * Double-click on the new database to connect to it
    * Right-click on the Schemas node under the database and select New Schema
        - Enter 'casda' as the schema name and change the owner to casdbusr.
        
We are using the pg_sphere extension on the servers but that is difficult to install on a Windows machine. Instead we can create a DOMAIN in Postgres, that maps the 'spoly' type to text. To create the DOMAIN, use the pgAdmin console as follows:
    * Expand the 'Databases' list, and left-click on the casda database
        - Click on the SQL menu button
        - run the following command: `CREATE DOMAIN spoly AS text;`
        
To use this, you'll also need to make sure that the `spring.jpa.database-platform` property is set to `au.csiro.casda.LocalPgSphericalDialect` (from casda_commons), configured in src/test/resources/application-casda_deposit_manager.properties   

Make sure the databases are intialised for `flyway`:

> `gradle flywayInit`

At this point you could also run `gradle flywayMigrate` to populate the database or let it auto-configure when you run-up the locally deployed application.


Eclipse
-------

The CASDA Deposit Manager will run without any special configuration within Eclipse.  However, this will only allow you to debug the deposit manager itself - the other command line tools will be run as external processes that you won't be able to debug.

To use these data deposit tools locally with deposit manager, please follow the instructions on [this page](https://stash.csiro.au/projects/CASDA/repos/data_deposit/browse).

Junit test should also need no special configuration to run in Eclipse.

Running the Tests Locally
-------------------------

	> `gradle clean test`

Building and Installing Locally
-------------------------------

	> gradle clean deployToLocal

This will build and deploy the war to your local tomcat installation (along with an application context file.)


Running Locally
---------------

Unreleased observations can be seen here:

	> `https://localhost:8443/casda_deposit_manager/observation/unreleased`

and released ones here:

	> `https://localhost:8443/casda_deposit_manager/observation/released?projectCode={projectCode}`

To release a project, execute the following

	> `curl --insecure -X POST https://localhost:8443/casda_deposit_manager/observation/{sbid}/release -d ""`

Unknown projects can be seen here:

	> `https://localhost:8443/casda_deposit_manager/opalProjectSync/getNewProjects`

To update a project, execute the following:

	> `curl --insecure -X POST --header "Content-Type: application/x-www-form-urlencoded" https://localhost:8443/casda_deposit_manager/opalProjectSync/updateProject -d "opalCode={opalCode}&shortName={shortName}&principalFirstName={principalFirstName}&principalLastName={principalLastName}"`

The deposit manager health can be seen at:

	> `https://localhost:8443/casda_deposit_manager/health`

and the status of deposits here:

	> `https://localhost:8443/casda_deposit_manager/deposit_status`

From that page you can also drill-down to see the details of a particular deposit.  (You can also recover a failed deposit from the detail view.)

To initiate a deposit, you should clone an existing observation in `data_deposit`'s `src/test/resources/deposit` directory using the `clone_observation.sh` script.  Then you will need to create the `READY` file in the new observation's directory. 

The tomcat server can be stopped and started with the following gradle commands:

	> `gradle localTomcatStop`

	> `gradle localTomcatStart`


Clearing Your Local Database
----------------------------

If you need a clean database (eg: loading the same datafile twice will result in an error due to a conflict with the 
same observation ID), then you can clear it by running the following gradle task:

	>gradle flywayClean flywayMigrate

*Please Note:* If this command outputs any flyway debug then something is probably *wrong* with the flyway scripts.  Please investigate!

Configuration
-------------

### Logging

The technique for loading the log4j configuration file relies on it being present under a `config` folder in the classpath *or* relative to the application's 'run' directory.  On the servers, the run directory is `$CATALINA_HOME` and so the log4j configuration file has to be located there, at: `$CATALINA_HOME/config/CasdaDepositManager-log4j2.xml`.  When running locally, the run directory is `$CATALINA_HOME/bin` and the configuration is picked up from `$CATALINA_HOME/bin/config/CasdaDepositManager-log4j2.xml`.  When running under Eclipse, the log configuration file is copied into the local Eclipse-managed server's WEB-INF/classes directory (as it's in the classpath by dint of being located under src/test/resources) and loaded via the classpath.

### Application Properties

The system uses Spring Boot's mechanism for loading application properties.  When running on a server, the properties are drawn from two locations:

    * $CATALINA_HOME/webapps/casda_deposit_manager/WEB-INF/classes/application.properties
    * $CATALINA_HOME/config/application-casda_deposit_manager.properties

The second file is loaded because it is under a `config` directory relative to the 'run' directory.  Locally the run directory is different and the 'local' file is picked up at:

    * $CATALINA_HOME/bin/config/application-casda_deposit_manager.properties

When running under Eclipse, the `src/test/resources/config` folder is copied into the local Eclipse-managed server's WEB-INF/classes directory (as it's in the classpath by dint of being located under src/test/resources).

Test cases always load application.properties files manually.

### Pure-local configuration

The default local configuration for the DepositManagerService is to use the NGAS server running in the dev environment.  This can be changed to a pure local mode where the staging and registering operations are replaced with local 'no-op'-style command-line calls.  Please note that the archiving service needs to talk to NGAS to obtain an exact file location so data deposit will most likely break at the ARCHIVING stage - the exception to this is when NGAS happens to contain a file with the same file_id as the artefact in which case the archiving 'no-op' will actually succeed.

You will need to make the following links for the 'no-op' commands to work.

*cygwin*

    ln -s /cygdrive/c/Dev/cygwin /dev
    ln -s /cygdrive/c/Projects /

*OS/X*

    sudo mkdir -p /dev/cygwin/bin
    ln -s /bin/bash /dev/cygwin/bin
    
    (also need to link your Projects directory to /Projects)

See the application-casda_deposit_manager.properties for which services can be run 'no-op'-style.

Command Line Tools
------------------
Due to a known intermittent (appears and disappears from release to release) Java bug which occurs when the JVM calls fork()+exec() too often, this component uses a Python service to run commands on the server (the script can be found [here](https://bitbucket.csiro.au/projects/CASDA/repos/casda-runcommand-service/browse)). 
The properties file for the run command service has a whitelist of commands that may be run, any changes to the commands or their arguments means the whitelist must be updated.

The properties file can be found in [server_config](https://bitbucket.csiro.au/projects/CASDA/repos/server_config/browse) at these addresses:


- server_config/browse/pawsey/dev/files/ASKAP/access/dev/casda_deposit_tools/runcommand/config
- server_config/browse/pawsey/test/files/ASKAP/access/test/casda_deposit_tools/runcommand/config
- server_config/browse/pawsey/at/files/ASKAP/access/at/casda_deposit_tools/runcommand/config
- server_config/browse/pawsey/prd/files/ASKAP/prd-access/prd/casda_deposit_tools/runcommand/config
        
        
Updates the license header of the current project source files
--------------------------------------------------------------
Change the relevant value in pom.xml then run this command in command prompt:

$ mvn license:update-file-header
