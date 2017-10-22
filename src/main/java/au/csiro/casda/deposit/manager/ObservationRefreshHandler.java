package au.csiro.casda.deposit.manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import au.csiro.casda.ResourceNotFoundException;
import au.csiro.casda.Utils;
import au.csiro.casda.deposit.CasdaToolProcessJobBuilderFactory;
import au.csiro.casda.deposit.DepositManagerEvents;
import au.csiro.casda.deposit.jobqueue.QueuePoller;
import au.csiro.casda.deposit.jpa.ObservationRefreshRepository;
import au.csiro.casda.deposit.jpa.ObservationRepository;
import au.csiro.casda.deposit.jpa.RefreshJobRepository;
import au.csiro.casda.deposit.services.NgasService;
import au.csiro.casda.deposit.services.NgasService.ServiceCallException;
import au.csiro.casda.deposit.services.NgasService.Status;
import au.csiro.casda.entity.CasdaDepositableArtefactEntity;
import au.csiro.casda.entity.EncapsulatedDataProduct;
import au.csiro.casda.entity.observation.Cubelet;
import au.csiro.casda.entity.observation.EncapsulationFile;
import au.csiro.casda.entity.observation.FitsObject;
import au.csiro.casda.entity.observation.ImageCube;
import au.csiro.casda.entity.observation.MomentMap;
import au.csiro.casda.entity.observation.Observation;
import au.csiro.casda.entity.observation.Spectrum;
import au.csiro.casda.entity.refresh.ObservationRefresh;
import au.csiro.casda.entity.refresh.RefreshJob;
import au.csiro.casda.entity.refresh.RefreshStateType;
import au.csiro.casda.jobmanager.CasdaToolProcessJobBuilder;
import au.csiro.casda.jobmanager.JobManager;
import au.csiro.casda.jobmanager.JobManager.JobStatus;
import au.csiro.casda.jobmanager.ProcessJob;
import au.csiro.casda.jobmanager.ProcessJobBuilder;
import au.csiro.casda.jobmanager.ProcessJobBuilder.ProcessJobFactory;
import au.csiro.casda.jobmanager.SimpleToolProcessJobBuilder;
import au.csiro.casda.util.EncapsulationTools;

/*
 * #%L
 * CSIRO Data Access Portal
 * %%
 * Copyright (C) 2010 - 2017 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

/**
 * Service object for refreshing the metadata of Observations.
 * <p>
 * Copyright 2017, CSIRO Australia. All rights reserved.
 */
@Component
public class ObservationRefreshHandler
{
    private static final Logger logger = LoggerFactory.getLogger(ObservationRefreshHandler.class);

    private static final String FITS_IMPORTER_TOOL_NAME = "fits_import";

    private ObservationRepository observationRepository;

    private ObservationRefreshRepository observationRefreshRepository;

    private RefreshJobRepository refreshJobRepository;

    private JobManager jobManager;

    private CasdaToolProcessJobBuilderFactory casdaToolBuilderFactory;

    private ProcessJobFactory processJobFactory;

    private NgasService ngasService;

    private EncapsulationTools encapsulationTools;

    private String encapsWorkingPath;

    private String archiveGetCommandAndArgs;

    private String archiveCleanupCommandAndArgs;

    private int daysToRetainData;

    private String depositToolsWorkingDirectory;

    /**
     * Create a new instance of ObservationRefreshHandler
     * 
     * @param observationRepository
     *            The JPA repository for observation objects
     * @param observationRefreshRepository
     *            The JPA repository for ObservationRefresh objects
     * @param refreshJobRepository
     *            The JPA repository for RefreshJob objects
     * @param jobManager
     *            The job manager instance used for job control.
     * @param casdaToolBuilderFactory
     *            A factory for building jobs that use CASDA command line tools.
     * @param processJobFactory
     *            A factory for build arbitrary jobs.
     * @param ngasService
     *            The NGAS service instance managing the archive.
     * @param encapsulationTools
     *            The EncapsulationTools library.
     * @param encapsWorkingPath
     *            The location in which encapsulated files will be extracted to.
     * @param archiveGetCommandAndArgs
     *            The command to get archive files online.
     * @param archiveCleanupCommandAndArgs
     *            The command to push archive files offline.
     * @param daysToRetainData
     *            The number of days since deposit before an observation is pushed offline after refresh
     * @param depositToolsWorkingDirectory
     *            The working directory for slurm jobs
     */
    @Autowired
    public ObservationRefreshHandler(ObservationRepository observationRepository,
            ObservationRefreshRepository observationRefreshRepository, RefreshJobRepository refreshJobRepository,
            JobManager jobManager, CasdaToolProcessJobBuilderFactory casdaToolBuilderFactory,
            ProcessJobFactory processJobFactory, NgasService ngasService, EncapsulationTools encapsulationTools,
            @Value("${observation.refresh.working.directory}") String encapsWorkingPath,
            @Value("${artefact.archive.get.command.and.args}") String archiveGetCommandAndArgs,
            @Value("${artefact.archive.cleanup.command.and.args}") String archiveCleanupCommandAndArgs,
            @Value("${observation.days.to.keep.online}") int daysToRetainData,
            @Value("${deposit.tools.working.directory}") String depositToolsWorkingDirectory)
    {
        this.observationRepository = observationRepository;
        this.observationRefreshRepository = observationRefreshRepository;
        this.refreshJobRepository = refreshJobRepository;
        this.jobManager = jobManager;
        this.casdaToolBuilderFactory = casdaToolBuilderFactory;
        this.processJobFactory = processJobFactory;
        this.ngasService = ngasService;
        this.encapsulationTools = encapsulationTools;
        this.encapsWorkingPath = encapsWorkingPath;
        this.archiveGetCommandAndArgs = archiveGetCommandAndArgs;
        this.archiveCleanupCommandAndArgs = archiveCleanupCommandAndArgs;
        this.daysToRetainData = daysToRetainData;
        this.depositToolsWorkingDirectory = depositToolsWorkingDirectory;
    }

    /**
     * Create a job to refresh the specified observation. No job will be created if the observation is already queued
     * for refreshing.
     * 
     * @param sbid
     *            The scheduling block id of the observation to be refreshed.
     * @return true if the create was successful, false if there was a failure.
     */
    public boolean refreshObservation(Integer sbid)
    {
        Observation observation = observationRepository.findBySbid(sbid);

        // Find if there is already an unprocessed refresh request for this observation
        ObservationRefresh existingTask =
                observationRefreshRepository.findUnprocessedTaskForObservation(observation.getId());
        if (existingTask != null)
        {
            return true;
        }
        // If not create a new one
        RefreshJob refreshJob = new RefreshJob();
        ObservationRefresh refreshTask = new ObservationRefresh();
        refreshTask.setObservationId(observation.getId());
        refreshTask.setSbid(sbid);
        refreshJob.addObservationRefresh(refreshTask);
        refreshJobRepository.save(refreshJob);
        return true;
    }

    /**
     * Create a job to refresh the metadata of all observations. Only those observations not already queued will be
     * included in the job.
     * 
     * @return The number of observations to be refreshed.
     */
    public int refreshAllObservations()
    {
        List<Object[]> observationsToRefresh = observationRefreshRepository.findObservationsToRefresh();
        if (observationsToRefresh.isEmpty())
        {
            return 0;
        }
        RefreshJob refreshJob = new RefreshJob();
        for (Object[] row : observationsToRefresh)
        {
            Long obsId = (Long) row[0];
            Integer sbid = (Integer) row[1];
            ObservationRefresh refreshTask = new ObservationRefresh();
            refreshTask.setObservationId(obsId);
            refreshTask.setSbid(sbid);
            refreshJob.addObservationRefresh(refreshTask);
        }
        refreshJobRepository.save(refreshJob);
        return observationsToRefresh.size();
    }

    /**
     * @return A list of all active observation refresh tasks.
     */
    public List<ObservationRefresh> getActiveRefreshes()
    {
        EnumSet<RefreshStateType> activeStates =
                EnumSet.range(RefreshStateType.UNREFRESHED, RefreshStateType.PROCESSED);
        return observationRefreshRepository.getRefreshesinStates(activeStates);
    }

    /**
     * @param failedCutoff The earliest date to be found.
     * @return A list of the recent failed observation refresh tasks.
     */
    public List<ObservationRefresh> getFailedRefreshesSince(DateTime failedCutoff)
    {
        return observationRefreshRepository.getRefreshesinStatesSince(EnumSet.of(RefreshStateType.FAILED),
                failedCutoff);
    }

    /**
     * @param completedCutoff
     *            The earliest date to search for.
     * @return The number of ObservationRefresh objects that have been completed since the specified date.
     */
    public int countCompletedRefreshesSince(DateTime completedCutoff)
    {
        return observationRefreshRepository.countCompletedRefreshesSince(completedCutoff);
    }

    /**
     * @return The refresh jobs that have not been completed yet.
     */
    public List<RefreshJob> getUncompletedRefreshJobs()
    {
        return refreshJobRepository.getUncompletedRefreshJobs();
    }

    /**
     * @param recentCutoff
     *            The earliest date to search for.
     * @return The list of RefreshJob objects that have been completed since the specified date.
     */
    public List<RefreshJob> findRefreshJobsCompletedSince(DateTime recentCutoff)
    {
        return refreshJobRepository.findRefreshJobsCompletedSince(recentCutoff);
    }

    /**
     * Progress any active observation refresh jobs.
     * 
     * @return true if there were any active refreshes
     */
    @Transactional
    public boolean progressRefreshObservations()
    {
        ObservationRefresh processingObsOffline =
                    observationRefreshRepository.findFirstByRefreshStateOrderById(RefreshStateType.UNREFRESHED);
        if (processingObsOffline != null)
        {
            logger.info("Bringing  sbid " + processingObsOffline.getSbid() + " online");
            ensureObservationIsOnline(processingObsOffline);
        }
        
        // Ready to be processed
        ObservationRefresh processingObsRefresh =
                observationRefreshRepository.findFirstByRefreshStateOrderById(RefreshStateType.PROCESSING);
        if (processingObsRefresh == null)
        {
            processingObsRefresh =
                    observationRefreshRepository.findFirstByRefreshStateOrderById(RefreshStateType.READY);
        }
        if (processingObsRefresh != null)
        {
            logger.info("Progressing refresh of sbid " + processingObsRefresh.getSbid());
            refreshAnObservation(processingObsRefresh);
        }

        // Processed
        ObservationRefresh finaliseObsRefresh =
                observationRefreshRepository.findFirstByRefreshStateOrderById(RefreshStateType.PROCESSED);
        if (finaliseObsRefresh != null)
        {
            finaliseRefresh(finaliseObsRefresh);
        }
        return processingObsRefresh != null || finaliseObsRefresh != null;
    }

    /**
     * Check if the observation to be refreshed has been brought online. If it has then advance the ObservationRefresh
     * object's state to ready. If a job hasn't yet been made to retrieve the files then start the job.
     * 
     * @param observationRefresh  The ObservationRefresh object to be progressed.
     */
    void ensureObservationIsOnline(ObservationRefresh observationRefresh)
    {
        boolean jobFailed = false;
        boolean jobFinished = false;
        String jobId = "Retrieve-obs-" + observationRefresh.getSbid();
        JobStatus jobStatus = jobManager.getJobStatus(jobId);
        if (jobStatus == null)
        {
            try
            {
                jobFinished = startObsRetrievalJob(observationRefresh, jobId);
            }
            catch (ServiceCallException e)
            {
                logger.warn("Call to NGAS failed, will retry next cycle. " + e.getMessage());
            }
            catch (ResourceNotFoundException e)
            {
                logger.error(String.format("Refresh of observation %d failed on job %s. %s",
                        observationRefresh.getSbid(), jobId, e.getMessage()));
                jobFailed = true;
            }
        }
        else if (jobStatus.isFailed())
        {
            logger.debug(String.format("Job %s has failed.", jobId));
            jobFailed = true;
        }
        else if (jobStatus.isFinished())
        {
            jobFinished = true;
        }
        else
        {
            logger.debug(String.format("Job %s is still in status %s.", jobId, jobStatus));
        }

        if (jobFinished)
        {
            logger.info("Files now online for observation " + observationRefresh.getSbid());
            observationRefresh.setRefreshState(RefreshStateType.READY);
            observationRefreshRepository.save(observationRefresh);
        }
        else if (jobFailed)
        {
            logger.error(DepositManagerEvents.E157.messageBuilder().add(observationRefresh.getSbid())
                    .add(" files could not be brought online").toString());
            observationRefresh.setRefreshState(RefreshStateType.FAILED);
            observationRefreshRepository.save(observationRefresh);
        }
        
    }

    /**
     * Start a job to retrieve the observation's files.
     * 
     * @param observationRefresh The ObservationRefresh object to be progressed.
     * @param jobId The id that should eb used when creating the retrieval job.
     * @return true if the job was not needed, false if it was started
     * 
     * @throws ResourceNotFoundException
     *             if there is no corresponding NGAS record for one of the observation's files
     * @throws ServiceCallException
     *             if there is a problem calling NGAS about the status or location of a file.
     */
    boolean startObsRetrievalJob(ObservationRefresh observationRefresh, String jobId)
            throws ServiceCallException, ResourceNotFoundException
    {
        List<String> observationFileList = getObservationFileList(observationRefresh);
        if (observationFileList.isEmpty())
        {
            return true;
        }
        String filePaths = StringUtils.join(observationFileList, " ");
        signalObservationFilesToGoOnline(filePaths, jobId);
        return false;
    }
    

    
    /**
     * Signal DMF to start moving files online
     * 
     * @param filePaths
     *            List of file paths separated by white spaces in string
     * @param jobId
     *            The id that should be used when creating the job
     */
    private void signalObservationFilesToGoOnline(String filePaths, String jobId)
    {
        logger.info(String.format("Bringing paths %s online", filePaths));
        
        // create new process job to get the DMF state for the artifact
        ProcessJobBuilder archiveStatusBuilder = new SimpleToolProcessJobBuilder(processJobFactory,
                Utils.elStringToArray(this.archiveGetCommandAndArgs));
        archiveStatusBuilder.setProcessParameter("dmf_file_locations", filePaths);
        archiveStatusBuilder.setWorkingDirectory(depositToolsWorkingDirectory);
        ProcessJob job = archiveStatusBuilder.createJob(jobId, "retrieve_obs");

        jobManager.startJob(job);
    }
    
    /**
     * Main processing cycle for observation refreshes. Each time it is called it will cycle through all of the FITS
     * objects in the observation trying to progress the refresh of the file's metadata. It will do this by starting 
     * the import jobs for each fits file and checking if they are finished. Once finished the ObservationRefresh is 
     * marked as PROCESSED.
     * 
     * @param observationRefresh The ObservationRefresh object to be progressed.
     */
    void refreshAnObservation(ObservationRefresh observationRefresh)
    {
        QueuePoller qjm = (QueuePoller) jobManager;
        if (observationRefresh.getRefreshState() != RefreshStateType.PROCESSING
                && !qjm.isQueueIdle(FITS_IMPORTER_TOOL_NAME))
        {
            return;
        }
        Observation observation = observationRepository.findBySbid(observationRefresh.getSbid());

        // Check if all the tasks have been completed
        boolean allFinished = true;
        boolean anyFailed = false;
        List<FitsObject> fitsObjects = getFitsObjects(observation);
        for (FitsObject fitsObject : fitsObjects)
        {
            String jobId = getJobId(fitsObject);
            JobStatus jobStatus = jobManager.getJobStatus(jobId);
            if (jobStatus == null)
            {
                allFinished = false;
                try
                {
                    startFitsImportJob(fitsObject, jobId);
                }
                catch (ServiceCallException e)
                {
                    logger.warn("Call to NGAS failed, will retry next cycle. " + e.getMessage());
                }
                catch (ResourceNotFoundException e)
                {
                    logger.error(String.format("Refresh of observation %d failed on job %s. %s", observation.getSbid(),
                            jobId, e.getMessage()));
                    anyFailed = true;
                }
            }
            else if (jobStatus.isFailed())
            {
                logger.debug(String.format("Job %s has failed.", jobId));
                anyFailed = true;
            }
            else if (!jobStatus.isFinished())
            {
                logger.debug(String.format("Job %s is still in status %s.", jobId, jobStatus));
                allFinished = false;
                break;
            }
        }
        if (anyFailed)
        {
            logger.error(DepositManagerEvents.E157.messageBuilder().add(observationRefresh.getSbid())
                    .add("it had failed refreshes").toString());
            observationRefresh.setRefreshState(RefreshStateType.FAILED);
            observationRefreshRepository.save(observationRefresh);
        }
        else if (allFinished)
        {
            logger.info(DepositManagerEvents.E158.messageBuilder().add(observationRefresh.getSbid()).toString());
            observationRefresh.setRefreshState(RefreshStateType.PROCESSED);
            observationRefreshRepository.save(observationRefresh);
        }
        else if (observationRefresh.getRefreshState()  != RefreshStateType.PROCESSING)
        {
            logger.info(DepositManagerEvents.E159.messageBuilder().add(observationRefresh.getSbid()).toString());
            observationRefresh.setRefreshState(RefreshStateType.PROCESSING);
            observationRefreshRepository.save(observationRefresh);
        }

    }

    private void startFitsImportJob(FitsObject fitsObject, String jobId)
            throws ServiceCallException, ResourceNotFoundException
    {
        String fitsType = null;
        if (fitsObject instanceof ImageCube)
        {
            fitsType = "image-cube";
        }
        else if (fitsObject instanceof Spectrum)
        {
            fitsType = "spectrum";
        }
        else if (fitsObject instanceof MomentMap)
        {
            fitsType = "moment-map";
        }
        else if (fitsObject instanceof Cubelet)
        {
            fitsType = "cubelet";
        }
        else
        {
            throw new IllegalArgumentException(
                    "Unexpected type of fitsObject: " + fitsObject.getClass().getCanonicalName());
        }

        CasdaToolProcessJobBuilder processBuilder = casdaToolBuilderFactory.createBuilder();
        Path fileInNgas = getFilePath(fitsObject);
        CasdaToolProcessJobBuilder jobBuilder =
                processBuilder.setCommand(FITS_IMPORTER_TOOL_NAME).addCommandArgument("-infile", fileInNgas.toString())
                        .addCommandArgument("-fitsFilename", fitsObject.getFilename())
                        .addCommandArgument("-fits-type", fitsType);
        jobBuilder.addCommandArgument("-parent-type", "observation");
        jobBuilder.addCommandArgument("-parent-id", fitsObject.getParent().getUniqueId());
        jobBuilder.addCommandSwitch("-refresh");
        final int lowPriority = 5;
        jobManager.startJob(jobBuilder.createJob(jobId, FITS_IMPORTER_TOOL_NAME), lowPriority);
    }

    private Path getFilePath(FitsObject fitsObject) throws ServiceCallException, ResourceNotFoundException
    {
        if (fitsObject instanceof EncapsulatedDataProduct
                && ((EncapsulatedDataProduct) fitsObject).getEncapsulationFile() != null)
        {
            EncapsulationFile encaps = ((EncapsulatedDataProduct) fitsObject).getEncapsulationFile();
            Path encapsPath = findFileInNgas(encaps.getFileId());
            Path extractedPath = Paths.get(encapsWorkingPath, encaps.getParent().getUniqueId(), encaps.getFileId());

            // Check working folder exists
            File workingFolder = extractedPath.getParent().toFile();
            if (!workingFolder.exists())
            {
                workingFolder.mkdirs();
            }
            String jobId = String.format("Extract-%s", fitsObject.getUniqueIdentifier());
            // Extract file to working folder
            ProcessJob job =
                    encapsulationTools.buildExtractJob(jobId, fitsObject, encapsPath, workingFolder.toPath());
            // Run extract job inline
            jobManager.startJob(job);
            JobStatus jobStatus = null;
            do
            {
                jobStatus = jobManager.getJobStatus(jobId);
            }
            while (jobStatus.isReady() || jobStatus.isRunning());

            if (jobStatus.isFailed())
            {
                throw new ResourceNotFoundException("Unable to extract " + fitsObject.getFileId() + " from "
                        + encaps.getFileId() + ". " + jobStatus.getFailureCause());
            }

            // return path of extracted file
            return Paths.get(workingFolder.getAbsolutePath(), fitsObject.getFileId());
        }
        else
        {
            return findFileInNgas(fitsObject.getFileId());
        }

    }

    /**
     * Finds the file path in NGAS corresponding with the given file id.
     * 
     * @param fileId
     *            the data product's file id in ngas
     * @return the file path.
     * @throws ResourceNotFoundException
     *             if there is no corresponding NGAS record for this file id
     * @throws ServiceCallException
     *             if there is a problem calling NGAS about the status or location of the file.
     */
    public Path findFileInNgas(String fileId) throws ServiceCallException, ResourceNotFoundException
    {
        Status ngasStatus = ngasService.getStatus(fileId);
        if (!ngasStatus.wasSuccess())
        {
            throw new ServiceCallException("Request to get status failed from NGAS for file id " + fileId);
        }
        if (StringUtils.isBlank(ngasStatus.getMountPoint()) || StringUtils.isBlank(ngasStatus.getFileName()))
        {
            throw new ResourceNotFoundException(fileId + " does not exist in NGAS");
        }
        Path filepath = Paths.get(ngasStatus.getMountPoint(), ngasStatus.getFileName());
        return filepath;
    }

    /**
     * Tidy up after the metadata of an observation has been refreshed. This will push older data offline, mark the
     * observation refresh as completed and, if it is the last observation, mark the refresh job as complete. If the
     * data is being taken offline then this method may return without updating the status of the ObservationRefresh
     * object. It will do this until the job to take the data offline is complete or failed.
     * 
     * @param observationRefresh
     *            The ObservationRefresh being finalised
     */
    void finaliseRefresh(ObservationRefresh observationRefresh)
    {
        logger.info(String.format("Finalising observation %d.", observationRefresh.getSbid()));
        
        DateTime newObsCutoff = DateTime.now().minusDays(daysToRetainData);
        Observation obs = observationRepository.findOne(observationRefresh.getObservationId());
        if (obs != null && obs.getDepositStarted().isBefore(newObsCutoff))
        {
            boolean jobFailed = false;
            boolean jobFinished = false;
            String jobId = "Cleanup-obs-" + observationRefresh.getSbid();
            JobStatus jobStatus = jobManager.getJobStatus(jobId);
            if (jobStatus == null)
            {
                try
                {
                    jobFinished = startObsCleanupJob(observationRefresh, jobId);
                }
                catch (ServiceCallException e)
                {
                    logger.warn("Call to NGAS failed, will retry next cycle. " + e.getMessage());
                }
                catch (ResourceNotFoundException e)
                {
                    logger.error(String.format("Refresh of observation %d failed on job %s. %s",
                            observationRefresh.getSbid(), jobId, e.getMessage()));
                    jobFailed = true;
                }
            }
            else if (jobStatus.isFailed())
            {
                logger.debug(String.format("Job %s has failed.", jobId));
                jobFailed = true;
            }
            else if (jobStatus.isFinished())
            {
                jobFinished = true;
            }
            else
            {
                logger.debug(String.format("Job %s is still in status %s.", jobId, jobStatus));
            }

            if (jobFailed)
            {
                logger.error(DepositManagerEvents.E157.messageBuilder().add(observationRefresh.getSbid())
                        .add(" files could not be sent offline").toString());
                observationRefresh.setRefreshState(RefreshStateType.FAILED);
                observationRefreshRepository.save(observationRefresh);
                return;
            }
            if (!jobFinished)
            {
                return;
            }
        }
        
        observationRefresh.setRefreshState(RefreshStateType.COMPLETED);
        observationRefreshRepository.save(observationRefresh);

        // Check if the overall refresh job is complete
        RefreshJob refreshJob = observationRefresh.getRefreshJob();
        for (ObservationRefresh task : refreshJob.getObservationRefreshTasks())
        {
            if (task.getRefreshState() != RefreshStateType.COMPLETED)
            {
                return;
            }
        }
        refreshJob.setJobCompleteTime(DateTime.now());
        refreshJobRepository.save(refreshJob);
        
        // Cleanup the working folder
        Path extractedPath = Paths.get(encapsWorkingPath, String.valueOf(observationRefresh.getSbid()));
        try
        {
            FileUtils.deleteDirectory(extractedPath.toFile());
        }
        catch (IOException e)
        {
            logger.error("Unable to delete folder " + extractedPath, e);
        }

    }

    /**
     * Start a job to retrieve the observation's files.
     * 
     * @param observationRefresh The ObservationRefresh object to be progressed.
     * @param jobId The id that should eb used when creating the retrieval job.
     * @return true if the job was not needed, false if it was started
     * 
     * @throws ResourceNotFoundException
     *             if there is no corresponding NGAS record for one of the observation's files
     * @throws ServiceCallException
     *             if there is a problem calling NGAS about the status or location of a file.
     */
    boolean startObsCleanupJob(ObservationRefresh observationRefresh, String jobId)
            throws ServiceCallException, ResourceNotFoundException
    {
        List<String> observationFileList = getObservationFileList(observationRefresh);
        if (observationFileList.isEmpty())
        {
            return true;
        }
        String filePaths = StringUtils.join(observationFileList, " ");
        signalObservationFilesToGoOffline(filePaths, jobId);
        return false;
    }
    

    
    /**
     * Signal DMF to start moving files offline
     * 
     * @param filePaths
     *            List of file paths separated by white spaces in string
     * @param jobId
     *            The id that should be used when creating the job
     */
    private void signalObservationFilesToGoOffline(String filePaths, String jobId)
    {
        logger.info(String.format("Sending paths %s offline", filePaths));
        
        // create new process job to get the DMF state for the artifact
        ProcessJobBuilder archiveStatusBuilder = new SimpleToolProcessJobBuilder(processJobFactory,
                Utils.elStringToArray(this.archiveCleanupCommandAndArgs));
        archiveStatusBuilder.setProcessParameter("dmf_file_locations", filePaths);
        archiveStatusBuilder.setWorkingDirectory(depositToolsWorkingDirectory);
        ProcessJob job = archiveStatusBuilder.createJob(jobId, "cleanup_obs");

        jobManager.startJob(job);
    }

    private List<String> getObservationFileList(ObservationRefresh observationRefresh)
            throws ResourceNotFoundException, ServiceCallException
    {
        Observation obs = observationRepository.findOne(observationRefresh.getObservationId());
        if (obs == null)
        {
            throw new ResourceNotFoundException(
                    "Unable to find observation with id " + observationRefresh.getObservationId());
        }
        Set<CasdaDepositableArtefactEntity> artefacts = new HashSet<>();
        for (FitsObject fitsObject : getFitsObjects(obs))
        {
            if (fitsObject instanceof EncapsulatedDataProduct
                    && ((EncapsulatedDataProduct) fitsObject).getEncapsulationFile() != null)
            {
                EncapsulationFile encaps = ((EncapsulatedDataProduct) fitsObject).getEncapsulationFile();
                artefacts.add(encaps);
            }
            else
            {
                artefacts.add(fitsObject);
            }
        }

        List<String> obsFileList = new ArrayList<>();
        for (CasdaDepositableArtefactEntity casdaDepositableArtefactEntity : artefacts)
        {
            Path filePath = findFileInNgas(casdaDepositableArtefactEntity.getFileId());
            obsFileList.add(filePath.toString());
        }
        return obsFileList;
    }
    
    private String getJobId(FitsObject fitsObject)
    {
        return "Refresh-" + fitsObject.getUniqueIdentifier();
    }

    private List<FitsObject> getFitsObjects(Observation observation)
    {
        List<FitsObject> fitsObjectList = new ArrayList<FitsObject>();
        fitsObjectList.addAll(observation.getImageCubes());
        fitsObjectList.addAll(observation.getSpectra());
        fitsObjectList.addAll(observation.getMomentMaps());
        fitsObjectList.addAll(observation.getCubelets());
        return fitsObjectList;
    }

    void setArchiveGetCommandAndArgs(String archiveGetCommandAndArgs)
    {
        this.archiveGetCommandAndArgs = archiveGetCommandAndArgs;
    }

}
