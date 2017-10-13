package au.csiro.casda.deposit.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.csiro.casda.datadeposit.ChildDepositableArtefact;
import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.entity.observation.FitsObject;
import au.csiro.casda.jobmanager.JobManager;
import au.csiro.casda.jobmanager.JobManager.Job;
import au.csiro.casda.jobmanager.ProcessJobBuilder;

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
 * Extension of DepositState that takes the images/cubes and adds them to the coverage maps.
 * <p>
 * Copyright 2017, CSIRO Australia. All rights reserved.
 */
public class CasdaMappingDepositState extends DepositState
{
    private static final Logger logger = LoggerFactory.getLogger(CasdaMappingDepositState.class);

    private static final String MAPPING_JOB_PREFIX = "mapping";

    private ProcessJobBuilder processJobBuilder;

    private JobManager jobManager;

    /**
     * Constructor
     * 
     * @param stateFactory
     *            see {@link DepositState}
     * @param depositableArtefact
     *            the DepositableArtefact that the state pertains to
     * @param processJobBuilder
     *            a ProcessJobBuilder that can be used to create a Job to perform the stage
     * @param jobManager
     *            a JobManager that can be used to run a Job to perform the stage
     * @param depositObservationParentDirectory
     *            the root directory of all deposited observations
     * @param workingDirectory 
     *            the working directory in which to run the job. 
     */
    protected CasdaMappingDepositState(DepositStateFactory stateFactory, FitsObject depositableArtefact,
            ProcessJobBuilder processJobBuilder, JobManager jobManager,
            String depositObservationParentDirectory, String workingDirectory)
    {
        super(DepositState.Type.MAPPING, stateFactory, depositableArtefact);

        this.processJobBuilder = processJobBuilder;

        this.processJobBuilder.setProcessParameter("imageFile",
                getInfile(depositableArtefact, depositObservationParentDirectory));
        this.processJobBuilder.setProcessParameter("projectCode", depositableArtefact.getProject().getOpalCode());
        this.processJobBuilder.setWorkingDirectory(workingDirectory);

        this.jobManager = jobManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void progress()
    {
        JobManager.JobStatus jobStatus = jobManager.getJobStatus(getJobId(MAPPING_JOB_PREFIX));
        if (jobStatus == null)
        {
            Job job = processJobBuilder.createJob(getJobId(MAPPING_JOB_PREFIX), MAPPING_JOB_PREFIX);
            jobManager.startJob(job);
        }
        else if (jobStatus.isFailed())
        {
            logger.error("Job {} failed while updating coverage plots with output :{}", getJobId(MAPPING_JOB_PREFIX),
                    jobStatus.getJobOutput());
            transitionTo(DepositState.Type.FAILED);
        }
        else if (jobStatus.isFinished())
        {
            transitionTo(DepositState.Type.MAPPED);
        } // else still running
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChildDepositableArtefact getDepositable()
    {
        return (ChildDepositableArtefact) super.getDepositable();
    }

    private String getInfile(ChildDepositableArtefact depositableArtefact, String parentDir)
    {
        // use the unix file separator
        return parentDir + "/" + depositableArtefact.getParent().getUniqueId() + "/"
                + depositableArtefact.getFilename();
    }

    @Override
    public boolean isCheckpointState()
    {
        return true;
    }
}
