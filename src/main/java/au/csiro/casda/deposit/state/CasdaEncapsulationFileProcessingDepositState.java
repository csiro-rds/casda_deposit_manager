package au.csiro.casda.deposit.state;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.datadeposit.ProcessingDepositState;
import au.csiro.casda.entity.observation.EncapsulationFile;
import au.csiro.casda.entity.observation.Level7Collection;
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
 * Extension of ProcessingDepositState specific to processing an EncapsulationFile
 * <p>
 * Copyright 2016, CSIRO Australia. All rights reserved.
 */
public class CasdaEncapsulationFileProcessingDepositState extends ProcessingDepositState
{
    private static final Logger logger = LoggerFactory.getLogger(CasdaEncapsulationFileProcessingDepositState.class);
            
    private static final String ENCAPSULATION_IMPORTER_TOOL_NAME = "encapsulate";

    /*
     * NB: All our paths are unix paths. Java is smart enough to convert that to a windows path when you use File, Path,
     * or Paths. (Also note that once a path has been turned into a Path then it is platform-specific.)
     */
    private static final String UNIX_FILE_SEPARATOR = "/";

    private String depositObservationParentDirectory;

    private CasdaToolProcessJobBuilder processBuilder;

    private JobManager jobManager;

    private String level7CollectionParentDirectory;

    /**
     * Constructor
     * 
     * @param stateFactory
     *            see {@link DepositState}
     * @param encapsulationFile
     *            the encapsulationFile that the state pertains to
     * @param depositObservationParentDirectory
     *            the parent directory of the deposit observation folders
     * @param level7CollectionParentDirectory
     *            the parent directory of the level 7 collection folders
     * @param processBuilder
     *            a CasdaToolProcessJobBuilder that can be used to create a job to run the fits object metadata import
     * @param jobManager
     *            a JobManager that can be used to manage the running of the imageCube metadata import
     */
    public CasdaEncapsulationFileProcessingDepositState(DepositStateFactory stateFactory,
            EncapsulationFile encapsulationFile, String depositObservationParentDirectory,
            String level7CollectionParentDirectory, CasdaToolProcessJobBuilder processBuilder, JobManager jobManager)
    {
        super(stateFactory, encapsulationFile);
        this.depositObservationParentDirectory = depositObservationParentDirectory;
        this.level7CollectionParentDirectory = level7CollectionParentDirectory;
        this.jobManager = jobManager;
        this.processBuilder = processBuilder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void progress()
    {
        JobManager.JobStatus jobStatus = jobManager.getJobStatus(getJobId(ENCAPSULATION_IMPORTER_TOOL_NAME));

        if (jobStatus == null)
        {
            CasdaToolProcessJobBuilder jobBuilder = this.processBuilder.setCommand(ENCAPSULATION_IMPORTER_TOOL_NAME)
                    .addCommandArgument("-encapsFilename", getDepositable().getFilename())
                    .addCommandArgument("-infile", getInfilePath());
            if(StringUtils.isNotBlank(getDepositable().getFilePattern()))
            {
            	jobBuilder.addCommandArgument("-pattern", getDepositable().getFilePattern());
            }
            
            if (getDepositable().getParent() instanceof Level7Collection)
            {
                jobBuilder.addCommandArgument("-parent-type", "derived-catalogue");
            }
            else
            {
                jobBuilder.addCommandArgument("-parent-type", "observation");
            }
            jobBuilder.addCommandArgument("-parent-id", getParentId());
            if(getDepositable().getEvaluationFiles().size() > 0)
            {
                jobBuilder.addCommandSwitch("-eval");
            }
            
            jobManager.startJob(
                    jobBuilder.createJob(getJobId(ENCAPSULATION_IMPORTER_TOOL_NAME), ENCAPSULATION_IMPORTER_TOOL_NAME));
        }
        else if (jobStatus.isFailed())
        {
            logger.error("Job {} failed while processing encapsulation file deposit state with output :{}", 
            		getJobId(ENCAPSULATION_IMPORTER_TOOL_NAME), jobStatus.getJobOutput());
            transitionTo(DepositState.Type.FAILED);
        }
        else if (jobStatus.isFinished())
        {
            // Do nothing as the encapsulate job will have advanced the encapsulation file to the next state
            return;
        } // else still running
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EncapsulationFile getDepositable()
    {
        return (EncapsulationFile) super.getDepositable();
    }

    private String getParentId()
    {
        return getDepositable().getParent().getUniqueId();
    }

    private String getInfilePath()
    {
        String parentFolder = depositObservationParentDirectory;
        if (getDepositable().getParent() instanceof Level7Collection)
        {
            parentFolder = level7CollectionParentDirectory;
        }
        return parentFolder + UNIX_FILE_SEPARATOR + getParentId() + UNIX_FILE_SEPARATOR
                + getDepositable().getFilename();
    }
}
