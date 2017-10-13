package au.csiro.casda.deposit.state;

import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.csiro.casda.datadeposit.ArchivingDepositState;
import au.csiro.casda.datadeposit.ChildDepositableArtefact;
import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.deposit.SingleJobMonitorFactory;
import au.csiro.casda.deposit.services.NgasService;
import au.csiro.casda.deposit.services.NgasService.ServiceCallException;
import au.csiro.casda.deposit.services.NgasService.Status;
import au.csiro.casda.jobmanager.ProcessJob;
import au.csiro.casda.jobmanager.ProcessJobBuilder;
import au.csiro.casda.jobmanager.SingleJobMonitor;

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
 * Extension of ArchivingDepositState that ensures that a DepositableArtefact's backing file has been archived and that
 * dual copies exist
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
public abstract class CasdaArchivingDepositState extends ArchivingDepositState
{
    private static final String DMF_FILE_LOCATION_PARAMETER = "dmf_file_location";

    private static final Logger logger = LoggerFactory.getLogger(CasdaArchivingDepositState.class);

    private final String artifactId;
    private final NgasService ngasService;
    private final ProcessJobBuilder archiveStatusBuilder;
    private final ProcessJobBuilder archivePutBuilder;
    private final SingleJobMonitorFactory singleJobMonitorFactory;

    /**
     * Constructor
     * 
     * @param stateFactory
     *            see {@link DepositState}
     * @param depositableArtefact
     *            the DepositableArtefact that the state pertains to
     * @param ngasArtefactVolumeMap
     *            a map from DepositableArtefact 'types' to NGAS volume names (used to control which volume an artefact
     *            is staged to).
     * @param ngasService
     *            a service to access ngas file manager.
     * @param archiveStatusBuilder
     *            a ProcessJobBuilder that can be used to check the archive status for an artefact.
     * @param archivePutBuilder
     *            a ProcessJobBuilder that can be used to request that an artefact be dual stated.
     * @param singleJobMonitorFactory
     *            the SingleJobMonitorFactory
     */
    protected CasdaArchivingDepositState(DepositStateFactory stateFactory,
            ChildDepositableArtefact depositableArtefact, Map<String, String> ngasArtefactVolumeMap,
            NgasService ngasService, ProcessJobBuilder archiveStatusBuilder, ProcessJobBuilder archivePutBuilder,
            SingleJobMonitorFactory singleJobMonitorFactory)
    {
        super(stateFactory, depositableArtefact);
        this.artifactId = depositableArtefact.getUniqueIdentifier().replace("/", "-");
        this.ngasService = ngasService;
        this.archiveStatusBuilder = archiveStatusBuilder;
        this.archivePutBuilder = archivePutBuilder;
        this.singleJobMonitorFactory = singleJobMonitorFactory;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void progress()
    {
        String filePath = null;
        try
        {
            Status status = ngasService.getStatus(this.artifactId);
                        
            if (status.wasFailure())
            {
                // NGAS cannot find ID
                logger.error("NGAS cannot find artifact {} and returned '{}'", this.artifactId,
                        status.toString());
                transitionTo(DepositState.Type.FAILED);
            }

            filePath = StringUtils.trimToEmpty(status.getMountPoint()) + IOUtils.DIR_SEPARATOR
                    + StringUtils.trimToEmpty(status.getFileName());

        }
        catch (ServiceCallException e)
        {
            String logMessage = getLogMessageForArchivingProgressWarning();
            logger.warn(logMessage, e);

            // this can be retried until ngas is back.
            return;
        }
        // create new process job to get the DMF state for the artifact
        archiveStatusBuilder.setProcessParameter(DMF_FILE_LOCATION_PARAMETER, filePath);
        ProcessJob job = archiveStatusBuilder.createJob(null, null);

        SingleJobMonitor monitor = singleJobMonitorFactory.createSingleJobMonitor();
        // runs inline
        job.run(monitor);
        if (monitor.isJobFailed())
        {
            logger.error("Checking status of artefact on the DMF using command {} failed, reason: {}", job
                    .getDescription(), StringUtils.isBlank(monitor.getJobOutput()) ? "<NO OUTPUT FROM PROCESS>"
                    : monitor.getJobOutput());
            transitionTo(DepositState.Type.FAILED);
        }
        else if (monitor.isJobFinished())
        {
            switch (monitor.getJobOutput())
            {
            case "OFL":
                // already dual state
                super.progress();
                break;
            case "DUL":
                // already dual state
                super.progress();
                break;
            case "MIG":
                // already in progress
                break;
            case "REG":
                // need to request dual state
                this.requestDualState(filePath);
                break;
            case "dmattr information not available":
                // DMF can be down for periods of time - we just ride it out
                break;
            default:
                // unknown file status
                logger.error("Checking status of artefact on the DMF using command {} failed, unknown status: '{}'",
                        job.getDescription(), monitor.getJobOutput());
                transitionTo(DepositState.Type.FAILED);
            }
        }
    }
    
    /**
     * Template method that subclasses must implement that returns the appropriate the warning log message to report
     * when there is an issue progressing during the archiving step. (Subclasses will specialise on different error
     * codes and parameters.)
     * 
     * @return the log message
     */
    protected abstract String getLogMessageForArchivingProgressWarning();

    /**
     * {@inheritDoc}
     */
    @Override
    public ChildDepositableArtefact getDepositable()
    {
        return (ChildDepositableArtefact) super.getDepositable();
    }

    private void requestDualState(String filePath)
    {
        // create new process job to get the DMF state for the artifact
        archivePutBuilder.setProcessParameter(DMF_FILE_LOCATION_PARAMETER, filePath);
        ProcessJob job = archivePutBuilder.createJob(null, null);

        SingleJobMonitor monitor = singleJobMonitorFactory.createSingleJobMonitor();
        // runs inline
        job.run(monitor);
        // mark as failed if put command did not complete otherwise status will be checked next time progressed.
        if (monitor.isJobFailed())
        {
            logger.error("Request to archive artefact on the DMF using command {} failed, reason: {}", job
                    .getDescription(), StringUtils.isBlank(monitor.getJobOutput()) ? "<NO OUTPUT FROM PROCESS>"
                    : monitor.getJobOutput());
            transitionTo(DepositState.Type.FAILED);
        }

    }

}
