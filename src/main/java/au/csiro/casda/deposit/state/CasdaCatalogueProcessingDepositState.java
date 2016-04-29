package au.csiro.casda.deposit.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.datadeposit.ProcessingDepositState;
import au.csiro.casda.entity.observation.Catalogue;
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
 * Extension of ProcessingDepositState specific to processing a Catalogue object, namely: importing a catalogue
 * continuum datafile.
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
public class CasdaCatalogueProcessingDepositState extends ProcessingDepositState
{

    private static final Logger logger = LoggerFactory.getLogger(CasdaCatalogueProcessingDepositState.class);

    /** The command name of the catalogue importer tool */
    public static final String CATALOGUE_IMPORTER_TOOL_NAME = "catalogue_import";

    /*
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
     * @param catalogue
     *            the Catalogue that the state pertains to
     * @param parentDirectory
     *            the parent directory of the deposit observation or level 7 collection folders
     * @param processBuilder
     *            a CasdaToolProcessJobBuilder that can be used to create a job to run the catalogue datafile import
     * @param jobManager
     *            a JobManager that can be used to manage the running of the catalogue datafile import
     */
    public CasdaCatalogueProcessingDepositState(DepositStateFactory stateFactory, Catalogue catalogue,
            String parentDirectory, CasdaToolProcessJobBuilder processBuilder, JobManager jobManager)
    {
        super(stateFactory, catalogue);
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
        JobManager.JobStatus jobStatus = jobManager.getJobStatus(getJobId());
        if (jobStatus == null)
        {
            jobManager.startJob(processBuilder.setCommand(CATALOGUE_IMPORTER_TOOL_NAME)
                    .addCommandArgument("-catalogue-type", getDepositable().getCatalogueType().getName())
                    .addCommandArgument("-parent-id", getParentId())
                    .addCommandArgument("-catalogue-filename", getDepositable().getFilename())
                    .addCommandArgument("-infile", getInfilePath().toString())
                    .createJob(getJobId(), CATALOGUE_IMPORTER_TOOL_NAME));
        }
        else if (jobStatus.isFailed())
        {
            logger.error("Job {} failed while processing catalogue deposit state with output :{}", getJobId(),
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
    public Catalogue getDepositable()
    {
        return (Catalogue) super.getDepositable();
    }

    private String getJobId()
    {
        return String.format("%s-%s-%d", CATALOGUE_IMPORTER_TOOL_NAME, getDepositable().getUniqueIdentifier(),
                getDepositable().getDepositFailureCount());
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
