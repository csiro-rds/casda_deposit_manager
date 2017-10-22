package au.csiro.casda.deposit.state;

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


import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositState.Type;
import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.jobmanager.CasdaToolProcessJobBuilder;
import au.csiro.casda.jobmanager.JobManager;
import au.csiro.casda.jobmanager.JobManager.JobStatus;
import au.csiro.casda.jobmanager.ProcessJob;
import au.csiro.casda.entity.observation.EvaluationFile;
import au.csiro.casda.entity.observation.Observation;

/**
 * Verify the functions of the CasdaCatalogueProcessingDepositState class.
 * 
 * Copyright 2015, CSIRO Australia
 * All rights reserved.
 */
public class CasdaValidationmetricProcessingDepositStateTest
{

    @Mock
    private DepositStateFactory stateFactory;

    @Mock
    private CasdaToolProcessJobBuilder processBuilder;

    @Mock
    private JobManager jobManager;

    @Mock
    private DepositState initial;

    @Mock
    private DepositState next;

    @Mock
    private DepositState failed;

    private String depositObservationParentDirectory = "dir";

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        when(processBuilder.setCommand(anyString())).thenReturn(processBuilder);
        when(processBuilder.addCommandArgument(anyString(), anyString())).thenReturn(processBuilder);

        when(initial.getType()).thenReturn(Type.PROCESSING);

        when(next.getType()).thenReturn(Type.PROCESSED);

        when(failed.getType()).thenReturn(Type.FAILED);

    }

    @Test
    public void testProgressValidationMetricStartsNewJob()
    {
    	EvaluationFile catalogue = createValidationMetric();

        ProcessJob job = mock(ProcessJob.class);

        CasdaValidationMetricProcessingDepositState state =
                new CasdaValidationMetricProcessingDepositState(stateFactory, catalogue, depositObservationParentDirectory,
                        processBuilder, jobManager);

        String jobId = getEvaluationFileJobId(catalogue.getParent().getUniqueId());

        when(jobManager.getJobStatus(jobId)).thenReturn(null);

        when(processBuilder.createJob(eq(jobId), 
        		eq(CasdaValidationMetricProcessingDepositState.VALIDATION_METRIC_IMPORTER_TOOL_NAME))).thenReturn(job);

        state.progress();

        // verify that the job was built with the commands expected
        verify(processBuilder, times(1)).setCommand(anyString());
        verify(processBuilder).setCommand(eq(
        		CasdaValidationMetricProcessingDepositState.VALIDATION_METRIC_IMPORTER_TOOL_NAME));

        verify(processBuilder, times(3)).addCommandArgument(anyString(), anyString());
        verify(processBuilder).addCommandArgument("-parent-id", "12345");
        verify(processBuilder).addCommandArgument("-filename", "filename");
        verify(processBuilder).addCommandArgument("-infile", "dir/12345/filename");

        // verify that the job was started
        verify(jobManager, times(1)).startJob(eq(job));

        // it should not have progressed yet
        assertEquals(initial, catalogue.getDepositState());
    }

    @Test
    public void testProgressValidationMetricFailsWhenJobFailed()
    {
    	EvaluationFile catalogue = createValidationMetric();

        CasdaValidationMetricProcessingDepositState state =
                new CasdaValidationMetricProcessingDepositState(stateFactory, catalogue, depositObservationParentDirectory,
                        processBuilder, jobManager);

        String jobId = getEvaluationFileJobId(catalogue.getParent().getUniqueId());

        JobStatus failedJobStatus = mock(JobStatus.class);
        when(failedJobStatus.isFailed()).thenReturn(true);
        when(jobManager.getJobStatus(jobId)).thenReturn(failedJobStatus);

        state.progress();

        // if the job failed, it should not start a job
        verify(jobManager, never()).startJob(any(ProcessJob.class));

        // verify that the catalogue state is transitioned to failed
        assertEquals(failed, catalogue.getDepositState());

    }

    @Test
    public void testProgressValidationMetricDoesNotProgressWhenJobFinished()
    {
    	EvaluationFile file = createValidationMetric();

    	CasdaValidationMetricProcessingDepositState state =
                new CasdaValidationMetricProcessingDepositState(stateFactory, file, depositObservationParentDirectory,
                        processBuilder, jobManager);

        String jobId = getEvaluationFileJobId(file.getParent().getUniqueId());

        JobStatus finishedJobStatus = mock(JobStatus.class);
        when(finishedJobStatus.isFailed()).thenReturn(false);
        when(finishedJobStatus.isFinished()).thenReturn(true);
        when(jobManager.getJobStatus(jobId)).thenReturn(finishedJobStatus);

        state.progress();

        // if the job is finished, it should not start a job
        verify(jobManager, never()).startJob(any(ProcessJob.class));

        // verify that the catalogue state has not transitioned to the next status
        assertEquals(initial, file.getDepositState());
    }

    @Test
    public void testProgressValidationMetricDoesNothingWhileJobStillRunning()
    {
    	EvaluationFile file = createValidationMetric();

        CasdaValidationMetricProcessingDepositState state =
                new CasdaValidationMetricProcessingDepositState(stateFactory, file, depositObservationParentDirectory,
                        processBuilder, jobManager);

        String jobId = getEvaluationFileJobId(file.getParent().getUniqueId());

        JobStatus runningJobStatus = mock(JobStatus.class);
        when(runningJobStatus.isFailed()).thenReturn(false);
        when(runningJobStatus.isFinished()).thenReturn(false);
        when(jobManager.getJobStatus(jobId)).thenReturn(runningJobStatus);

        state.progress();

        // if the job is finished, it should not start a job
        verify(jobManager, never()).startJob(any(ProcessJob.class));

        // verify that the catalogue state doesn't change
        assertEquals(initial, file.getDepositState());
    }

    private EvaluationFile createValidationMetric()
    {
        EvaluationFile file = new EvaluationFile();
        file.setFilename("filename");
        file.setFormat("validation-metrics");
        file.setDepositState(initial);

        when(stateFactory.createState(eq(Type.PROCESSED), eq(file))).thenReturn(next);
        when(stateFactory.createState(eq(Type.FAILED), eq(file))).thenReturn(failed);


        Observation observation = new Observation();
        observation.setSbid(12345);
        observation.addEvaluationFile(file);

        return file;
    }

    private String getEvaluationFileJobId(String sbid)
    {
        String prefix = "validation_metric_import-observations/";
        return prefix + sbid + "/evaluation_files/filename-0";
    }
}
