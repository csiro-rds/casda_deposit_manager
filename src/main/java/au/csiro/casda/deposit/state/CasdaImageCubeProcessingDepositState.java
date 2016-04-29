package au.csiro.casda.deposit.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.datadeposit.ProcessingDepositState;
import au.csiro.casda.entity.observation.ImageCube;
import au.csiro.casda.jobmanager.CasdaToolProcessJobBuilder;
import au.csiro.casda.jobmanager.JobManager;

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
 * Extension of ProcessingDepositState specific to processing an ImageCube object, namely: importing the FITS metadata
 * from the image file.
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
public class CasdaImageCubeProcessingDepositState extends ProcessingDepositState
{
    private static final Logger logger = LoggerFactory.getLogger(CasdaImageCubeProcessingDepositState.class);
            
    private static final String FITS_IMPORTER_TOOL_NAME = "fits_import";

    /*
     * NB: All our paths are unix paths. Java is smart enough to convert that to a windows path when you use File, Path,
     * or Paths. (Also note that once a path has been turned into a Path then it is platform-specific.)
     */
    private static final String UNIX_FILE_SEPARATOR = "/";

    private String depositObservationParentDirectory;

    private CasdaToolProcessJobBuilder processBuilder;

    private JobManager jobManager;

    /**
     * Constructor
     * 
     * @param stateFactory
     *            see {@link DepositState}
     * @param imageCube
     *            the ImageCube that the state pertains to
     * @param depositObservationParentDirectory
     *            the parent directory of the deposit observation folders
     * @param processBuilder
     *            a CasdaToolProcessJobBuilder that can be used to create a job to run the imageCube metadata import
     * @param jobManager
     *            a JobManager that can be used to manage the running of the imageCube metadata import
     */
    public CasdaImageCubeProcessingDepositState(DepositStateFactory stateFactory, ImageCube imageCube,
            String depositObservationParentDirectory, CasdaToolProcessJobBuilder processBuilder, JobManager jobManager)
    {
        super(stateFactory, imageCube);
        this.depositObservationParentDirectory = depositObservationParentDirectory;
        this.jobManager = jobManager;
        this.processBuilder = processBuilder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void progress()
    {
        JobManager.JobStatus jobStatus = jobManager.getJobStatus(getJobId());
        if (jobStatus == null)
        {
            jobManager.startJob(this.processBuilder.setCommand(FITS_IMPORTER_TOOL_NAME)
                    .addCommandArgument("-infile", getInfilePath())
                    .addCommandArgument("-imageCubeFilename", getDepositable().getFilename())
                    .addCommandArgument("-sbid", getSbid())
                    .createJob(getJobId(), FITS_IMPORTER_TOOL_NAME));
        }
        else if (jobStatus.isFailed())
        {
            logger.error("Job {} failed while processing image deposit state with output :{}", getJobId(),
                    jobStatus.getJobOutput());
            transitionTo(DepositState.Type.FAILED);
        }
        else if (jobStatus.isFinished())
        {
            super.progress();
        } // else still running
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ImageCube getDepositable()
    {
        return (ImageCube) super.getDepositable();
    }

    private String getJobId()
    {
        return String.format("%s-%s-%d", FITS_IMPORTER_TOOL_NAME, getDepositable().getUniqueIdentifier(),
                getDepositable().getDepositFailureCount());
    }

    private String getSbid()
    {
        return getDepositable().getParent().getUniqueId();
    }

    private String getInfilePath()
    {
        return this.depositObservationParentDirectory + UNIX_FILE_SEPARATOR + getSbid() + UNIX_FILE_SEPARATOR
                + getDepositable().getFilename();
    }
}
