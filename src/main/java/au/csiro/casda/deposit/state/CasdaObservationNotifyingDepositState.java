package au.csiro.casda.deposit.state;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.entity.observation.Observation;
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
 * Extension of ObservationNotifyingDepositState that ensures that the RTC DONE file is written.
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
public class CasdaObservationNotifyingDepositState extends ObservationNotifyingDepositState
{
    private static final Logger logger = LoggerFactory.getLogger(CasdaObservationNotifyingDepositState.class);
            
    private static final String RTC_NOTIFIER_TOOL_NAME = "rtc_notify";

    private Path depositObservationParentDirectory;

    private CasdaToolProcessJobBuilder processBuilder;

    private JobManager jobManager;

    /**
     * Constructor
     * 
     * @param stateFactory
     *            see {@link DepositState}
     * @param observation
     *            the Observation that the state pertains to
     * @param depositObservationParentDirectory
     *            the parent directory of the deposit observation folders
     * @param processBuilder
     *            a CasdaToolProcessJobBuilder that can be used to create a job to run the rtc notification
     * @param jobManager
     *            a JobManager that can be used to manage the running of the rtc notification
     */
    public CasdaObservationNotifyingDepositState(DepositStateFactory stateFactory, Observation observation,
            String depositObservationParentDirectory, CasdaToolProcessJobBuilder processBuilder, JobManager jobManager)
    {
        super(stateFactory, observation);
        this.depositObservationParentDirectory = Paths.get(depositObservationParentDirectory);
        this.jobManager = jobManager;
        this.processBuilder = processBuilder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void progress()
    {
        JobManager.JobStatus jobStatus = jobManager.getJobStatus(getJobId(RTC_NOTIFIER_TOOL_NAME));
        if (jobStatus == null)
        {
            jobManager
                    .startJob(processBuilder
                            .setCommand(RTC_NOTIFIER_TOOL_NAME)
                            .addCommandArgument("-infile",
                                    this.depositObservationParentDirectory.resolve(getSbid()).toString())
                            .addCommandArgument("-sbid", getSbid()).createJob
                            (getJobId(RTC_NOTIFIER_TOOL_NAME), RTC_NOTIFIER_TOOL_NAME));
        }
        else if (jobStatus.isFinished())
        {
            if (jobStatus.isFailed())
            {
                logger.error("Job {} failed while notifying observation deposit state with output :{}", 
                		getJobId(RTC_NOTIFIER_TOOL_NAME), jobStatus.getJobOutput());
                transitionTo(DepositState.Type.FAILED);
            }
            else
            {
                super.progress();
            }
        } // else still running
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observation getDepositable()
    {
        return (Observation) super.getDepositable();
    }

    private String getSbid()
    {
        return getDepositable().getSbid().toString();
    }
}
