package au.csiro.casda.deposit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import au.csiro.casda.deposit.JobQueueUiController.QueueSummary;
import au.csiro.casda.deposit.jobqueue.QueuedJob;
import au.csiro.casda.deposit.jobqueue.QueuedJobManager;
import au.csiro.casda.deposit.jobqueue.QueuedJobStatus;
import au.csiro.casda.deposit.jobqueue.TestJob;
import au.csiro.casda.jobmanager.JobManager.Job;

/*
 * #%L
 * CSIRO Data Access Portal
 * %%
 * Copyright (C) 2010 - 2017 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

/**
 * Tests the Job Queue UI Controller.
 * <p>
 * Copyright 2017, CSIRO Australia. All rights reserved.
 */
public class JobQueueUiControllerTest
{

    @InjectMocks
    private JobQueueUiController controller;

    @Mock
    private QueuedJobManager jobManager;

    @Mock
    private HttpServletRequest httpServletRequest;

    /**
     * Set up the ui controller before each test.
     * 
     * @throws Exception
     *             any exception thrown during set up
     */
    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("localhost:8080/hello/"));
    }

    @Test
    public void testQueuedJobStatusSetsModelKeysForNoJobs()
    {
        Model model = spy(new ExtendedModelMap());

        when(jobManager.getQueuedJobMap()).thenReturn(new HashMap<>());

        String newPage = controller.queuedJobStatus(httpServletRequest, model);
        assertEquals(JobQueueUiController.JOB_QUEUE_PAGE, newPage);

        Set<String> expectedModelKeys = new HashSet<>(
                Arrays.asList(JobQueueUiController.QUEUED_JOB_MAP_KEY, JobQueueUiController.JOB_TYPE_LIST_KEY,
                        JobQueueUiController.JOB_TYPE_MAX_JOBS_KEY, JobQueueUiController.NUM_JOBS_KEY,
                        JobQueueUiController.MAX_JOBS_KEY, JobQueueUiController.URL, JobQueueUiController.PAUSED));
        assertEquals(expectedModelKeys, model.asMap().keySet());
        assertEquals(new HashMap<String, QueueSummary>(), model.asMap().get(JobQueueUiController.QUEUED_JOB_MAP_KEY));
        assertEquals(0, model.asMap().get(JobQueueUiController.NUM_JOBS_KEY));
        assertEquals(0, model.asMap().get(JobQueueUiController.MAX_JOBS_KEY));

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testQueuedJobStatus()
    {
        Model model = spy(new ExtendedModelMap());

        HashMap<String, List<QueuedJob>> queuedJobMap = new HashMap<>();
        String jobTypeStage = "stage_artefact";
        String jobTypeRegister = "register_artefact";
        Job unstartedJob = new TestJob("test1", jobTypeStage);
        QueuedJob queuedJob = new QueuedJob(unstartedJob);
        Job runningJob = new TestJob("test2", jobTypeStage);
        QueuedJob queuedJob2 = new QueuedJob(runningJob);
        queuedJob2.setStatus(QueuedJobStatus.RUNNING);
        Job completedJob = new TestJob("test3", jobTypeRegister);
        QueuedJob queuedJob3 = new QueuedJob(completedJob);
        queuedJob3.setStatus(QueuedJobStatus.COMPLETED);
        List<QueuedJob> stagedJobs = new ArrayList<>();
        stagedJobs.add(queuedJob);
        stagedJobs.add(queuedJob2);
        queuedJobMap.put(jobTypeStage, stagedJobs);
        List<QueuedJob> registeredJobs = new ArrayList<>();
        registeredJobs.add(queuedJob3);
        queuedJobMap.put(jobTypeRegister, registeredJobs);
        when(jobManager.getQueuedJobMap()).thenReturn(queuedJobMap);
        when(jobManager.getNumAllowedJobs(jobTypeStage)).thenReturn(2);
        when(jobManager.getNumAllowedJobs(jobTypeRegister)).thenReturn(-1);

        String newPage = controller.queuedJobStatus(httpServletRequest, model);
        assertEquals(JobQueueUiController.JOB_QUEUE_PAGE, newPage);
        Set<String> expectedModelKeys = new HashSet<>(
                Arrays.asList(JobQueueUiController.QUEUED_JOB_MAP_KEY, JobQueueUiController.JOB_TYPE_LIST_KEY,
                        JobQueueUiController.JOB_TYPE_MAX_JOBS_KEY, JobQueueUiController.NUM_JOBS_KEY,
                        JobQueueUiController.MAX_JOBS_KEY, JobQueueUiController.URL, JobQueueUiController.PAUSED));
        assertEquals(expectedModelKeys, model.asMap().keySet());
        Map<String, QueueSummary> queueSummaryMap =
                (Map<String, QueueSummary>) model.asMap().get(JobQueueUiController.QUEUED_JOB_MAP_KEY);
        assertThat(queueSummaryMap.keySet(),
                containsInAnyOrder(Arrays.asList(equalTo(jobTypeStage), equalTo(jobTypeRegister))));
        QueueSummary queueSummary = queueSummaryMap.get(jobTypeStage);
        assertThat(queueSummary.getMaxAllowedJobs(), is(2));
        assertThat(queueSummary.getNumCompletedJobs(), is(0));
        assertThat(queueSummary.getNumQueuedJobs(), is(0));
        List<QueuedJob> displayJobs = queueSummary.getDisplayJobs();
        assertThat(displayJobs.get(0).getJobId(), is("test1"));
        assertThat(displayJobs.get(1).getJobId(), is("test2"));
        assertThat(displayJobs.size(), is(2));

        queueSummary = queueSummaryMap.get(jobTypeRegister);
        assertThat(queueSummary.getMaxAllowedJobs(), is(-1));
        assertThat(queueSummary.getNumCompletedJobs(), is(1));
        assertThat(queueSummary.getNumQueuedJobs(), is(0));
        displayJobs = queueSummary.getDisplayJobs();
        assertThat(displayJobs.size(), is(0));

        assertEquals(1, model.asMap().get(JobQueueUiController.NUM_JOBS_KEY));
        assertEquals(2, model.asMap().get(JobQueueUiController.MAX_JOBS_KEY));
    }

}
