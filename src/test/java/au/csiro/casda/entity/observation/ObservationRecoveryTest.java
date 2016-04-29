package au.csiro.casda.entity.observation;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositState.IllegalEventException;
import au.csiro.casda.datadeposit.DepositState.Type;
import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.deposit.CasdaToolProcessJobBuilderFactory;
import au.csiro.casda.deposit.SingleJobMonitorFactory;
import au.csiro.casda.deposit.services.NgasService;
import au.csiro.casda.deposit.services.VoToolsService;
import au.csiro.casda.deposit.state.CasdaDepositStateFactory;
import au.csiro.casda.entity.CasdaDepositableEntity;
import au.csiro.casda.jobmanager.CasdaToolProcessJobBuilder;
import au.csiro.casda.jobmanager.JavaProcessJobFactory;
import au.csiro.casda.jobmanager.JobManager;
import au.csiro.casda.jobmanager.JobManager.JobStatus;

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
 * Test Observation recovery after it is progressed() and failed.
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
public class ObservationRecoveryTest
{
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private DepositStateFactory depositStateFactory;

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
    private CasdaToolProcessJobBuilder processBuilder;

    @Mock
    private VoToolsService voToolsService;

    @Before
    public void setup() throws Exception
    {
        MockitoAnnotations.initMocks(this);

        observationParentDir = tempFolder.newFolder("observation");
        level7ParentDir = tempFolder.newFolder("level7");

        this.depositStateFactory =
                new CasdaDepositStateFactory(ngasService, jobManager, factory, new JavaProcessJobFactory(),
                        new SingleJobMonitorFactory(), voToolsService, observationParentDir.getAbsolutePath(),
                        level7ParentDir.getAbsolutePath(), "{\"stageCommand\"}", "SIMPLE", "stageCommandAndArgs",
                        "{\"registerCommand\"}", "SIMPLE", "registerCommandAndArgs", "{\"archiveStatus\"}",
                        "{\"archivePut\"}", " {\"stage_artefact\", \"1\", \"register_artefact\", \"4\" }");
        when(factory.createBuilder()).thenReturn(processBuilder);
        when(processBuilder.setCommand(any(String.class))).thenReturn(processBuilder);
        when(processBuilder.addCommandArgument(any(String.class), any(String.class))).thenReturn(processBuilder);
        when(processBuilder.addCommandArguments(any(String[].class))).thenReturn(processBuilder);
        when(processBuilder.addCommandSwitch(any(String.class))).thenReturn(processBuilder);
    }

    @Test
    public void testRecoverFromNotifyingState()
    {
        CasdaDepositableEntity observation = createDepositableArtefactInState(DepositState.Type.NOTIFYING);
        ObservationStateChecks.checkStateIsNotifying(observation);

        JobStatus success = mock(JobStatus.class);
        when(jobManager.getJobStatus(any(String.class))).thenReturn(success);
        when(success.isFailed()).thenReturn(false);
        when(success.isFinished()).thenReturn(true);

        observation.progressDeposit();
        ObservationStateChecks.checkStateIsDeposited(observation);
        // Simulate failure
        observation.setDepositState(depositStateFactory.createState(DepositState.Type.FAILED, observation));

        ObservationStateChecks.checkStateIsFailed(observation);
        observation.recoverDeposit();
        ObservationStateChecks.checkStateIsDeposited(observation);
    }

    @Test
    public void testRecoverFromNotifyingStateWhereItFails()
    {
        // Can't do from DEPOSITING state and transition to NOTIFYING similar to other tests as observations will only
        // progress like that when their artifacts do but in this test we don't have such control over them. Other tests
        // (eg. ObservationDepositingRecoveryTest) do that.
        CasdaDepositableEntity observation = createDepositableArtefactInState(DepositState.Type.NOTIFYING);
        ObservationStateChecks.checkStateIsNotifying(observation);
        // observation.progressDeposit(); Can't progress as per above comment
        // Simulate failure
        observation.setDepositState(depositStateFactory.createState(DepositState.Type.FAILED, observation));

        ObservationStateChecks.checkStateIsFailed(observation);
        observation.recoverDeposit();
        ObservationStateChecks.checkStateIsNotifying(observation);
    }

    @Test
    public void testRecoverFromDepositedState()
    {
        thrown.expect(IllegalEventException.class);
        DepositState.Type type = DepositState.Type.DEPOSITED;
        CasdaDepositableEntity observation = createDepositableArtefactInState(type);
        ObservationStateChecks.checkStateIsDeposited(observation);
        observation.progressDeposit();
    }

    @Test
    public void testRecoverFromUndepositedState()
    {
        DepositState.Type type = DepositState.Type.UNDEPOSITED;
        CasdaDepositableEntity observation = createDepositableArtefactInState(type);
        ObservationStateChecks.checkStateIsInitial(observation);
        observation.progressDeposit();
        ObservationStateChecks.checkStateIsDepositing(observation);
        // Simulate failure
        observation.setDepositState(depositStateFactory.createState(DepositState.Type.FAILED, observation));

        ObservationStateChecks.checkStateIsFailed(observation);
        observation.recoverDeposit();
        ObservationStateChecks.checkStateIsDepositing(observation);
    }

    @Test
    public void testRecoverFromFailedState()
    {
        thrown.expect(IllegalEventException.class);
        DepositState.Type type = DepositState.Type.FAILED;
        CasdaDepositableEntity observation = createDepositableArtefactInState(type);
        ObservationStateChecks.checkStateIsFailed(observation);
        observation.progressDeposit();
    }

    private CasdaDepositableEntity createDepositableArtefactInState(Type depositState)
    {
        Observation observation = new Observation(1234);
        observation.setDepositStateFactory(depositStateFactory);
        observation.setDepositState(depositStateFactory.createState(depositState, observation));
        return observation;
    }
}
