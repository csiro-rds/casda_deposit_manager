package au.csiro.casda.datadeposit;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Implementation of DepositState for the DepositState.Type.UNVALIDATED pre-initial state. This will copy all files in
 * the level 7 collection to the staging area.
 * <p>
 * Copyright 2017, CSIRO Australia All rights reserved.
 */
public class Level7CollectionUnvalidatedDepositState extends DepositState
{
    private static final Logger logger = LoggerFactory.getLogger(Level7CollectionUnvalidatedDepositState.class);

    /** The command name of the data copier tool */
    public static final String DATA_COPY_TOOL_NAME = "data_copy";
    
    private String level7CollectionsDirectory;

    private CasdaToolProcessJobBuilder processBuilder;

    private JobManager jobManager;
    
    /**
     * Constructor @see DepositState
     * 
     * @param stateFactory
     *            see @see DepositState
     * @param depositable
     *            see @see DepositState
     * @param level7CollectionsDirectory
     *            The path to the directory where the level 7 image data will be staged.
     * @param processBuilder
     *            a CasdaToolProcessJobBuilder that can be used to create a job to run the data copy
     * @param jobManager
     *            a JobManager that can be used to manage the running of the data copy
     */
    public Level7CollectionUnvalidatedDepositState(DepositStateFactory stateFactory, Depositable depositable,
            String level7CollectionsDirectory, CasdaToolProcessJobBuilder processBuilder, JobManager jobManager)
    {
        super(DepositState.Type.UNVALIDATED, stateFactory, depositable);
        this.level7CollectionsDirectory = level7CollectionsDirectory;
        this.processBuilder = processBuilder;
        this.jobManager = jobManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void progress()
    {
        Level7Collection collection = getDepositable();
        
        logger.info("Transfering " + collection);

        // Copy files
        // If not already running, kick off copies for files
        JobManager.JobStatus jobStatus = jobManager.getJobStatus(getJobId(DATA_COPY_TOOL_NAME));
        if (jobStatus == null)
        {
            // Create test folders
            Path collectionPath = 
            		Paths.get(level7CollectionsDirectory, String.valueOf(collection.getDapCollectionId()));
            
            if(!createStagingFolders(collection))
            {
            	//if clean/create of directory is not successful
            	logger.error("Creation of staging folders has failed for level 7 collection with ID: {}", 
            			collection.getDapCollectionId());
                transitionTo(DepositState.Type.FAILED);
            }

            CasdaToolProcessJobBuilder jobBuilder = processBuilder.setCommand(DATA_COPY_TOOL_NAME)
                    .addCommandArgument("-parent-id", String.valueOf(collection.getDapCollectionId()))
                    .addCommandArgument("-folder", String.valueOf(collectionPath));
            jobManager.startJob(jobBuilder.createJob
                    (getJobId(DATA_COPY_TOOL_NAME), DATA_COPY_TOOL_NAME));
        }
        else if (jobStatus.isFailed())
        {
            logger.error("Job {} failed while processing data copy with output :{}", 
                    getJobId(DATA_COPY_TOOL_NAME), jobStatus.getJobOutput());
            transitionTo(DepositState.Type.FAILED);
        }
        else if (jobStatus.isFinished())
        {
            transitionTo(DepositState.Type.VALIDATING);
        } 
    }

    private boolean createStagingFolders(Level7Collection collection)
    {
        Path collectionPath = Paths.get(level7CollectionsDirectory, String.valueOf(collection.getDapCollectionId()));
        File dcFolder = collectionPath.toFile();
        if(!dcFolder.exists())
        {
        	//directory will be created on its first deposit attempt
            dcFolder.mkdir();
        }
        else
        {
        	//clean up all files & sub-directories version from previous (failed) deposit attempt
        	try 
        	{
				FileUtils.cleanDirectory(dcFolder);
			} 
        	catch (IOException e) 
        	{
				return false;
			}
        }
        
        if(dcFolder.list().length > 0)
        {
        	return false;
        }
        if (collection.getImageCubePath() != null)
        {
            new File(dcFolder, "image_cube").mkdir();
        }
        if (collection.getSpectrumPath() != null)
        {
            new File(dcFolder, "spectrum").mkdir();
        }
        if (collection.getMomentMapPath() != null)
        {
            new File(dcFolder, "moment_map").mkdir();
        }
        if (collection.getCubeletPath() != null)
        {
            new File(dcFolder, "cubelet").mkdir();
        }
        return true;
    }
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCheckpointState()
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Level7Collection getDepositable()
    {
        return (Level7Collection) super.getDepositable();
    }
}
