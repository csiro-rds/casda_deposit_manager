package au.csiro.casda.deposit.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.datadeposit.ProcessingDepositState;
import au.csiro.casda.entity.observation.Cubelet;
import au.csiro.casda.entity.observation.FitsObject;
import au.csiro.casda.entity.observation.ImageCube;
import au.csiro.casda.entity.observation.ImageDerivedProduct;
import au.csiro.casda.entity.observation.Level7Collection;
import au.csiro.casda.entity.observation.MomentMap;
import au.csiro.casda.entity.observation.Spectrum;
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
 * Extension of ProcessingDepositState specific to processing an ImageCube or Spectrum object, namely: importing the 
 * FITS metadata from the image file.
 * <p>
 * Copyright 2016, CSIRO Australia. All rights reserved.
 */
public class CasdaFitsObjectProcessingDepositState extends ProcessingDepositState
{
    private static final Logger logger = LoggerFactory.getLogger(CasdaFitsObjectProcessingDepositState.class);
    
    private static final String FITS_IMPORTER_TOOL_NAME = "fits_import";

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
     * @param fitsObject
     *            the ImageCube or Spectrum that the state pertains to
     * @param depositObservationParentDirectory
     *            the parent directory of the deposit observation folders
     * @param level7CollectionParentDirectory
     *            the parent directory of the level 7 collection folders
     * @param processBuilder
     *            a CasdaToolProcessJobBuilder that can be used to create a job to run the fits object metadata import
     * @param jobManager
     *            a JobManager that can be used to manage the running of the imageCube metadata import
     */
    public CasdaFitsObjectProcessingDepositState(DepositStateFactory stateFactory, FitsObject fitsObject,
            String depositObservationParentDirectory, String level7CollectionParentDirectory,
            CasdaToolProcessJobBuilder processBuilder, JobManager jobManager)
    {
        super(stateFactory, fitsObject);
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
        JobManager.JobStatus jobStatus = jobManager.getJobStatus(getJobId(FITS_IMPORTER_TOOL_NAME));
        if (jobStatus == null)
        {
            if (getDepositable() instanceof ImageCube)
            {
                startImageCubeImportJob();
            }
            else
            {
                startImageDerivedProductImportJob();
            }
        }
        else if (jobStatus.isFailed())
        {
            logger.error("Job {} failed while processing image deposit state with output :{}", 
            		getJobId(FITS_IMPORTER_TOOL_NAME), jobStatus.getJobOutput());
            transitionTo(DepositState.Type.FAILED);
        }
        else if (jobStatus.isFinished())
        {
            // Do nothing as the fits_import job will have advanced the FITS object to the appropriate next state
            return;
        } // else still running
    }

    private void startImageCubeImportJob()
    {
        CasdaToolProcessJobBuilder jobBuilder = this.processBuilder.setCommand(FITS_IMPORTER_TOOL_NAME)
                .addCommandArgument("-infile", getInfilePath())
                .addCommandArgument("-fitsFilename", getDepositable().getFilename())
                .addCommandArgument("-fits-type", "image-cube");
        if (getDepositable().getParent() instanceof Level7Collection)
        {
            jobBuilder.addCommandArgument("-parent-type", "derived-catalogue");
        }
        else
        {
            jobBuilder.addCommandArgument("-parent-type", "observation");
        }
        jobBuilder.addCommandArgument("-parent-id", getParentId());
        jobManager.startJob(jobBuilder
                .createJob(getJobId(FITS_IMPORTER_TOOL_NAME), FITS_IMPORTER_TOOL_NAME));
    }

    private void startImageDerivedProductImportJob()
    {
        String fitsType = null;
        if (getDepositable() instanceof Spectrum)
        {
            fitsType = "spectrum";
        }
        else if (getDepositable() instanceof MomentMap)
        {
            fitsType = "moment-map";
        }
        else if (getDepositable() instanceof Cubelet)
        {
            fitsType = "cubelet";
        }
        else
        {
            throw new IllegalStateException("Unexpected depositable type: " + getDepositable().getClass().getName());
        }

        String inFile;
        String imageCubeFilename;
        if (((ImageDerivedProduct) getDepositable()).getImageCube() != null)
        {
            imageCubeFilename = ((ImageDerivedProduct) getDepositable()).getImageCube().getFilename();
            inFile = getParentBasePath();
        }
        else
        {
            imageCubeFilename = getDepositable().getFilename();
            inFile = getInfilePath();
        }

        CasdaToolProcessJobBuilder jobBuilder = this.processBuilder.setCommand(FITS_IMPORTER_TOOL_NAME)
                .addCommandArgument("-infile", inFile)
                .addCommandArgument("-fitsFilename", imageCubeFilename)
                .addCommandArgument("-fits-type", fitsType);
        if (getDepositable().getParent() instanceof Level7Collection)
        {
            jobBuilder.addCommandArgument("-parent-type", "derived-catalogue");
        }
        else
        {
            jobBuilder.addCommandArgument("-parent-type", "observation");
        }
        jobBuilder.addCommandArgument("-parent-id", getParentId());
        jobManager.startJob(jobBuilder
                .createJob(getJobId(FITS_IMPORTER_TOOL_NAME), FITS_IMPORTER_TOOL_NAME));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FitsObject getDepositable()
    {
        return (FitsObject) super.getDepositable();
    }

    private String getParentId()
    {
        return getDepositable().getParent().getUniqueId();
    }

    private String getInfilePath()
    {
        return getParentBasePath() + UNIX_FILE_SEPARATOR + getDepositable().getFilename();
    }

    private String getParentBasePath()
    {
        String parentFolder = depositObservationParentDirectory;
        if (getDepositable().getParent() instanceof Level7Collection)
        {
            parentFolder = level7CollectionParentDirectory;
        }
        return parentFolder + UNIX_FILE_SEPARATOR + getParentId();
    }
    
    /**
     * gets the job id for use by the job manager based on the command line tool name
     * @param commandLineToolName the command line tool being used
     * @return the unique job id
     */
    protected String getJobId(String commandLineToolName)
    {
        String fitsType = null;
        if (getDepositable() instanceof ImageCube || (getDepositable() instanceof ImageDerivedProduct
                && ((ImageDerivedProduct) getDepositable()).getImageCube() == null))
        {
            return super.getJobId(commandLineToolName);
        }        
        
        String parentIdentifier = ""; 
        if (getDepositable() instanceof Spectrum)
        {
            fitsType = "spectrum";
            parentIdentifier = ((ImageDerivedProduct) getDepositable()).getImageCube().getUniqueIdentifier();
        }
        else if (getDepositable() instanceof MomentMap)
        {
            fitsType = "moment-map";
            parentIdentifier = ((ImageDerivedProduct) getDepositable()).getImageCube().getUniqueIdentifier();
        }
        else if (getDepositable() instanceof Cubelet)
        {
            fitsType = "cubelet";
            parentIdentifier = ((ImageDerivedProduct) getDepositable()).getImageCube().getUniqueIdentifier();
        }

        return String.format("%s-%s-%s-%d", commandLineToolName, fitsType, parentIdentifier,
                getDepositable().getDepositFailureCount());
    }
}
