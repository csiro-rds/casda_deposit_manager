package au.csiro.casda.datadeposit;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import au.csiro.casda.datadeposit.DepositState.Type;
import au.csiro.casda.deposit.CasdaToolProcessJobBuilderFactory;
import au.csiro.casda.deposit.SingleJobMonitorFactory;
import au.csiro.casda.deposit.jdbc.SimpleJdbcRepository;
import au.csiro.casda.deposit.services.NgasService;
import au.csiro.casda.deposit.services.NgasService.Status;
import au.csiro.casda.deposit.services.VoToolsService;
import au.csiro.casda.deposit.state.CasdaDepositStateFactory;
import au.csiro.casda.deposit.state.CasdaDepositStateFactory.ProcessJobType;
import au.csiro.casda.entity.observation.EncapsulationFile;
import au.csiro.casda.jobmanager.CasdaToolProcessJobBuilder;
import au.csiro.casda.jobmanager.JavaProcessJobFactory;
import au.csiro.casda.jobmanager.JobManager;
import au.csiro.casda.jobmanager.JobManager.JobStatus;
import au.csiro.casda.jobmanager.ProcessJob;
import au.csiro.casda.jobmanager.ProcessJobBuilder;
import au.csiro.casda.jobmanager.SingleJobMonitor;

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
 * CasdaDepositableArtefactEntity Test
 * <p>
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
public abstract class AbstractDepositableArtefactTest
{
    @Rule
    public ExpectedException exception = ExpectedException.none();

    protected DepositStateFactory depositStateFactory;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private File observationParentDir;

    private File level7ParentDir;

    @Mock
    private JobManager jobManager;

    @Mock
    private NgasService ngasService;

    @Mock
    private CasdaToolProcessJobBuilderFactory factory;

    @Spy
    private SingleJobMonitorFactory singleJobMonitorFactory;

    @Mock
    private CasdaToolProcessJobBuilder processBuilder;

    @Mock
    private VoToolsService voToolsService;

    @Mock
    private SimpleJdbcRepository simpleJdbcRepository;

    @Before
    public void setup() throws Exception
    {
        MockitoAnnotations.initMocks(this);

        observationParentDir = tempFolder.newFolder("observation");
        level7ParentDir = tempFolder.newFolder("level7");
        
		JobStatus status = mock(JobStatus.class);
		when(jobManager.getJobStatus(any(String.class))).thenReturn(status);
		when(status.isFinished()).thenReturn(true);
		
		Status ngasStatus = mock(Status.class);
		when(ngasStatus.wasFailure()).thenReturn(false);
		when(ngasService.getStatus(any(String.class))).thenReturn(ngasStatus);
		
		singleJobMonitorFactory = mock(SingleJobMonitorFactory.class);
		SingleJobMonitor jobMonitor = mock(SingleJobMonitor.class);
		when(singleJobMonitorFactory.createSingleJobMonitor()).thenReturn(jobMonitor);
		when(jobMonitor.isJobFailed()).thenReturn(false);
		when(jobMonitor.isJobFinished()).thenReturn(true);
		when(jobMonitor.getJobOutput()).thenReturn("DUL");

        this.depositStateFactory = spy(new CasdaDepositStateFactory(ngasService, jobManager, factory,
                new JavaProcessJobFactory(), singleJobMonitorFactory, voToolsService, simpleJdbcRepository, "",
                observationParentDir.getAbsolutePath(), level7ParentDir.getAbsolutePath(), "{\"stageCommand\"}",
                "SIMPLE", "stageCommandAndArgs", "{\"registerCommand\"}", "SIMPLE", "registerCommandAndArgs",
                "{\"archiveStatus\"}", "{\"archivePut\"}",
                " {\"stage_artefact\", \"1\", \"register_artefact\", \"4\" }", ""));
        when(factory.createBuilder()).thenReturn(processBuilder);
        when(processBuilder.setCommand(any(String.class))).thenReturn(processBuilder);
        when(processBuilder.setProcessParameter(any(String.class), any(String.class))).thenReturn(processBuilder);
        when(processBuilder.addCommandArgument(any(String.class), any(String.class))).thenReturn(processBuilder);
        when(processBuilder.addCommandArguments(any(String[].class))).thenReturn(processBuilder);
        when(processBuilder.addCommandSwitch(any(String.class))).thenReturn(processBuilder);
    }

    @Test
    public void initialStateIsUndeposited()
    {
        DepositableArtefact depositableArtefact = createDepositableArtefact();
        StateChecks.checkStateIsInitial(depositableArtefact);
    }

    @Test
    public void progressDepositIsIgnoreWhenNoDepositStateFactorySet()
    {
        DepositableArtefact depositableArtefact = createDepositableArtefactWithoutDepositStateFactory();

        depositableArtefact.progressDeposit();
        StateChecks.checkStateIsInitial(depositableArtefact);
    }

    @Test
    public void progressDepositFromUndepositedTransitionsToProcessing()
    {
        DepositableArtefact depositableArtefact =
                createDepositableArtefactInState(DepositState.Type.UNDEPOSITED, DepositState.Type.UNDEPOSITED);

        depositableArtefact.progressDeposit();
        StateChecks.checkStateIsProcessing(depositableArtefact);
    }

    @Test
    public void progressDepositFromProcessingTransitionsToProcessed()
    {
        DepositableArtefact depositableArtefact =
                createDepositableArtefactInState(DepositState.Type.PROCESSING, DepositState.Type.PROCESSING);

        depositableArtefact.progressDeposit();
        StateChecks.checkStateIsProcessed(depositableArtefact);
    }

    @Test
    public void progressDepositFromProcessedTransitionsToStaging()
    {
        DepositableArtefact depositableArtefact =
                createDepositableArtefactInState(DepositState.Type.PROCESSED, DepositState.Type.PROCESSED);

        depositableArtefact.progressDeposit();
        StateChecks.checkStateIsStaging(depositableArtefact);
    }

    @Test
    public void progressDepositFromStagingTransitionsToStaged()
    {
        DepositableArtefact depositableArtefact =
                createDepositableArtefactInState(DepositState.Type.STAGING, DepositState.Type.STAGING);

        JobStatus success = mock(JobStatus.class);
        when(success.isFinished()).thenReturn(true);
        when(success.isFailed()).thenReturn(false);
        when(jobManager.getJobStatus(any(String.class))).thenReturn(success);

        depositableArtefact.progressDeposit();
        StateChecks.checkStateIsStaged(depositableArtefact);
    }

    @Test
    public void progressDepositFromStagedTransitionsToRegistering()
    {
        DepositableArtefact depositableArtefact =
                createDepositableArtefactInState(DepositState.Type.STAGED, DepositState.Type.STAGING);

        depositableArtefact.progressDeposit();
        StateChecks.checkStateIsRegistering(depositableArtefact);
    }

    @Test
    public void progressDepositFromRegisteringTransitionsToRegistered()
    {
        DepositableArtefact depositableArtefact =
                createDepositableArtefactInState(DepositState.Type.REGISTERING, DepositState.Type.STAGING);
        JobStatus success = mock(JobStatus.class);
        when(success.isFinished()).thenReturn(true);
        when(success.isFailed()).thenReturn(false);
        when(jobManager.getJobStatus("register_artefact-" + depositableArtefact.getUniqueIdentifier() + "-0"))
                .thenReturn(success);

        depositableArtefact.progressDeposit();
        StateChecks.checkStateIsRegistered(depositableArtefact);
    }

    @Test
    public void progressDepositFromRegisteredTransitionsToArchiving()
    {
        DepositableArtefact depositableArtefact =
                createDepositableArtefactInState(DepositState.Type.REGISTERED, DepositState.Type.REGISTERED);

        depositableArtefact.progressDeposit();
        StateChecks.checkStateIsArchiving(depositableArtefact);
    }

    public void progressDepositFromArchivingTransitionsToArchived() throws Exception
    {
        DepositableArtefact depositableArtefact =
                createDepositableArtefactInState(DepositState.Type.ARCHIVING, DepositState.Type.ARCHIVING);

        Status mockStatus = mock(Status.class);
        when(mockStatus.wasFailure()).thenReturn(false);
        when(ngasService.getStatus(depositableArtefact.getUniqueIdentifier().replace("/", "-"))).thenReturn(mockStatus);

        ProcessJobBuilder ProcessJobBuilder = mock(ProcessJobBuilder.class);
        doReturn(ProcessJobBuilder).when((CasdaDepositStateFactory) depositStateFactory)
                .createProcessJobBuilderForProcessJobType(any(ProcessJobType.class), any(String.class),
                        any(String.class));

        when(ProcessJobBuilder.setProcessParameter(any(String.class), any(String.class))).thenReturn(ProcessJobBuilder);
        ProcessJob job = mock(ProcessJob.class);
        when(ProcessJobBuilder.createJob(null, null)).thenReturn(job);
        doNothing().when(job).run(any(SingleJobMonitor.class));

        when(any(SingleJobMonitor.class).isJobFailed()).thenReturn(false);

        depositableArtefact.progressDeposit();
        StateChecks.checkStateIsArchived(depositableArtefact);
    }

    @Test
    public void progressDepositFromArchivedTransitionsToDeposited()
    {
        DepositableArtefact depositableArtefact =
                createDepositableArtefactInState(DepositState.Type.ARCHIVED, DepositState.Type.ARCHIVED);

        depositableArtefact.progressDeposit();
        StateChecks.checkStateIsDeposited(depositableArtefact);
    }

    @Test
    public void testFailedInvariants()
    {
        DepositableArtefact depositableArtefact =
                createDepositableArtefactInState(DepositState.Type.FAILED, DepositState.Type.PROCESSING);
        StateChecks.checkStateIsFailed(depositableArtefact);
    }

    @Test
    public void progressDepositWhenDepositedThrowsException()
    {
        DepositableArtefact depositableArtefact =
                createDepositableArtefactInState(DepositState.Type.DEPOSITED, DepositState.Type.DEPOSITED);

        exception.expect(DepositState.IllegalEventException.class);
        exception.expectMessage("State 'DEPOSITED' for depositable '" + depositableArtefact.getClass().getSimpleName()
                + "' does not respond to event 'progress'");

        depositableArtefact.progressDeposit();
    }

    @Test
    public void progressDepositWhenFailedThrowsException()
    {
        DepositableArtefact depositableArtefact =
                createDepositableArtefactInState(DepositState.Type.FAILED, DepositState.Type.PROCESSING);

        exception.expect(DepositState.IllegalEventException.class);
        exception.expectMessage("State 'FAILED' for depositable '" + depositableArtefact.getClass().getSimpleName()
                + "' does not respond to event 'progress'");

        depositableArtefact.progressDeposit();
    }

    @Test
    public void setDepositStateShouldNotifyListenerWhenStateChanges()
    {
        Type initialDepositStateType = DepositState.Type.UNDEPOSITED;
        DepositableArtefact depositableArtefact =
                createDepositableArtefactInState(initialDepositStateType, initialDepositStateType);

        DepositStateChangeListener listener = mock(DepositStateChangeListener.class);
        DepositState.Type nextDepositStateType = getRandomDepositStateTypeExcluding(initialDepositStateType);
        DepositState nextDepositState = mock(DepositState.class);
        when(nextDepositState.getType()).thenReturn(nextDepositStateType);

        depositableArtefact.setDepositStateChangeListener(listener);
        depositableArtefact.setDepositState(nextDepositState);

        verify(listener).stateChanged(depositableArtefact, initialDepositStateType, nextDepositStateType);
    }

    private Type getRandomDepositStateTypeExcluding(DepositState.Type... excludedTypes)
    {
        List<DepositState.Type> validNextStates =
                new ArrayList<DepositState.Type>(Arrays.asList(DepositState.Type.values()));
        for (int i = 0; i < excludedTypes.length; i++)
        {
            validNextStates.remove(excludedTypes[i]);
        }
        Type type = validNextStates.get(RandomUtils.nextInt(0, validNextStates.size() - 1));
        return type;
    }

    @Test
    public void setDepositStateShouldNotNotifyListenerStateNull()
    {
        Type initialDepositStateType = DepositState.Type.UNDEPOSITED;
        DepositableArtefact depositableArtefact =
                createDepositableArtefactInState(initialDepositStateType, initialDepositStateType);

        DepositStateChangeListener listener = mock(DepositStateChangeListener.class);
        DepositState.Type nextDepositStateType = getRandomDepositStateTypeExcluding(initialDepositStateType);
        DepositState nextDepositState = mock(DepositState.class);
        when(nextDepositState.getType()).thenReturn(nextDepositStateType);

        depositableArtefact.setDepositStateChangeListener(listener);
        depositableArtefact.setDepositState(nextDepositState);

        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("Expected depositState != null");

        try
        {
            depositableArtefact.setDepositState(null);
            fail("Expected IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
            verify(listener).stateChanged(depositableArtefact, initialDepositStateType, nextDepositStateType);
            verifyNoMoreInteractions(listener);
            throw e;
        }
    }

    @Test
    public void setDepositStateShouldNotNotifyListenerNull()
    {
        Type initialDepositStateType = DepositState.Type.UNDEPOSITED;
        DepositableArtefact depositableArtefact =
                createDepositableArtefactInState(initialDepositStateType, initialDepositStateType);

        DepositStateChangeListener listener = mock(DepositStateChangeListener.class);
        DepositState.Type nextDepositStateType = getRandomDepositStateTypeExcluding(initialDepositStateType);
        DepositState nextDepositState = mock(DepositState.class);
        when(nextDepositState.getType()).thenReturn(nextDepositStateType);

        // don't set the listener
        depositableArtefact.setDepositState(nextDepositState);

        verifyZeroInteractions(listener);
    }

    @Test
    public void setDepositStateShouldNotNotifySameState()
    {
        Type initialDepositStateType = DepositState.Type.PROCESSING;
        DepositableArtefact depositableArtefact =
                createDepositableArtefactInState(initialDepositStateType, initialDepositStateType);

        DepositStateChangeListener listener = mock(DepositStateChangeListener.class);
        DepositState.Type nextDepositStateType = DepositState.Type.PROCESSING;
        DepositState nextDepositState = mock(DepositState.class);
        when(nextDepositState.getType()).thenReturn(nextDepositStateType);

        depositableArtefact.setDepositStateChangeListener(listener);
        depositableArtefact.setDepositState(nextDepositState);

        verifyZeroInteractions(listener);
    }

    protected abstract DepositableArtefact createDepositableArtefact();

    protected abstract DepositableArtefact createDepositableArtefactInState(DepositState.Type depositStateType,
            Type checkpointDepositStateType);

    protected abstract DepositableArtefact createDepositableArtefactWithoutDepositStateFactory();
    
    protected abstract EncapsulationFile createEncapsulationFile();
}
