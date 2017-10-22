package au.csiro.casda.deposit.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.datadeposit.ProcessingDepositState;
import au.csiro.casda.entity.observation.EvaluationFile;
import au.csiro.casda.jobmanager.CasdaToolProcessJobBuilder;
import au.csiro.casda.jobmanager.JobManager;

/*
 * #%L
 * CSIRO ASKAP Science Data Archive
 * %%
 * Copyright (C) 2017 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

/**
 * Extension of ProcessingDepositState specific to process a validation metric object, namely: importing a 
 * validation metric
 * <p>
 * Copyright 2017, CSIRO Australia. All rights reserved.
 */
public class CasdaValidationMetricProcessingDepositState extends ProcessingDepositState
{

    private static final Logger logger = LoggerFactory.getLogger(CasdaValidationMetricProcessingDepositState.class);
    
    /** The command name of the validation metric importer tool */
    public static final String VALIDATION_METRIC_IMPORTER_TOOL_NAME = "validation_metric_import";    /*
     * NB: All our paths are unix paths. Java is smart enough to convert that to a windows path when you use File, Path,
     * or Paths. (Also note that once a path has been turned into a Path then it is platform-specific.)
     */
    private static final String UNIX_FILE_SEPARATOR = "/";

    private String parentDirectory;

    private CasdaToolProcessJobBuilder processBuilder;

    private JobManager jobManager;
    
    /**
     * Constructor
     * 
     * @param stateFactory
     *            see {@link DepositState}
     * @param evaluationFile
     *            the evaluationFile that the state pertains to
     * @param parentDirectory
     *            the parent directory of the deposit observation or level 7 collection folders
     * @param processBuilder
     *            a CasdaToolProcessJobBuilder that can be used to create a job to run the catalogue datafile import
     * @param jobManager
     *            a JobManager that can be used to manage the running of the catalogue datafile import
     */
    public CasdaValidationMetricProcessingDepositState(DepositStateFactory stateFactory, EvaluationFile evaluationFile,
            String parentDirectory, CasdaToolProcessJobBuilder processBuilder, JobManager jobManager)
    {
        super(stateFactory, evaluationFile);
        this.parentDirectory = parentDirectory;
        this.jobManager = jobManager;
        this.processBuilder = processBuilder;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void progress()
    {
        JobManager.JobStatus jobStatus = jobManager.getJobStatus(getJobId(VALIDATION_METRIC_IMPORTER_TOOL_NAME));
        if (jobStatus == null)
        {
        	EvaluationFile evaluationFile = getDepositable();
            CasdaToolProcessJobBuilder jobBuilder = processBuilder.setCommand(VALIDATION_METRIC_IMPORTER_TOOL_NAME)
                    .addCommandArgument("-parent-id", getParentId())
                    .addCommandArgument("-filename", evaluationFile.getFilename())
                    .addCommandArgument("-infile", getInfilePath().toString());
            jobManager.startJob(jobBuilder.createJob
            		(getJobId(VALIDATION_METRIC_IMPORTER_TOOL_NAME), VALIDATION_METRIC_IMPORTER_TOOL_NAME));
        }
        else if (jobStatus.isFailed())
        {
            logger.error("Job {} failed while processing validation metric deposit state with output :{}", 
            		getJobId(VALIDATION_METRIC_IMPORTER_TOOL_NAME), jobStatus.getJobOutput());
            transitionTo(DepositState.Type.FAILED);
        }
        else if (jobStatus.isFinished())
        {
            // Do nothing as the validation_metric_import job will have advanced the validation metric to the next state
            return;
        } // else still running
    }

    
    /**
     * {@inheritDoc}
     */
    @Override
    public EvaluationFile getDepositable()
    {
        return (EvaluationFile) super.getDepositable();
    }

    private String getParentId()
    {
        return getDepositable().getParent().getUniqueId();
    }

    private String getInfilePath()
    {
        return this.parentDirectory + UNIX_FILE_SEPARATOR + getParentId() + UNIX_FILE_SEPARATOR
                + getDepositable().getFilename();

    }
}
