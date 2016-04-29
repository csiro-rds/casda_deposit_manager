package au.csiro.casda.deposit.manager;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import au.csiro.casda.deposit.DepositManagerEvents;
import au.csiro.casda.deposit.exception.ImportException;
import au.csiro.casda.deposit.exception.PollingException;
import au.csiro.casda.deposit.jpa.ObservationRepository;
import au.csiro.casda.entity.observation.Observation;
import au.csiro.casda.jobmanager.CasdaToolProcessJobBuilder;
import au.csiro.casda.jobmanager.JobManager;
import au.csiro.casda.jobmanager.JobManager.JobStatus;
import au.csiro.casda.jobmanager.ProcessJob;
import au.csiro.casda.jobmanager.ProcessJobBuilder.ProcessJobFactory;

/*
 * #%L
 * CSIRO ASKAP Science Data Archive
 * %%
 * Copyright (C) 2015 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

/**
 * ObservationsJobsManager to do the work on deposited Job and called from DepositManager. Specifically it will scan the
 * given directory set in setDepositObservationParentDirectory() looking for directories (one level down only) with a
 * READY file. It will then call the commandLineImporter tool on the observation.xml file that must exist in this
 * directory.
 * <p>
 * The Directory structure at and below a given search directory is: <code>
        top_level_dir_containing_obs
            obs_dir1
                READY
                Catalogue
                Image
                ...
            obs_dir2
                READY
                Catalogue
                Image
                ...
 * </code>
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 * 
 */
@Component
public class ObservationsJobsHandler
{
    private static final Logger logger = LoggerFactory.getLogger(ObservationsJobsHandler.class);

    /**
     * Max depth under the root directory that is searched for deposit jobs' files
     */
    static final Integer MAX_DEPTH = 2;
    /**
     * Name of the Observation XML file expected in the deposited jobs directories
     */
    static final String OBSERVATION_XML_FILE_NAME = "observation.xml";

    private static final long SLEEP_TIME_BETWEEN_CHECKING_IF_DEPOSIT_JOB_FINISHED = 500;

    private static final String READY_FILE_NAME = "READY";
    private static final String DONE_FILE_NAME = "DONE";

    /** The name of the observation importer command. */
    private static final String OBSERVATION_COMMAND_LINE_IMPORTER_TOOL_NAME = "observation_import";

    private ProcessJobFactory processJobFactory;

    private JobManager jobManager;

    private String depositToolsWorkingDirectory;

    private String depositToolsScriptExtension;

    private String depositToolsInstallationDirectory;

    private ObservationRepository observationRepository;

    /**
     * Creates an ObservationJobsHandler configured with the given params
     * 
     * @param processJobFactory
     *            a ProcessJobFactory used to instantiate a specific concrete subclass of ProcessJob
     * @param depositToolsWorkingDirectory
     *            used to configure the working directory of the external process used to import the observation
     * @param depositToolsInstallationDirectory
     *            used to configure the commandPath of the external process used to import the observation
     * @param depositToolsScriptExtension
     *            used to configure the command extension of the external process used to import the observation
     * @param jobManager
     *            a JobManager that will be used to import the observation metadata files
     * @param observationRepository
     *            an ObservationRepository used to access information about existing Observations
     */
    @Autowired
    public ObservationsJobsHandler(ProcessJobFactory processJobFactory,
            @Value("${deposit.tools.working.directory}") String depositToolsWorkingDirectory,
            @Value("${deposit.tools.installation.directory}") String depositToolsInstallationDirectory,
            @Value("${deposit.tools.script.extension}") String depositToolsScriptExtension,
            @Qualifier("observationImportJobManager") JobManager jobManager,
            ObservationRepository observationRepository)
    {
        this.processJobFactory = processJobFactory;
        this.jobManager = jobManager;
        this.depositToolsWorkingDirectory = depositToolsWorkingDirectory;
        this.depositToolsScriptExtension = depositToolsScriptExtension;
        this.depositToolsInstallationDirectory = depositToolsInstallationDirectory;
        this.observationRepository = observationRepository;
    }

    /**
     * Run the deposit job on the given directory. Scan that looking for subdirs (one level deep) with READY files and
     * then process that directory as per @link ObservationCommandLineImporter.
     * 
     * @param depositObservationParentDirectory
     *            is the dir to run the deposit job on.
     * @throws PollingException
     *             when there is an exception with the directory polling
     */
    public void run(String depositObservationParentDirectory) throws PollingException
    {
        logger.debug("ObservationJobsHandler.run() - Poll directory for new observations: "
                + depositObservationParentDirectory);

        List<Path> paths = findNewObservationDirs(depositObservationParentDirectory);

        if (paths != null)
        {
            for (Path path : paths)
            {
                try
                {
                    runJob(path);
                }
                catch (ImportException e)
                {
                    logger.error(DepositManagerEvents.E073.messageBuilder().add(e.getSbid()).toString(), e);
                }
            }
        }
    }

    /**
     * 
     * @param depositObservationsParentDir
     *            is the parent dir to search from that contains the observation directories
     * @return the absolute path to the observation dirs (directly) under parentDir that contain a READY file but NOT A
     *         DONE file.
     * @throws PollingException
     *             if there is an IO Exception
     */
    List<Path> findNewObservationDirs(String depositObservationsParentDir) throws PollingException
    {
        if (StringUtils.isBlank(depositObservationsParentDir))
        {
            throw new IllegalArgumentException("depositObservationParentDirectory isBlank");
        }

        List<Path> readyNotDonePaths = new ArrayList<>();
        for (Path obsDir : getAllObservationDirs(depositObservationsParentDir))
        {
            if (observationDirHasFile(obsDir, READY_FILE_NAME) && !observationDirHasFile(obsDir, DONE_FILE_NAME))
            {
                readyNotDonePaths.add(obsDir);
            }
        }
        return readyNotDonePaths;
    }

    /**
     * @param parentDir
     *            is the 'ROOT' directory.
     * @return the dirs under the given 'ROOT' directory which are the Observation directories (they are the immediate
     *         children of the parentDir).
     * @throws PollingException
     *             an exception
     */
    protected List<Path> getAllObservationDirs(String parentDir) throws PollingException
    {

        Path parentDirPath = Paths.get(parentDir);  
        try (DirectoryStream<Path> observationDirs = Files.newDirectoryStream(parentDirPath))
        {
            List<Path> paths = new ArrayList<>();  
            for(Path p : observationDirs)
            {
                if(Files.isDirectory(p))
                {
                    if(Files.isSymbolicLink(p) && Files.isDirectory(p))
                    {
                        paths.add(Files.readSymbolicLink(p));
                    }
                    else
                    {
                        paths.add(p);
                    }     
                }
            }
            return paths;
        }
        catch (IOException ex)
        {
            throw new PollingException(ex);
        }
    }

    /**
     * @param observationDir
     *            to check for the file in
     * @param fileToFind
     *            is the file to find in the observationDir
     * @return if the file exists
     * @throws PollingException
     *             if there is an IOException
     */
    private boolean observationDirHasFile(Path observationDir, String fileToFind) throws PollingException
    {
        Path fileToFindPath = observationDir.resolve(fileToFind);
        int MAX_DEPTH_BELOW_OBS_DIR = MAX_DEPTH - 1;
        try (Stream<Path> readyFilesDirs = Files.find(observationDir, MAX_DEPTH_BELOW_OBS_DIR, (entry, attribute) -> {
            return (entry.equals(fileToFindPath));
        }))
        {
            List<Path> paths = readyFilesDirs.collect(Collectors.toList());
            return paths.size() > 0;
        }
        catch (IOException ex)
        {
            throw new PollingException(ex);
        }
    }

    /**
     * Given an observation directory with a READY file (ie is ready for ingest), create an observation import job @link
     * ObservationCommandLineImporter
     * 
     * @param observationDir
     *            absolute path to the sub-directory (one level deep) under the parent with READY file and NO DONE file
     * @throws ImportException
     *             if the jobManager process fails. The Job manager is responsible for running the @link
     *             ObservationCommandLineImporter.
     */
    void runJob(Path observationDir) throws ImportException
    {
        logger.debug("ObservationJobsHandler.runJob() - observationDir: {}", observationDir);
        String sbid = observationDir.getFileName().toString();
        Observation observation;

        try
        {
            observation = observationRepository.findBySbid(Integer.parseInt(sbid));
        }
        catch (NumberFormatException | DataAccessException e)
        {
            throw new ImportException(sbid, e);
        }
        if (observation == null)
        {

            String jobId = OBSERVATION_COMMAND_LINE_IMPORTER_TOOL_NAME + "-" + sbid;
            String obsXmlFilePath = observationDir.resolve(OBSERVATION_XML_FILE_NAME).toString();
            String jobPath = observationDir.resolve(OBSERVATION_XML_FILE_NAME).toString();

            ProcessJob importJob =
                    new CasdaToolProcessJobBuilder(this.processJobFactory, this.depositToolsWorkingDirectory,
                            this.depositToolsInstallationDirectory, this.depositToolsScriptExtension)
                                    .setCommand(OBSERVATION_COMMAND_LINE_IMPORTER_TOOL_NAME)
                                    .addCommandArgument("-sbid", sbid).addCommandArgument("-infile", jobPath)
                                    .createJob(jobId, OBSERVATION_COMMAND_LINE_IMPORTER_TOOL_NAME);
            jobManager.startJob(importJob);
            JobStatus jobStatus = jobManager.getJobStatus(jobId);
            while (!(jobStatus.isFinished() || jobStatus.isFailed()))
            {
                try
                {
                    Thread.sleep(SLEEP_TIME_BETWEEN_CHECKING_IF_DEPOSIT_JOB_FINISHED);
                }
                catch (InterruptedException e)
                {
                    throw new ImportException(sbid, e);
                }
                jobStatus = jobManager.getJobStatus(jobId);
            }
            if (jobStatus.isFailed())
            {
                logger.error("Job {} failed during observation import with output :{}", jobId,
                        jobStatus.getJobOutput());
                throw new ImportException(OBSERVATION_COMMAND_LINE_IMPORTER_TOOL_NAME, sbid, obsXmlFilePath,
                        jobStatus.getFailureCause());
            }
            else
            {
                // LOG E35 - new data
                logger.info(DepositManagerEvents.E035.messageBuilder().add(sbid).toString());
            }
        }

    }
}
