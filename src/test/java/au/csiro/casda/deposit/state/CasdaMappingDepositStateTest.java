package au.csiro.casda.deposit.state;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
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
import au.csiro.casda.entity.observation.ImageCube;
import au.csiro.casda.entity.observation.Observation;
import au.csiro.casda.entity.observation.Project;
import au.csiro.casda.jobmanager.CasdaToolProcessJobBuilder;
import au.csiro.casda.jobmanager.JobManager;
import au.csiro.casda.jobmanager.JobManager.JobStatus;
import au.csiro.casda.jobmanager.ProcessJob;

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
 * Test the implementation of the casda staging deposit state
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 * 
 */
public class CasdaMappingDepositStateTest
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

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        when(processBuilder.setCommand(anyString())).thenReturn(processBuilder);
        when(processBuilder.addCommandArgument(anyString(), anyString())).thenReturn(processBuilder);

        when(initial.getType()).thenReturn(Type.MAPPING);
        when(next.getType()).thenReturn(Type.MAPPED);
        when(failed.getType()).thenReturn(Type.FAILED);

        when(stateFactory.createState(eq(Type.FAILED), anyObject())).thenReturn(failed);
        when(stateFactory.createState(eq(Type.MAPPED), anyObject())).thenReturn(next);
    }

    @Test
    public void testProgressMappingStartsNewJob()
    {
        ImageCube imageCube = createImageCube();

        ProcessJob job = mock(ProcessJob.class);

        CasdaMappingDepositState state =
                new CasdaMappingDepositState(stateFactory, imageCube, processBuilder, jobManager, "", "path");

        verify(processBuilder, times(2)).setProcessParameter(anyString(), anyString());
        verify(processBuilder).setProcessParameter("imageFile", "/42/image.fits");
        verify(processBuilder).setProcessParameter("projectCode", "AA000");
        verify(processBuilder).setWorkingDirectory("path");
        
        when(jobManager.getJobStatus(anyString())).thenReturn(null);

        when(processBuilder.createJob(anyString(), anyString())).thenReturn(job);

        state.progress();

        // verify that the job was built with the commands expected
        verify(processBuilder, times(1)).createJob(eq("mapping-observations/42/image_cubes/image.fits-0"),
                eq("mapping"));

        // verify that the job was started
        verify(jobManager, times(1)).startJob(eq(job));

        // it should not have progressed yet
        assertEquals(initial, imageCube.getDepositState());
    }

    @Test
    public void testProgressMappingFailsWhenJobFails()
    {
        ImageCube imageCube = createImageCube();

        CasdaMappingDepositState state =
                new CasdaMappingDepositState(stateFactory, imageCube, processBuilder, jobManager, "", "");

        JobStatus failedJobStatus = mock(JobStatus.class);
        when(failedJobStatus.isFailed()).thenReturn(true);
        when(jobManager.getJobStatus(anyString())).thenReturn(failedJobStatus);

        state.progress();

        // if the job failed, it should not start a job
        verify(processBuilder, never()).createJob(anyString(), anyString());
        verify(jobManager, never()).startJob(any(ProcessJob.class));

        // verify that the catalogue state is transitioned to failed
        assertEquals(failed, imageCube.getDepositState());
    }

    @Test
    public void testProgressMappingProgressesWhenFinished()
    {
        ImageCube imageCube = createImageCube();

        CasdaMappingDepositState state =
                new CasdaMappingDepositState(stateFactory, imageCube, processBuilder, jobManager, "", "");

        JobStatus completedJobStatus = mock(JobStatus.class);
        when(completedJobStatus.isFailed()).thenReturn(false);
        when(completedJobStatus.isFinished()).thenReturn(true);
        when(jobManager.getJobStatus(anyString())).thenReturn(completedJobStatus);

        state.progress();

        // if the job failed, it should not start a job
        verify(processBuilder, never()).createJob(anyString(), anyString());
        verify(jobManager, never()).startJob(any(ProcessJob.class));

        // verify that the catalogue state is transitioned to failed
        assertEquals(next, imageCube.getDepositState());
    }

    @Test
    public void testProgressMappingDoesNothingWhileJobStillRunning()
    {
        ImageCube imageCube = createImageCube();

        CasdaMappingDepositState state =
                new CasdaMappingDepositState(stateFactory, imageCube, processBuilder, jobManager, "", "");

        JobStatus runningJobStatus = mock(JobStatus.class);
        when(runningJobStatus.isFailed()).thenReturn(false);
        when(runningJobStatus.isFinished()).thenReturn(false);
        when(jobManager.getJobStatus(anyString())).thenReturn(runningJobStatus);

        state.progress();

        // if the job failed, it should not start a job
        verify(processBuilder, never()).createJob(anyString(), anyString());
        verify(jobManager, never()).startJob(any(ProcessJob.class));

        // verify that the image cube state doesn't change
        assertEquals(initial, imageCube.getDepositState());
    }

    private ImageCube createImageCube()
    {
        ImageCube imageCube = new ImageCube();
        Observation obs = new Observation();
        obs.setSbid(42);
        imageCube.setParent(obs);
        Project project = new Project("AA000");
        imageCube.setProject(project);
        imageCube.setFilename("image.fits");
        imageCube.setDepositState(initial);
        return imageCube;
    }

}
