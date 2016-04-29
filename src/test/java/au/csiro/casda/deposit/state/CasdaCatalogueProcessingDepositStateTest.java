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
import au.csiro.casda.entity.observation.Catalogue;
import au.csiro.casda.entity.observation.CatalogueType;
import au.csiro.casda.entity.observation.Observation;

/**
 * Verify the functions of the CasdaCatalogueProcessingDepositState class.
 * 
 * Copyright 2015, CSIRO Australia
 * All rights reserved.
 */
public class CasdaCatalogueProcessingDepositStateTest
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
    public void testProgressContinuumComponentNotStartedStartsJob()
    {
        testProgressCatalogueStartsNewJob(CatalogueType.CONTINUUM_COMPONENT, "continuum-component");
    }

    @Test
    public void testProgressContinuumComponentCatalogueFailsWhenJobFailed()
    {
        testProgressCatalogueFailsWhenJobFailed(CatalogueType.CONTINUUM_COMPONENT);
    }

    @Test
    public void testProgressContinuumComponentCatalogueProgressesWhenJobFinished()
    {
        testProgressCatalogueProgressesWhenJobFinished(CatalogueType.CONTINUUM_COMPONENT);
    }

    @Test
    public void testProgressContinuumComponentCatalogueDoesNothingWhileJobStillRunning()
    {
        testProgressCatalogueDoesNothingWhileJobStillRunning(CatalogueType.CONTINUUM_COMPONENT);
    }

    @Test
    public void testProgressContinuumIslandCatalogueFailsWhenJobFailed()
    {
        testProgressCatalogueFailsWhenJobFailed(CatalogueType.CONTINUUM_ISLAND);
    }

    @Test
    public void testProgressContinuumIslandNotStartedStartsJob()
    {
        testProgressCatalogueStartsNewJob(CatalogueType.CONTINUUM_ISLAND, "continuum-island");
    }

    @Test
    public void testProgressContinuumIslandCatalogueProgressesWhenJobFinished()
    {
        testProgressCatalogueProgressesWhenJobFinished(CatalogueType.CONTINUUM_ISLAND);
    }

    @Test
    public void testProgressContinuumIslandCatalogueDoesNothingWhileJobStillRunning()
    {
        testProgressCatalogueDoesNothingWhileJobStillRunning(CatalogueType.CONTINUUM_ISLAND);
    }

    @Test
    public void testProgressSpectralLineAbsorptionCatalogueFailsWhenJobFailed()
    {
        testProgressCatalogueFailsWhenJobFailed(CatalogueType.SPECTRAL_LINE_ABSORPTION);
    }
    
    @Test
    public void testProgressSpectralLineEmissionCatalogueFailsWhenJobFailed()
    {
        testProgressCatalogueFailsWhenJobFailed(CatalogueType.SPECTRAL_LINE_EMISSION);
    }

    @Test
    public void testProgressSpectralLineAbsorptionNotStartedStartsJob()
    {
        testProgressCatalogueStartsNewJob(CatalogueType.SPECTRAL_LINE_ABSORPTION, "spectral-line-absorption");
    }
    
    @Test
    public void testProgressSpectralLineEmissionNotStartedStartsJob()
    {
        testProgressCatalogueStartsNewJob(CatalogueType.SPECTRAL_LINE_EMISSION, "spectral-line-emission");
    }

    @Test
    public void testProgressSpectralLineAbsorptionCatalogueProgressesWhenJobFinished()
    {
        testProgressCatalogueProgressesWhenJobFinished(CatalogueType.SPECTRAL_LINE_ABSORPTION);
    }
    
    @Test
    public void testProgressSpectralLineEmissionCatalogueProgressesWhenJobFinished()
    {
        testProgressCatalogueProgressesWhenJobFinished(CatalogueType.SPECTRAL_LINE_EMISSION);
    }

    @Test
    public void testProgressSpectralLineAbsorptionCatalogueDoesNothingWhileJobStillRunning()
    {
        testProgressCatalogueDoesNothingWhileJobStillRunning(CatalogueType.SPECTRAL_LINE_ABSORPTION);
    }
    
    @Test
    public void testProgressSpectralLineEmissionCatalogueDoesNothingWhileJobStillRunning()
    {
        testProgressCatalogueDoesNothingWhileJobStillRunning(CatalogueType.SPECTRAL_LINE_EMISSION);
    }

    @Test
    public void testProgressPolarisationCatalogueFailsWhenJobFailed()
    {
        testProgressCatalogueFailsWhenJobFailed(CatalogueType.POLARISATION_COMPONENT);
    }

    @Test
    public void testProgressPolarisationNotStartedStartsJob()
    {
        testProgressCatalogueStartsNewJob(CatalogueType.POLARISATION_COMPONENT, "polarisation-component");
    }

    @Test
    public void testProgressPolarisationCatalogueProgressesWhenJobFinished()
    {
        testProgressCatalogueProgressesWhenJobFinished(CatalogueType.POLARISATION_COMPONENT);
    }

    @Test
    public void testProgressPolarisationCatalogueDoesNothingWhileJobStillRunning()
    {
        testProgressCatalogueDoesNothingWhileJobStillRunning(CatalogueType.POLARISATION_COMPONENT);
    }

    private void testProgressCatalogueStartsNewJob(CatalogueType catalogueType, String expectedCatalogueCommandArg)
    {
        Catalogue catalogue = createCatalogue(catalogueType);

        ProcessJob job = mock(ProcessJob.class);

        CasdaCatalogueProcessingDepositState state =
                new CasdaCatalogueProcessingDepositState(stateFactory, catalogue, depositObservationParentDirectory,
                        processBuilder, jobManager);

        String jobId = getCatalogueJobId(catalogue.getParent().getUniqueId());

        when(jobManager.getJobStatus(jobId)).thenReturn(null);

        when(processBuilder.createJob(eq(jobId), eq(CasdaCatalogueProcessingDepositState.CATALOGUE_IMPORTER_TOOL_NAME)))
                .thenReturn(job);

        state.progress();

        // verify that the job was built with the commands expected
        verify(processBuilder, times(1)).setCommand(anyString());
        verify(processBuilder).setCommand(eq(CasdaCatalogueProcessingDepositState.CATALOGUE_IMPORTER_TOOL_NAME));

        verify(processBuilder, times(4)).addCommandArgument(anyString(), anyString());
        verify(processBuilder).addCommandArgument("-catalogue-type", expectedCatalogueCommandArg);
        verify(processBuilder).addCommandArgument("-parent-id", "12345");
        verify(processBuilder).addCommandArgument("-catalogue-filename", "filename");
        verify(processBuilder).addCommandArgument("-infile", "dir/12345/filename");

        // verify that the job was started
        verify(jobManager, times(1)).startJob(eq(job));

        // it should not have progressed yet
        assertEquals(initial, catalogue.getDepositState());
    }

    public void testProgressCatalogueFailsWhenJobFailed(CatalogueType catalogueType)
    {
        Catalogue catalogue = createCatalogue(catalogueType);

        CasdaCatalogueProcessingDepositState state =
                new CasdaCatalogueProcessingDepositState(stateFactory, catalogue, depositObservationParentDirectory,
                        processBuilder, jobManager);

        String jobId = getCatalogueJobId(catalogue.getParent().getUniqueId());

        JobStatus failedJobStatus = mock(JobStatus.class);
        when(failedJobStatus.isFailed()).thenReturn(true);
        when(jobManager.getJobStatus(jobId)).thenReturn(failedJobStatus);

        state.progress();

        // if the job failed, it should not start a job
        verify(jobManager, never()).startJob(any(ProcessJob.class));

        // verify that the catalogue state is transitioned to failed
        assertEquals(failed, catalogue.getDepositState());

    }

    public void testProgressCatalogueProgressesWhenJobFinished(CatalogueType catalogueType)
    {
        Catalogue catalogue = createCatalogue(catalogueType);

        CasdaCatalogueProcessingDepositState state =
                new CasdaCatalogueProcessingDepositState(stateFactory, catalogue, depositObservationParentDirectory,
                        processBuilder, jobManager);

        String jobId = getCatalogueJobId(catalogue.getParent().getUniqueId());

        JobStatus finishedJobStatus = mock(JobStatus.class);
        when(finishedJobStatus.isFailed()).thenReturn(false);
        when(finishedJobStatus.isFinished()).thenReturn(true);
        when(jobManager.getJobStatus(jobId)).thenReturn(finishedJobStatus);

        state.progress();

        // if the job is finished, it should not start a job
        verify(jobManager, never()).startJob(any(ProcessJob.class));

        // verify that the catalogue state is transitioned to the next status
        assertEquals(next, catalogue.getDepositState());
    }

    public void testProgressCatalogueDoesNothingWhileJobStillRunning(CatalogueType catalogueType)
    {
        Catalogue catalogue = createCatalogue(catalogueType);

        CasdaCatalogueProcessingDepositState state =
                new CasdaCatalogueProcessingDepositState(stateFactory, catalogue, depositObservationParentDirectory,
                        processBuilder, jobManager);

        String jobId = getCatalogueJobId(catalogue.getParent().getUniqueId());

        JobStatus runningJobStatus = mock(JobStatus.class);
        when(runningJobStatus.isFailed()).thenReturn(false);
        when(runningJobStatus.isFinished()).thenReturn(false);
        when(jobManager.getJobStatus(jobId)).thenReturn(runningJobStatus);

        state.progress();

        // if the job is finished, it should not start a job
        verify(jobManager, never()).startJob(any(ProcessJob.class));

        // verify that the catalogue state doesn't change
        assertEquals(initial, catalogue.getDepositState());
    }

    private Catalogue createCatalogue(CatalogueType catalogueType)
    {
        Catalogue catalogue = new Catalogue();
        catalogue.setCatalogueType(catalogueType);
        catalogue.setFilename("filename");

        catalogue.setDepositState(initial);

        when(stateFactory.createState(eq(Type.PROCESSED), eq(catalogue))).thenReturn(next);
        when(stateFactory.createState(eq(Type.FAILED), eq(catalogue))).thenReturn(failed);

        Observation observation = new Observation();
        observation.setSbid(12345);
        observation.addCatalogue(catalogue);

        return catalogue;
    }

    private String getCatalogueJobId(String sbid)
    {
        return "catalogue_import-observations/" + sbid + "/catalogues/filename-0";
    }
}
