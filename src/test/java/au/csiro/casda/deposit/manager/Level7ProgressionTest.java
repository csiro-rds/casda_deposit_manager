package au.csiro.casda.deposit.manager;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositState.Type;
import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.deposit.CasdaToolProcessJobBuilderFactory;
import au.csiro.casda.deposit.SingleJobMonitorFactory;
import au.csiro.casda.deposit.jdbc.SimpleJdbcRepository;
import au.csiro.casda.deposit.services.NgasService;
import au.csiro.casda.deposit.services.NgasService.Status;
import au.csiro.casda.deposit.services.VoToolsService;
import au.csiro.casda.deposit.state.CasdaDepositStateFactory;
import au.csiro.casda.entity.observation.Catalogue;
import au.csiro.casda.entity.observation.CatalogueType;
import au.csiro.casda.entity.observation.Level7Collection;
import au.csiro.casda.entity.observation.Project;
import au.csiro.casda.jobmanager.CasdaToolProcessJobBuilder;
import au.csiro.casda.jobmanager.JavaProcessJobFactory;
import au.csiro.casda.jobmanager.JobManager;
import au.csiro.casda.jobmanager.JobManager.JobStatus;
import au.csiro.casda.jobmanager.SingleJobMonitor;
/*
 * #%L
 * CSIRO ASKAP Science Data Archive
 * %%
 * Copyright (C) 2016 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */
/**
 * Tests the full deposit progression for a level 7 collection and its associated catalogues.
 * <p>
 * Copyright 2016, CSIRO Australia. All rights reserved.
 */
public class Level7ProgressionTest 
{
    private DepositStateFactory depositStateFactory;

    @Mock
    private JobManager jobManager;

    @Mock
    private NgasService ngasService;

    @Mock
    private CasdaToolProcessJobBuilderFactory factory;

    @Mock
    private CasdaToolProcessJobBuilder processBuilder;

    @Mock
    private VoToolsService voToolsService;
    
    @Mock
    private SingleJobMonitorFactory singleJobMonitorFactory;

    @Mock
    private SingleJobMonitor monitor;

    @Mock
    private SimpleJdbcRepository simpleJdbcRepository;

    @Before
    public void setup() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        this.depositStateFactory = new CasdaDepositStateFactory(ngasService, jobManager, factory,
                new JavaProcessJobFactory(), singleJobMonitorFactory, voToolsService, simpleJdbcRepository, "",
                "observation", "level7", "{\"stageCommand\"}", "SIMPLE", "stageCommandAndArgs", "{\"registerCommand\"}",
                "SIMPLE", "registerCommandAndArgs", "{\"archiveStatus\"}", "{\"archivePut\"}",
                " {\"stage_artefact\", \"1\", \"register_artefact\", \"4\" }", "");
        when(factory.createBuilder()).thenReturn(processBuilder);
        when(processBuilder.setCommand(any(String.class))).thenReturn(processBuilder);
        when(processBuilder.addCommandArgument(any(String.class), any(String.class))).thenReturn(processBuilder);
        when(processBuilder.addCommandArguments(any(String[].class))).thenReturn(processBuilder);
        when(processBuilder.addCommandSwitch(any(String.class))).thenReturn(processBuilder);
        
        JobStatus success = mock(JobStatus.class);
        when(success.isFinished()).thenReturn(true);
        when(success.isFailed()).thenReturn(false);
        when(jobManager.getJobStatus(any())).thenReturn(success);
        
		Status ngasStatus = mock(Status.class);
		when(ngasStatus.wasFailure()).thenReturn(false);
		when(ngasService.getStatus(any(String.class))).thenReturn(ngasStatus);
		
        when(singleJobMonitorFactory.createSingleJobMonitor()).thenReturn(monitor);
        when(monitor.isJobFailed()).thenReturn(false);
        when(monitor.isJobFinished()).thenReturn(true);
        when(monitor.getJobOutput()).thenReturn("DUL");
    }
    
    @Test
    public void testLevel7Progression()
    {
    	Level7Collection collection = createLevel7CollectionInState(DepositState.Type.UNDEPOSITED);
    	Catalogue catalogue = collection.getCatalogues().get(0);
    	
    	//moves L7 collection through to depositing
    	collection.progressDeposit();	
    	assertEquals("Incorrect deposit state for collection,", collection.getDepositState().getType(), Type.DEPOSITING);
    	assertEquals("Incorrect deposit state for catalogue,", catalogue.getDepositState().getType(), Type.UNDEPOSITED);
    	
    	//moves catalogue through to processing & collection stays at depositing
    	collection.progressDeposit();  
    	assertEquals("Incorrect deposit state for collection,", collection.getDepositState().getType(), Type.DEPOSITING);
    	assertEquals("Incorrect deposit state for catalogue,", catalogue.getDepositState().getType(), Type.PROCESSING);
    	
    	//moves catalogue through to processed & collection stays at depositing
    	collection.progressDeposit();
    	assertEquals("Incorrect deposit state for collection,", collection.getDepositState().getType(), Type.DEPOSITING);
    	assertEquals("Incorrect deposit state for catalogue,", catalogue.getDepositState().getType(), Type.PROCESSING);
    	// The deposit state advancement would be done by the batch job, so do it manually here 
    	catalogue.setDepositState(depositStateFactory.createState(Type.PROCESSED, catalogue));
    	
    	//moves catalogue through to staging & collection stays at depositing
    	collection.progressDeposit();
    	assertEquals("Incorrect deposit state for collection,", collection.getDepositState().getType(), Type.DEPOSITING);
    	assertEquals("Incorrect deposit state for catalogue,", catalogue.getDepositState().getType(), Type.STAGING);
    	
    	//moves catalogue through to staged & collection stays at depositing
    	collection.progressDeposit();
    	assertEquals("Incorrect deposit state for collection,", collection.getDepositState().getType(), Type.DEPOSITING);
    	assertEquals("Incorrect deposit state for catalogue,", catalogue.getDepositState().getType(), Type.STAGED);
    	
    	//moves catalogue through to registering & collection stays at depositing
    	collection.progressDeposit();
    	assertEquals("Incorrect deposit state for collection,", collection.getDepositState().getType(), Type.DEPOSITING);
    	assertEquals("Incorrect deposit state for catalogue,", catalogue.getDepositState().getType(), Type.REGISTERING);
    	
    	//moves catalogue through to registered & collection stays at depositing
    	collection.progressDeposit();
    	assertEquals("Incorrect deposit state for collection,", collection.getDepositState().getType(), Type.DEPOSITING);
    	assertEquals("Incorrect deposit state for catalogue,", catalogue.getDepositState().getType(), Type.REGISTERED);
    	
    	//moves catalogue through to archiving & collection stays at depositing
    	collection.progressDeposit();
    	assertEquals("Incorrect deposit state for collection,", collection.getDepositState().getType(), Type.DEPOSITING);
    	assertEquals("Incorrect deposit state for catalogue,", catalogue.getDepositState().getType(), Type.ARCHIVING);
    	
    	//moves catalogue through to archived & collection stays at depositing
    	collection.progressDeposit();
    	assertEquals("Incorrect deposit state for collection,", collection.getDepositState().getType(), Type.ARCHIVING);
    	assertEquals("Incorrect deposit state for catalogue,", catalogue.getDepositState().getType(), Type.ARCHIVED);
    	
    	//moves catalogue through to deposited & collection stays at depositing
    	collection.progressDeposit();
    	assertEquals("Incorrect deposit state for collection,", collection.getDepositState().getType(), Type.ARCHIVING);
    	assertEquals("Incorrect deposit state for catalogue,", catalogue.getDepositState().getType(), Type.DEPOSITED);
    	
    	//moves collection to cleanup, catalogue remains at deposited
    	collection.progressDeposit();
    	assertEquals("Incorrect deposit state for collection,", collection.getDepositState().getType(), Type.CLEANUP);
    	assertEquals("Incorrect deposit state for catalogue,", catalogue.getDepositState().getType(), Type.DEPOSITED);
    	
    	//moves collection to cleanup, catalogue remains at deposited
    	collection.progressDeposit();
    	assertEquals("Incorrect deposit state for collection,", collection.getDepositState().getType(), Type.DEPOSITED);
    	assertEquals("Incorrect deposit state for catalogue,", catalogue.getDepositState().getType(), Type.DEPOSITED);
    }
    
    private Level7Collection createLevel7CollectionInState(Type type)
    {
    	Level7Collection collection = new Level7Collection();
    	collection.setDepositStateFactory(depositStateFactory);
    	collection.setDepositState(depositStateFactory.createState(type, collection));
    	
        Project project = new Project("C007");
        collection.setProject(project);
    	
        Catalogue catalogue1 = new Catalogue();
        catalogue1.setCatalogueType(CatalogueType.DERIVED_CATALOGUE);
        catalogue1.setProject(project);
        catalogue1.setFilename("catalogue1");
        catalogue1.setDepositState(depositStateFactory.createState(Type.UNDEPOSITED, catalogue1));
        catalogue1.setFilesize(RandomUtils.nextLong(1, Long.MAX_VALUE));
        collection.addCatalogue(catalogue1);
    	
    	return collection;
    }
}
