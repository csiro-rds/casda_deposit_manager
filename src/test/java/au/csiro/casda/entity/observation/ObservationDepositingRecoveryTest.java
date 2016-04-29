package au.csiro.casda.entity.observation;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import au.csiro.casda.datadeposit.ChildDepositableArtefact;
import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositState.IllegalEventException;
import au.csiro.casda.datadeposit.DepositState.Type;
import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.datadeposit.StateChecks;
import au.csiro.casda.deposit.CasdaToolProcessJobBuilderFactory;
import au.csiro.casda.deposit.SingleJobMonitorFactory;
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
 * Observation depositing test
 * <p>
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
public class ObservationDepositingRecoveryTest
{
    @Rule
    public ExpectedException exception = ExpectedException.none();

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

    @After
    public void teardown() throws Exception
    {
        tempFolder.delete();
    }

    @Test
    public void observationRecoveryDepositablesIncludesImageCubes$FailAndGoThroughToRecovery()
    {
        Observation observation = createObservationInState(DepositState.Type.UNDEPOSITED);

        ImageCube imageCube1 = new ImageCube();
        imageCube1.setFilename("Image Cube 1");
        observation.addImageCube(imageCube1);

        ImageCube imageCube2 = new ImageCube();
        imageCube2.setFilename("Image Cube 2");
        observation.addImageCube(imageCube2);

        ObservationStateChecks.checkStateIsUndeposited(observation);
        StateChecks.checkStateIsUndeposited(imageCube1);
        StateChecks.checkStateIsUndeposited(imageCube2);

        observation.progressDeposit();
        ObservationStateChecks.checkStateIsDepositing(observation);
        StateChecks.checkStateIsUndeposited(imageCube1);
        StateChecks.checkStateIsUndeposited(imageCube2);

        // Progressing an observation in the Depositing state causes all artifacts to progress
        observation.progressDeposit();

        // set metadata file to deposited
        ChildDepositableArtefact obsMetadataFile = observation.getObservationMetadataFileDepositable();
        obsMetadataFile.setDepositState(depositStateFactory.createState(Type.DEPOSITED, obsMetadataFile));
        // Simulate failure on one image cube
        imageCube1.setDepositState(depositStateFactory.createState(DepositState.Type.FAILED, imageCube1));
        // Simulate successful deposit on the other
        imageCube2.setDepositState(depositStateFactory.createState(DepositState.Type.DEPOSITED, imageCube2));

        // Progress again - imageCube2 will be progressed to Deposited and the obs is still depositing
        ObservationStateChecks.checkStateIsDepositing(observation);
        StateChecks.checkStateIsFailed(imageCube1);
        StateChecks.checkStateIsDeposited(imageCube2);

        // Progress again - imageCube2 is still Deposited and the obs is failed (as ImageCube1 failed)
        observation.progressDeposit();

        ObservationStateChecks.checkStateIsFailed(observation);
        StateChecks.checkStateIsFailed(imageCube1);
        StateChecks.checkStateIsDeposited(imageCube2);

        // Now recover it
        observation.recoverDeposit();
        ObservationStateChecks.checkStateIsDepositing(observation);
        StateChecks.checkStateIsProcessing(imageCube1);
        StateChecks.checkStateIsDeposited(imageCube2);
    }

    @Test
    public void observationRecoveryDepositablesIncludesImageCubes$FailAndAttemptRecoveryBeforeObsFails()
    {
        Observation observation = createObservationInState(DepositState.Type.UNDEPOSITED);

        ImageCube imageCube1 = new ImageCube();
        imageCube1.setFilename("Image Cube 1");
        observation.addImageCube(imageCube1);

        ImageCube imageCube2 = new ImageCube();
        imageCube2.setFilename("Image Cube 2");
        observation.addImageCube(imageCube2);

        ObservationStateChecks.checkStateIsUndeposited(observation);
        StateChecks.checkStateIsUndeposited(imageCube1);
        StateChecks.checkStateIsUndeposited(imageCube2);

        observation.progressDeposit();
        ObservationStateChecks.checkStateIsDepositing(observation);
        StateChecks.checkStateIsUndeposited(imageCube1);
        StateChecks.checkStateIsUndeposited(imageCube2);

        // Progressing an observation in the Depositing state causes all artifacts to progress
        observation.progressDeposit();

        // set metadata file to deposited
        ChildDepositableArtefact obsMetadataFile = observation.getObservationMetadataFileDepositable();
        obsMetadataFile.setDepositState(depositStateFactory.createState(Type.DEPOSITED, obsMetadataFile));
        // Simulate failure on one image cube
        imageCube1.setDepositState(depositStateFactory.createState(DepositState.Type.FAILED, imageCube1));
        // Simulate staging on the other
        imageCube2.setDepositState(depositStateFactory.createState(DepositState.Type.STAGING, imageCube2));

        ObservationStateChecks.checkStateIsDepositing(observation);
        StateChecks.checkStateIsFailed(imageCube1);
        StateChecks.checkStateIsStaging(imageCube2);

        exception.expect(IllegalEventException.class);
        exception.expectMessage("State 'DEPOSITING' for depositable 'Observation' does not respond to event 'recover'");

        // Attempting to recover before the observation has failed will throw an exception
        observation.recoverDeposit();
    }

    private Observation createObservationInState(Type depositState)
    {
        Observation observation = new Observation(1234);
        observation.setDepositStateFactory(depositStateFactory);
        observation.setDepositState(depositStateFactory.createState(depositState, observation));
        return observation;
    }
}
