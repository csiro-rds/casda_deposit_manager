package au.csiro.casda.deposit.state;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.csiro.casda.datadeposit.ChildDepositableArtefact;
import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.datadeposit.RegisteringDepositState;
import au.csiro.casda.entity.observation.Level7Collection;
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
 * Extension of CopyingDepositState that ensures that a DepositableArtefact's backing file has been copied into the
 * archive.
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
public class CasdaRegisteringDepositState extends RegisteringDepositState
{
    private static final Logger logger = LoggerFactory.getLogger(CasdaRegisteringDepositState.class);

    private static final String REGISTER_ARTEFACT_TOOL_NAME = "register_artefact";

    private ProcessJobBuilder processJobBuilder;

    private JobManager jobManager;

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
     * @param processJobBuilder
     *            a ProcessJobBuilder that can be used to create a Job to perform the stage
     * @param jobManager
     *            a JobManager that can be used to run a Job to perform the stage
     * @param depositObservationParentDirectory
     *            the root directory of all deposited observations
     * @param level7CollectionParentDirectory
     *            the root directory of all level 7 collections
     */
    protected CasdaRegisteringDepositState(DepositStateFactory stateFactory,
            ChildDepositableArtefact depositableArtefact, Map<String, String> ngasArtefactVolumeMap,
            ProcessJobBuilder processJobBuilder, JobManager jobManager, String depositObservationParentDirectory,
            String level7CollectionParentDirectory)
    {
        super(stateFactory, depositableArtefact);
        this.processJobBuilder = processJobBuilder;

        if (depositableArtefact.getParent() instanceof Level7Collection)
        {
            this.processJobBuilder.setProcessParameter("infile",
                    getInfile(depositableArtefact, level7CollectionParentDirectory));
        }
        else
        {
            this.processJobBuilder.setProcessParameter("infile",
                    getInfile(depositableArtefact, depositObservationParentDirectory));
        }

        this.processJobBuilder.setProcessParameter("parent_id", depositableArtefact.getParent().getUniqueId());
        this.processJobBuilder.setProcessParameter("artefact_id",
                depositableArtefact.getUniqueIdentifier().replace("/", "-"));
        this.processJobBuilder.setProcessParameter("staging_volume",
                ngasArtefactVolumeMap.get(depositableArtefact.getDepositableArtefactTypeName()));
        this.jobManager = jobManager;
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
            Job job = processJobBuilder.createJob(getJobId(), REGISTER_ARTEFACT_TOOL_NAME);
            jobManager.startJob(job);
        }
        else if (jobStatus.isFailed())
        {
            logger.error("Job {} failed while registering deposit state with output :{}", getJobId(),
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
    public ChildDepositableArtefact getDepositable()
    {
        return (ChildDepositableArtefact) super.getDepositable();
    }

    private String getJobId()
    {
        return String.format("%s-%s-%d", REGISTER_ARTEFACT_TOOL_NAME, getDepositable().getUniqueIdentifier(),
                getDepositable().getDepositFailureCount());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("PMD")
    public void cleanupAfterRecovery()
    {
        // clean up failed copy
        // Staging / Staged / Registering / Registered is a super-state such that when any of these fails they all need
        // to be done again but done again at all of these places. The clean up for Registering is now done in Staging
        // (CasdaStagingDepositingState)
        super.cleanupAfterRecovery();
    }

    private String getInfile(ChildDepositableArtefact depositableArtefact, String parentDir)
    {
        // use the unix file separator
        return parentDir + "/" + depositableArtefact.getParent().getUniqueId() + "/"
                + depositableArtefact.getFilename();
    }
}
