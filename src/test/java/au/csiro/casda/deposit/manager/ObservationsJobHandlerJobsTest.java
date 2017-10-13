package au.csiro.casda.deposit.manager;

import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.not;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import au.csiro.casda.deposit.DepositManagerEvents;
import au.csiro.casda.deposit.Log4JTestAppender;
import au.csiro.casda.deposit.exception.ImportException;
import au.csiro.casda.deposit.exception.PollingException;
import au.csiro.casda.deposit.jpa.ObservationRepository;
import au.csiro.casda.deposit.services.ObservationService;
import au.csiro.casda.entity.observation.Observation;
import au.csiro.casda.jobmanager.JavaProcessJobFactory;
import au.csiro.casda.jobmanager.JobManager;
import au.csiro.casda.jobmanager.JobManager.Job;
import au.csiro.casda.jobmanager.JobManager.JobStatus;
import au.csiro.casda.jobmanager.ProcessJob;
import au.csiro.casda.jobmanager.SynchronousProcessJobManager;

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
 * Test of DepositManager.findObservation() & runJob() methods.
 * 
 * Copyright 2014, CSIRO Australia. All rights reserved.
 * 
 */
public class ObservationsJobHandlerJobsTest
{
    private Log4JTestAppender testAppender;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Mock
    private ObservationRepository observationRepository;

    private ObservationService observationService;

    @Spy
    private SynchronousProcessJobManager jobManager;

    private ObservationsJobsHandler observationsJobsHandler;

    private String sbid1 = "1234";
    private String sbid2 = "7890";
    private Path testObsRootDir = null;
    private Path testObsDir1 = null;
    private Path testObsDir2 = null;
    private Path testObsReadyFile1 = null;
    private Path testObsReadyFile2 = null;
    private List<Path> filePaths = null;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        testAppender = Log4JTestAppender.createAppender();
        MockitoAnnotations.initMocks(this);
        observationService = new ObservationService();
        observationsJobsHandler = new ObservationsJobsHandler(new JavaProcessJobFactory(), ".", ".", null, jobManager,
                observationService, observationRepository);
        testObsRootDir = Paths.get(tempFolder.newFolder("DATA_DEPOSIT_TEST_ROOTDIR").getPath());
        testObsDir1 = Files.createDirectories(Paths.get(testObsRootDir.toString(), sbid1));
        testObsDir2 = Files.createDirectories(Paths.get(testObsRootDir.toString(), sbid2));
        testObsReadyFile1 = Files.createFile(Paths.get(testObsDir1.toString(), "READY"));
        testObsReadyFile2 = Files.createFile(Paths.get(testObsDir2.toString(), "READY"));
        filePaths = new ArrayList<>();
        filePaths.add(testObsReadyFile1);
        filePaths.add(testObsReadyFile2);
    }

    @Test
    public void testStartJobCalledAndLogsEvents() throws NumberFormatException, ImportException, PollingException
    {
        JobStatus mockSuccessStatus = mock(JobStatus.class);
        doReturn(true).when(mockSuccessStatus).isFinished();
        doReturn(false).when(mockSuccessStatus).isRunning();
        doReturn(false).when(mockSuccessStatus).isReady();
        doReturn(false).when(mockSuccessStatus).isFailed();

        doReturn(null).when(observationRepository).findBySbid(anyInt());
        doReturn(mockSuccessStatus).when(jobManager).getJobStatus(anyString());
        doNothing().when(jobManager).startJob(any(Job.class));

        observationsJobsHandler.run(testObsRootDir.toString());

        verify(jobManager, times(2)).startJob(any(JobManager.Job.class));

        testAppender.verifyLogMessage(Level.INFO, DepositManagerEvents.E035.messageBuilder().add(sbid1).toString());
        testAppender.verifyLogMessage(Level.INFO, DepositManagerEvents.E035.messageBuilder().add(sbid2).toString());
    }

    @Test
    public void testFailedImportJob() throws NumberFormatException, PollingException, ImportException
    {
        JobStatus mockFailedStatus = mock(JobStatus.class);
        doReturn(true).when(mockFailedStatus).isFinished();
        doReturn(false).when(mockFailedStatus).isRunning();
        doReturn(false).when(mockFailedStatus).isReady();
        doReturn(true).when(mockFailedStatus).isFailed();

        JobStatus mockCompletedStatus = mock(JobStatus.class);
        doReturn(true).when(mockCompletedStatus).isFinished();
        doReturn(false).when(mockCompletedStatus).isRunning();
        doReturn(false).when(mockCompletedStatus).isReady();
        doReturn(false).when(mockCompletedStatus).isFailed();
        
        when(jobManager.getJobStatus("observation_import-"+sbid1)).thenReturn(mockFailedStatus);
        when(jobManager.getJobStatus("observation_import-"+sbid2)).thenReturn(mockCompletedStatus);
        doNothing().when(jobManager).startJob(Mockito.any(Job.class));

        String dir = testObsRootDir.toString();

        observationsJobsHandler.run(dir);
        testAppender.verifyLogMessage(Level.ERROR,
                "Job observation_import-1234 failed during observation import with output");
        testAppender.verifyLogMessage(Level.ERROR, DepositManagerEvents.E073.messageBuilder().add(sbid1).toString(),
                ImportException.class, "sbid: " + sbid1);
        
        testAppender.verifyLogMessage(Level.INFO, DepositManagerEvents.E035.messageBuilder().add(sbid2).toString());
        
        assertThat(observationService.getInvalidObservationIds(), contains(sbid1));
    }

    @Test
    public void testPreviouslyFailedImportJob() throws Exception
    {
        Files.createFile(Paths.get(testObsDir1.toString(), "ERROR"));

        JobStatus mockCompletedStatus = mock(JobStatus.class);
        doReturn(true).when(mockCompletedStatus).isFinished();
        doReturn(false).when(mockCompletedStatus).isRunning();
        doReturn(false).when(mockCompletedStatus).isReady();
        doReturn(false).when(mockCompletedStatus).isFailed();
        
        when(jobManager.getJobStatus(Mockito.anyString())).thenReturn(mockCompletedStatus);
        doNothing().when(jobManager).startJob(Mockito.any(Job.class));

        String dir = testObsRootDir.toString();
        observationService.addInvalidObservation("Should be cleared");

        observationsJobsHandler.run(dir);
        testAppender.verifyLogMessage(Level.INFO, DepositManagerEvents.E035.messageBuilder().add(sbid2).toString());
        
        assertThat(observationService.getInvalidObservationIds(), contains(sbid1));
    }
    
    @Test
    public void testInvalidObservationName() throws Exception
    {
        Path testObsRootDir = Paths.get(tempFolder.newFolder("DATA_DEPOSIT_TEST_ROOTDIR2").getPath());
        Path testObsDir1 = Files.createDirectories(Paths.get(testObsRootDir.toString(), "text1234"));
        Path testObsReadyFile1 = Files.createFile(Paths.get(testObsDir1.toString(), "READY"));
        List<Path> filePaths = new ArrayList<>();
        filePaths.add(testObsReadyFile1);

        String dir = testObsRootDir.toString();

        observationsJobsHandler.run(dir);
        
        testAppender.verifyLogMessage(Level.ERROR, DepositManagerEvents.E073.messageBuilder().add("text1234").toString()
                , ImportException.class, "sbid: text1234");
        //checks that an error file has been created for the observation with text in its name
        assertTrue(Files.exists(Paths.get(testObsDir1.toString(), "ERROR")));
    }

    @Test
    public void testFailedRedepositJob() throws NumberFormatException, PollingException
    {
        JobStatus mockFailedStatus = mock(JobStatus.class);
        doReturn(true).when(mockFailedStatus).isFinished();
        doReturn(false).when(mockFailedStatus).isRunning();
        doReturn(false).when(mockFailedStatus).isReady();
        doReturn(true).when(mockFailedStatus).isFailed();
        
        when(jobManager.getJobStatus("observation_import-"+sbid1)).thenReturn(mockFailedStatus);
        doNothing().when(jobManager).startJob(Mockito.any(Job.class));

        int sbid = Integer.parseInt(sbid1);
        Observation observation = new Observation(sbid);
        doReturn(observation).when(observationRepository).findBySbid(anyInt());
        
        String dir = testObsRootDir.toString();

        try
        {
            observationsJobsHandler.redepositObservation(dir, sbid);
            fail("Job failure should have resulted in an exception");
        }
        catch (ImportException e)
        {
            assertThat(e.getMessage(), CoreMatchers.startsWith("Import failed - tool: observation_import, sbid: 1234"));
        }
        
        // Verify job contains redeposit flag
        ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
        verify(jobManager).startJob(jobCaptor.capture());
        ProcessJob job = (ProcessJob) jobCaptor.getValue();
        assertThat(Arrays.asList(job.getCommandAndArgs()), hasItem("-redeposit"));
        assertThat(Arrays.asList(job.getCommandAndArgs()), hasItem("-sbid"));
        assertThat(Arrays.asList(job.getCommandAndArgs()), hasItem(sbid1));
        
        
        testAppender.verifyLogMessage(Level.ERROR,
                "Job observation_import-1234 failed during observation import with output");
        
        assertThat(observationService.getInvalidObservationIds(), contains(sbid1));
    }

    @Test
    public void testCompletedRedepositJob() throws NumberFormatException, PollingException, ImportException
    {
        JobStatus mockCompletedStatus = mock(JobStatus.class);
        doReturn(true).when(mockCompletedStatus).isFinished();
        doReturn(false).when(mockCompletedStatus).isRunning();
        doReturn(false).when(mockCompletedStatus).isReady();
        doReturn(false).when(mockCompletedStatus).isFailed();
        
        when(jobManager.getJobStatus("observation_import-"+sbid2)).thenReturn(mockCompletedStatus);
        doNothing().when(jobManager).startJob(Mockito.any(Job.class));

        int sbid = Integer.parseInt(sbid2);
        Observation observation = new Observation(sbid);
        doReturn(observation).when(observationRepository).findBySbid(anyInt());
        
        String dir = testObsRootDir.toString();

        observationsJobsHandler.redepositObservation(dir, sbid);
        
        // Verify job contains redeposit flag
        ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
        verify(jobManager).startJob(jobCaptor.capture());
        ProcessJob job = (ProcessJob) jobCaptor.getValue();
        assertThat(Arrays.asList(job.getCommandAndArgs()), hasItem("-redeposit"));
        assertThat(Arrays.asList(job.getCommandAndArgs()), hasItem("-sbid"));
        assertThat(Arrays.asList(job.getCommandAndArgs()), hasItem(sbid2));
        
        testAppender.verifyLogMessage(Level.INFO, DepositManagerEvents.E035.messageBuilder().add(sbid2).toString());
        
        assertThat(observationService.getInvalidObservationIds(), not(contains(sbid2)));
    }
    

}