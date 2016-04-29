package au.csiro.casda.entity.observation;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import au.csiro.casda.datadeposit.ChildDepositableArtefact;
import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositState.Type;
import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.datadeposit.Depositable;
import au.csiro.casda.deposit.CasdaToolProcessJobBuilderFactory;
import au.csiro.casda.deposit.SingleJobMonitorFactory;
import au.csiro.casda.deposit.services.NgasService;
import au.csiro.casda.deposit.services.VoToolsService;
import au.csiro.casda.deposit.state.CasdaDepositStateFactory;
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
 * Observation depositing test
 * <p>
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
public class ObservationDepositingTest
{
    @Rule
    public ExpectedException exception = ExpectedException.none();

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

    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
        this.depositStateFactory =
                new CasdaDepositStateFactory(ngasService, jobManager, factory, new JavaProcessJobFactory(),
                        new SingleJobMonitorFactory(), voToolsService, "observation", "level7", "{\"stageCommand\"}",
                        "SIMPLE", "stageCommandAndArgs", "{\"registerCommand\"}", "SIMPLE", "registerCommandAndArgs",
                        "archiveStatus", "archivePut", " {\"stage_artefact\", \"1\", \"register_artefact\", \"4\" }");
        when(factory.createBuilder()).thenReturn(processBuilder);
        when(processBuilder.setCommand(any(String.class))).thenReturn(processBuilder);
        when(processBuilder.addCommandArgument(any(String.class), any(String.class))).thenReturn(processBuilder);
        when(processBuilder.addCommandArguments(any(String[].class))).thenReturn(processBuilder);
        when(processBuilder.addCommandSwitch(any(String.class))).thenReturn(processBuilder);
    }

    @Test
    public void observationDepositablesIncludesMetadataFile()
    {
        Observation observation = new Observation();

        Set<ChildDepositableArtefact> expectedDepositables = new HashSet<>();
        expectedDepositables.add(observation.getObservationMetadataFileDepositable());
        assertThat(observation.getDepositableArtefacts(), equalTo(expectedDepositables));
    }

    @Test
    public void observationDepositablesIncludesImageCubes()
    {
        Observation observation = new Observation();
        Set<ChildDepositableArtefact> expectedDepositables = new HashSet<>();
        expectedDepositables.add(observation.getObservationMetadataFileDepositable());

        assertThat(observation.getDepositableArtefacts(), equalTo(expectedDepositables));

        ImageCube imageCube1 = new ImageCube();
        observation.addImageCube(imageCube1);
        expectedDepositables.add(imageCube1);
        assertThat(observation.getDepositableArtefacts(), equalTo(expectedDepositables));

        ImageCube imageCube2 = new ImageCube();
        observation.addImageCube(imageCube2);
        expectedDepositables.add(imageCube2);
        assertThat(observation.getDepositableArtefacts(), equalTo(expectedDepositables));
    }

    @Test
    public void observationDepositablesIncludesCatalogues()
    {
        Observation observation = new Observation();
        Set<ChildDepositableArtefact> expectedDepositables = new HashSet<>();
        expectedDepositables.add(observation.getObservationMetadataFileDepositable());

        assertThat(observation.getDepositableArtefacts(), equalTo(expectedDepositables));

        Catalogue catalogue1 = new Catalogue();
        observation.addCatalogue(catalogue1);
        expectedDepositables.add(catalogue1);
        assertThat(observation.getDepositableArtefacts(), equalTo(expectedDepositables));

        Catalogue catalogue2 = new Catalogue();
        observation.addCatalogue(catalogue2);
        expectedDepositables.add(catalogue2);
        assertThat(observation.getDepositableArtefacts(), equalTo(expectedDepositables));
    }

    @Test
    public void observationDepositablesIncludesMeasurementSets()
    {
        Observation observation = new Observation();
        Set<ChildDepositableArtefact> expectedDepositables = new HashSet<>();
        expectedDepositables.add(observation.getObservationMetadataFileDepositable());

        assertThat(observation.getDepositableArtefacts(), equalTo(expectedDepositables));

        MeasurementSet measurementSet1 = new MeasurementSet();
        observation.addMeasurementSet(measurementSet1);
        expectedDepositables.add(measurementSet1);
        assertThat(observation.getDepositableArtefacts(), equalTo(expectedDepositables));

        MeasurementSet measurementSet2 = new MeasurementSet();
        observation.addMeasurementSet(measurementSet2);
        expectedDepositables.add(measurementSet2);
        assertThat(observation.getDepositableArtefacts(), equalTo(expectedDepositables));
    }

    @Test
    public void observationDepositablesIncludesEvaluationFiles()
    {
        Observation observation = new Observation();
        Set<ChildDepositableArtefact> expectedDepositables = new HashSet<>();
        expectedDepositables.add(observation.getObservationMetadataFileDepositable());

        assertThat(observation.getDepositableArtefacts(), equalTo(expectedDepositables));

        EvaluationFile evaluationFile1 = new EvaluationFile();
        observation.addEvaluationFile(evaluationFile1);
        expectedDepositables.add(evaluationFile1);
        assertThat(observation.getDepositableArtefacts(), equalTo(expectedDepositables));

        EvaluationFile evaluationFile2 = new EvaluationFile();
        observation.addEvaluationFile(evaluationFile2);
        expectedDepositables.add(evaluationFile2);
        assertThat(observation.getDepositableArtefacts(), equalTo(expectedDepositables));
    }

    @Test
    public void initialObservationStateIsUndeposited()
    {
        Observation observation = new Observation();
        assertThat(observation.isNewDeposit(), is(true));
        assertThat(observation.isDepositing(), is(false));
        assertThat(observation.isNotifying(), is(false));
        assertThat(observation.isDeposited(), is(false));
        assertThat(observation.isFailedDeposit(), is(false));
    }

    @Test
    public void initialObservationsDepositablesAreUndeposited()
    {
        Observation observation = new Observation();
        ImageCube imageCube = new ImageCube();
        observation.addImageCube(imageCube);
        Catalogue catalogue = new Catalogue();
        observation.addCatalogue(catalogue);
        MeasurementSet measurementSet = new MeasurementSet();
        observation.addMeasurementSet(measurementSet);
        EvaluationFile evaluationFile = new EvaluationFile();
        observation.addEvaluationFile(evaluationFile);

        for (Depositable depositable : observation.getDepositableArtefacts())
        {
            assertThat(depositable.isNewDeposit(), is(true));
            assertThat(depositable.isDepositing(), is(false));
            assertThat(observation.isNotifying(), is(false));
            assertThat(depositable.isDeposited(), is(false));
            assertThat(depositable.isFailedDeposit(), is(false));
        }
    }

    @Test
    public void progressDepositIsIgnoreWhenNoDepositStateFactorySet()
    {
        Observation observation = new Observation();

        observation.progressDeposit();
        assertThat(observation.isNewDeposit(), is(true));
        assertThat(observation.isDepositing(), is(false));
        assertThat(observation.isNotifying(), is(false));
        assertThat(observation.isDeposited(), is(false));
        assertThat(observation.isFailedDeposit(), is(false));
    }

    @Test
    public void progressDepositFromUndepositedTransitionsToDepositing()
    {
        Observation observation = createObservationInState(DepositState.Type.UNDEPOSITED);

        observation.progressDeposit();
        assertThat(observation.isNewDeposit(), is(false));
        assertThat(observation.isDepositing(), is(true));
        assertThat(observation.isNotifying(), is(false));
        assertThat(observation.isDeposited(), is(false));
        assertThat(observation.isFailedDeposit(), is(false));
        for (Depositable depositable : observation.getDepositableArtefacts())
        {
            assertThat(depositable.isNewDeposit(), is(true));
            assertThat(depositable.isDepositing(), is(false));
            assertThat(depositable.isDeposited(), is(false));
            assertThat(depositable.isFailedDeposit(), is(false));
        }
    }

    @Test
    public void progressDepositWhileDepositingProgressesDepositables()
    {
        Observation observation = createObservationInState(DepositState.Type.DEPOSITING);

        observation.progressDeposit();
        assertThat(observation.isNewDeposit(), is(false));
        assertThat(observation.isDepositing(), is(true));
        assertThat(observation.isNotifying(), is(false));
        assertThat(observation.isDeposited(), is(false));
        assertThat(observation.isFailedDeposit(), is(false));
        for (Depositable depositable : observation.getDepositableArtefacts())
        {
            assertThat(depositable.isNewDeposit(), is(false));
            assertThat(depositable.isDepositing(), is(true));
            assertThat(depositable.isDeposited(), is(false));
            assertThat(depositable.isFailedDeposit(), is(false));
        }
    }

    @Test
    public void progressDepositWhenAllDepositablesAreDepositedTransitionsToNotifying()
    {
        Observation observation = createObservationInState(DepositState.Type.DEPOSITING);

        observation.setDepositState(
                observation.getDepositStateFactory().createState(DepositState.Type.DEPOSITING, observation));
        for (Depositable depositable : observation.getDepositableArtefacts())
        {
            depositable.setDepositState(
                    observation.getDepositStateFactory().createState(DepositState.Type.DEPOSITED, depositable));
        }

        observation.progressDeposit();

        assertThat(observation.isNewDeposit(), is(false));
        assertThat(observation.isDepositing(), is(false));
        assertThat(observation.isNotifying(), is(true));
        assertThat(observation.isDeposited(), is(false));
        assertThat(observation.isFailedDeposit(), is(false));
        for (Depositable depositable : observation.getDepositableArtefacts())
        {
            assertThat(depositable.isNewDeposit(), is(false));
            assertThat(depositable.isDepositing(), is(false));
            assertThat(depositable.isDeposited(), is(true));
            assertThat(depositable.isFailedDeposit(), is(false));
        }
    }

    @Test
    public void progressDepositWhenAnyDepositableIsFailedTransitionsToFailed()
    {
        Observation observation = createObservationInState(DepositState.Type.DEPOSITING);

        for (Depositable depositableToFail : observation.getDepositableArtefacts())
        {
            observation.setDepositState(
                    observation.getDepositStateFactory().createState(DepositState.Type.DEPOSITING, observation));
            for (Depositable depositable : observation.getDepositableArtefacts())
            {
                if (depositable == depositableToFail)
                {
                    depositable.setDepositState(
                            observation.getDepositStateFactory().createState(DepositState.Type.FAILED, depositable));
                }
                else
                {
                    depositable.setDepositState(
                            observation.getDepositStateFactory().createState(DepositState.Type.DEPOSITED, depositable));
                }
            }
            depositableToFail.setDepositState(
                    observation.getDepositStateFactory().createState(DepositState.Type.FAILED, depositableToFail));

            observation.progressDeposit();

            assertThat(observation.isNewDeposit(), is(false));
            assertThat(observation.isDepositing(), is(false));
            assertThat(observation.isNotifying(), is(false));
            assertThat(observation.isDeposited(), is(false));
            assertThat(observation.isFailedDeposit(), is(true));
            for (Depositable depositable : observation.getDepositableArtefacts())
            {
                assertThat(depositable.isNewDeposit(), is(false));
                assertThat(depositable.isDepositing(), is(false));
                if (depositable == depositableToFail)
                {
                    assertThat(depositable.isDeposited(), is(false));
                    assertThat(depositable.isFailedDeposit(), is(true));
                }
                else
                {
                    assertThat(depositable.isDeposited(), is(true));
                    assertThat(depositable.isFailedDeposit(), is(false));
                }
            }
        }
    }

    @Test
    public void progressDepositWhenNotifyingTransitionsToDeposited()
    {
        Observation observation = createObservationInState(DepositState.Type.NOTIFYING);
        for (Depositable depositable : observation.getDepositableArtefacts())
        {
            depositable.setDepositState(
                    observation.getDepositStateFactory().createState(DepositState.Type.DEPOSITED, depositable));
        }

        JobStatus success = mock(JobStatus.class);
        when(jobManager.getJobStatus(any(String.class))).thenReturn(success);
        when(success.isFailed()).thenReturn(false);
        when(success.isFinished()).thenReturn(true);

        observation.progressDeposit();

        assertThat(observation.isNewDeposit(), is(false));
        assertThat(observation.isDepositing(), is(false));
        assertThat(observation.isNotifying(), is(false));
        assertThat(observation.isDeposited(), is(true));
        assertThat(observation.isFailedDeposit(), is(false));
    }

    @Test
    public void progressDepositWhenDepositedThrowsException()
    {
        Observation observation = createObservationInState(DepositState.Type.DEPOSITED);
        for (Depositable depositable : observation.getDepositableArtefacts())
        {
            depositable.setDepositState(
                    observation.getDepositStateFactory().createState(DepositState.Type.DEPOSITED, depositable));
        }
        exception.expect(DepositState.IllegalEventException.class);
        exception.expectMessage("State 'DEPOSITED' for depositable 'Observation' does not respond to event 'progress'");

        observation.progressDeposit();
    }

    @Test
    public void progressDepositWhenFailedThrowsException()
    {
        Observation observation = createObservationInState(DepositState.Type.FAILED);
        for (Depositable depositable : observation.getDepositableArtefacts())
        {
            depositable.setDepositState(
                    observation.getDepositStateFactory().createState(DepositState.Type.FAILED, depositable));
        }
        exception.expect(DepositState.IllegalEventException.class);
        exception.expectMessage("State 'FAILED' for depositable 'Observation' does not respond to event 'progress'");

        observation.progressDeposit();
    }

    private Observation createObservationInState(Type depositState)
    {
        Observation observation = new Observation(1234);
        ImageCube imageCube = new ImageCube();
        imageCube.setFilename("image_cube.xml");
        observation.addImageCube(imageCube);
        Catalogue catalogue = new Catalogue();
        catalogue.setFilename("catalogue.xml");
        observation.addCatalogue(catalogue);
        MeasurementSet measurementSet = new MeasurementSet();
        measurementSet.setFilename("measurementset.xml");
        observation.addMeasurementSet(measurementSet);
        EvaluationFile evaluationFile = new EvaluationFile();
        evaluationFile.setFilename("evaluation.xml");
        observation.addEvaluationFile(evaluationFile);
        observation.setDepositStateFactory(depositStateFactory);
        observation.setDepositState(depositStateFactory.createState(depositState, observation));
        return observation;
    }
}
