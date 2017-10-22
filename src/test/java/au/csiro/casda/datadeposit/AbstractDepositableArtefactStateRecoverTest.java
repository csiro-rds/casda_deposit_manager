package au.csiro.casda.datadeposit;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import static org.hamcrest.CoreMatchers.is;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import au.csiro.casda.datadeposit.DepositState.IllegalEventException;
import au.csiro.casda.datadeposit.DepositState.Type;
import au.csiro.casda.deposit.CasdaToolProcessJobBuilderFactory;
import au.csiro.casda.deposit.SingleJobMonitorFactory;
import au.csiro.casda.deposit.jdbc.SimpleJdbcRepository;
import au.csiro.casda.deposit.services.NgasService;
import au.csiro.casda.deposit.services.VoToolsService;
import au.csiro.casda.deposit.state.CasdaDepositStateFactory;
import au.csiro.casda.jobmanager.CasdaToolProcessJobBuilder;
import au.csiro.casda.jobmanager.JavaProcessJobFactory;
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
 * CasdaDepositableArtefactRecoveryEntity Test
 * <p>
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
public abstract class AbstractDepositableArtefactStateRecoverTest
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

    @Mock
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
        
        this.depositStateFactory = new CasdaDepositStateFactory(ngasService, jobManager, factory,
                new JavaProcessJobFactory(), singleJobMonitorFactory, voToolsService, simpleJdbcRepository, "",
                observationParentDir.getAbsolutePath(), level7ParentDir.getAbsolutePath(), "{\"stageCommand\"}",
                "SIMPLE", "stageCommandAndArgs", "{\"registerCommand\"}", "SIMPLE", "registerCommandAndArgs",
                "{\"archiveStatus\"}", "{\"archivePut\"}",
                " {\"stage_artefact\", \"1\", \"register_artefact\", \"4\" }", "");
        when(factory.createBuilder()).thenReturn(processBuilder);
        when(processBuilder.setCommand(any(String.class))).thenReturn(processBuilder);
        when(processBuilder.addCommandArgument(any(String.class), any(String.class))).thenReturn(processBuilder);
        when(processBuilder.addCommandArguments(any(String[].class))).thenReturn(processBuilder);
        when(processBuilder.addCommandSwitch(any(String.class))).thenReturn(processBuilder);
    }

    @Test
    public void initialStateRecoveryTest()
    {
        DepositableArtefact depositableArtefact = createDepositableArtefact();
        StateChecks.checkStateIsInitial(depositableArtefact);
        assertThat(depositableArtefact.getDepositFailureCount(), is(0));

        depositableArtefact.getDepositState().transitionTo(DepositState.Type.FAILED);
        StateChecks.checkStateIsFailed(depositableArtefact);
        assertThat(depositableArtefact.getDepositFailureCount(), is(1));
        depositableArtefact.recoverDeposit();
        assertThat(depositableArtefact.getDepositFailureCount(), is(1));
        StateChecks.checkStateIsInitial(depositableArtefact);

        depositableArtefact.getDepositState().transitionTo(DepositState.Type.FAILED);
        StateChecks.checkStateIsFailed(depositableArtefact);
        assertThat(depositableArtefact.getDepositFailureCount(), is(2));
        depositableArtefact.recoverDeposit();
        assertThat(depositableArtefact.getDepositFailureCount(), is(2));
        StateChecks.checkStateIsInitial(depositableArtefact);
    }

    @Test
    public void processingStateRecoveryTest()
    {
        DepositableArtefact depositableArtefact = createDepositableArtefactInState(DepositState.Type.UNDEPOSITED);
        StateChecks.checkStateIsInitial(depositableArtefact);
        depositableArtefact.progressDeposit();
        StateChecks.checkStateIsProcessing(depositableArtefact);
        assertThat(depositableArtefact.getDepositFailureCount(), is(0));

        depositableArtefact.getDepositState().transitionTo(DepositState.Type.FAILED);
        StateChecks.checkStateIsFailed(depositableArtefact);
        assertThat(depositableArtefact.getDepositFailureCount(), is(1));
        depositableArtefact.recoverDeposit();
        assertThat(depositableArtefact.getDepositFailureCount(), is(1));
        StateChecks.checkStateIsProcessing(depositableArtefact);

        depositableArtefact.getDepositState().transitionTo(DepositState.Type.FAILED);
        StateChecks.checkStateIsFailed(depositableArtefact);
        assertThat(depositableArtefact.getDepositFailureCount(), is(2));
        depositableArtefact.recoverDeposit();
        assertThat(depositableArtefact.getDepositFailureCount(), is(2));
        StateChecks.checkStateIsProcessing(depositableArtefact);
    }

    @Test
    public void processingStateRecoveryShouldFailIfNotInErrorStateTest()
    {
        DepositableArtefact depositableArtefact = createDepositableArtefactInState(DepositState.Type.UNDEPOSITED);
        StateChecks.checkStateIsInitial(depositableArtefact);
        assertThat(depositableArtefact.getDepositFailureCount(), is(0));

        try
        {
            depositableArtefact.recoverDeposit();
            fail("Expected IllegalEventException");
        }
        catch (IllegalEventException e)
        {
            assertThat(e.getMessage(), is("State 'UNDEPOSITED' for depositable '"
                    + depositableArtefact.getClass().getSimpleName() + "' does not respond to event 'recover'"));
            assertThat(depositableArtefact.getDepositFailureCount(), is(0));
        }

        depositableArtefact.progressDeposit();
        StateChecks.checkStateIsProcessing(depositableArtefact);
        assertThat(depositableArtefact.getDepositFailureCount(), is(0));

        try
        {
            depositableArtefact.recoverDeposit();
            fail("Expected IllegalEventException");
        }
        catch (IllegalEventException e)
        {
            assertThat(e.getMessage(), is("State 'PROCESSING' for depositable '"
                    + depositableArtefact.getClass().getSimpleName() + "' does not respond to event 'recover'"));
            assertThat(depositableArtefact.getDepositFailureCount(), is(0));
        }

        depositableArtefact.getDepositState().transitionTo(DepositState.Type.FAILED);
        StateChecks.checkStateIsFailed(depositableArtefact);
        assertThat(depositableArtefact.getDepositFailureCount(), is(1));
        depositableArtefact.recoverDeposit();
        assertThat(depositableArtefact.getDepositFailureCount(), is(1));
        StateChecks.checkStateIsProcessing(depositableArtefact);

        try
        {
            depositableArtefact.recoverDeposit();
            fail("Expected IllegalEventException");
        }
        catch (IllegalEventException e)
        {
            assertThat(e.getMessage(), is("State 'PROCESSING' for depositable '"
                    + depositableArtefact.getClass().getSimpleName() + "' does not respond to event 'recover'"));
            assertThat(depositableArtefact.getDepositFailureCount(), is(1));
        }

    }

    @Test
    public void processedStateRecoveryTest()
    {
        DepositableArtefact depositableArtefact = createDepositableArtefactInState(DepositState.Type.UNDEPOSITED);
        StateChecks.checkStateIsInitial(depositableArtefact);
        depositableArtefact.progressDeposit();
        depositableArtefact.progressDeposit();
        StateChecks.checkStateIsProcessed(depositableArtefact);
        assertThat(depositableArtefact.getDepositFailureCount(), is(0));

        depositableArtefact.getDepositState().transitionTo(DepositState.Type.FAILED);
        StateChecks.checkStateIsFailed(depositableArtefact);
        assertThat(depositableArtefact.getDepositFailureCount(), is(1));
        depositableArtefact.recoverDeposit();
        assertThat(depositableArtefact.getDepositFailureCount(), is(1));
        StateChecks.checkStateIsProcessed(depositableArtefact);

        depositableArtefact.getDepositState().transitionTo(DepositState.Type.FAILED);
        StateChecks.checkStateIsFailed(depositableArtefact);
        assertThat(depositableArtefact.getDepositFailureCount(), is(2));
        depositableArtefact.recoverDeposit();
        assertThat(depositableArtefact.getDepositFailureCount(), is(2));
        StateChecks.checkStateIsProcessed(depositableArtefact);
    }

    @Test
    public void stagingStateRecoveryTest()
    {
        DepositableArtefact depositableArtefact = createDepositableArtefactInState(DepositState.Type.UNDEPOSITED);
        StateChecks.checkStateIsInitial(depositableArtefact);
        depositableArtefact.progressDeposit();
        depositableArtefact.progressDeposit();
        depositableArtefact.progressDeposit();
        StateChecks.checkStateIsStaging(depositableArtefact);
        assertThat(depositableArtefact.getDepositFailureCount(), is(0));

        depositableArtefact.getDepositState().transitionTo(DepositState.Type.FAILED);
        StateChecks.checkStateIsFailed(depositableArtefact);
        assertThat(depositableArtefact.getDepositFailureCount(), is(1));
        depositableArtefact.recoverDeposit();
        assertThat(depositableArtefact.getDepositFailureCount(), is(1));
        StateChecks.checkStateIsStaging(depositableArtefact);

        depositableArtefact.getDepositState().transitionTo(DepositState.Type.FAILED);
        StateChecks.checkStateIsFailed(depositableArtefact);
        assertThat(depositableArtefact.getDepositFailureCount(), is(2));
        depositableArtefact.recoverDeposit();
        assertThat(depositableArtefact.getDepositFailureCount(), is(2));
        StateChecks.checkStateIsStaging(depositableArtefact);
    }

    @Test
    public void stagedStateRecoveryTest()
    {
        DepositableArtefact depositableArtefact = createDepositableArtefactInState(DepositState.Type.STAGED);
        depositableArtefact.setCheckpointStateType(Type.STAGING);

        StateChecks.checkStateIsStaged(depositableArtefact);
        assertThat(depositableArtefact.getDepositFailureCount(), is(0));

        depositableArtefact.getDepositState().transitionTo(DepositState.Type.FAILED);
        StateChecks.checkStateIsFailed(depositableArtefact);
        assertThat(depositableArtefact.getDepositFailureCount(), is(1));
        depositableArtefact.recoverDeposit();
        assertThat(depositableArtefact.getDepositFailureCount(), is(1));
        StateChecks.checkStateIsStaging(depositableArtefact);

        depositableArtefact.getDepositState().transitionTo(DepositState.Type.FAILED);
        StateChecks.checkStateIsFailed(depositableArtefact);
        assertThat(depositableArtefact.getDepositFailureCount(), is(2));
        depositableArtefact.recoverDeposit();
        assertThat(depositableArtefact.getDepositFailureCount(), is(2));
        StateChecks.checkStateIsStaging(depositableArtefact);
    }

    @Test
    public void registeringStateRecoveryTest()
    {
        DepositableArtefact depositableArtefact = createDepositableArtefactInState(DepositState.Type.REGISTERING);
        depositableArtefact.setCheckpointStateType(Type.STAGING);

        StateChecks.checkStateIsRegistering(depositableArtefact);
        assertThat(depositableArtefact.getDepositFailureCount(), is(0));

        depositableArtefact.getDepositState().transitionTo(DepositState.Type.FAILED);
        StateChecks.checkStateIsFailed(depositableArtefact);
        assertThat(depositableArtefact.getDepositFailureCount(), is(1));
        depositableArtefact.recoverDeposit();
        assertThat(depositableArtefact.getDepositFailureCount(), is(1));
        StateChecks.checkStateIsStaging(depositableArtefact);

        depositableArtefact.getDepositState().transitionTo(DepositState.Type.FAILED);
        StateChecks.checkStateIsFailed(depositableArtefact);
        assertThat(depositableArtefact.getDepositFailureCount(), is(2));
        depositableArtefact.recoverDeposit();
        assertThat(depositableArtefact.getDepositFailureCount(), is(2));
        StateChecks.checkStateIsStaging(depositableArtefact);
    }

    @Test
    public void registeredStateRecoveryTest()
    {
        DepositableArtefact depositableArtefact = createDepositableArtefactInState(DepositState.Type.REGISTERED);

        StateChecks.checkStateIsRegistered(depositableArtefact);
        assertThat(depositableArtefact.getDepositFailureCount(), is(0));

        depositableArtefact.getDepositState().transitionTo(DepositState.Type.FAILED);
        StateChecks.checkStateIsFailed(depositableArtefact);
        assertThat(depositableArtefact.getDepositFailureCount(), is(1));
        depositableArtefact.recoverDeposit();
        assertThat(depositableArtefact.getDepositFailureCount(), is(1));
        StateChecks.checkStateIsRegistered(depositableArtefact);

        depositableArtefact.getDepositState().transitionTo(DepositState.Type.FAILED);
        StateChecks.checkStateIsFailed(depositableArtefact);
        assertThat(depositableArtefact.getDepositFailureCount(), is(2));
        depositableArtefact.recoverDeposit();
        assertThat(depositableArtefact.getDepositFailureCount(), is(2));
        StateChecks.checkStateIsRegistered(depositableArtefact);
    }

    @Test
    public void depositedStateRecoveryTest()
    {
        DepositableArtefact depositableArtefact = createDepositableArtefactInState(DepositState.Type.DEPOSITED);

        StateChecks.checkStateIsDeposited(depositableArtefact);
        assertThat(depositableArtefact.getDepositFailureCount(), is(0));

        depositableArtefact.getDepositState().transitionTo(DepositState.Type.FAILED);
        StateChecks.checkStateIsFailed(depositableArtefact);
        assertThat(depositableArtefact.getDepositFailureCount(), is(1));
        depositableArtefact.recoverDeposit();
        assertThat(depositableArtefact.getDepositFailureCount(), is(1));
        StateChecks.checkStateIsDeposited(depositableArtefact);

        depositableArtefact.getDepositState().transitionTo(DepositState.Type.FAILED);
        StateChecks.checkStateIsFailed(depositableArtefact);
        assertThat(depositableArtefact.getDepositFailureCount(), is(2));
        depositableArtefact.recoverDeposit();
        assertThat(depositableArtefact.getDepositFailureCount(), is(2));
        StateChecks.checkStateIsDeposited(depositableArtefact);
    }

    protected abstract DepositableArtefact createDepositableArtefact();

    protected abstract DepositableArtefact createDepositableArtefactInState(DepositState.Type depositStateType);
}
