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

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositState.Type;
import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.datadeposit.Depositable;
import au.csiro.casda.deposit.CasdaToolProcessJobBuilderFactory;
import au.csiro.casda.deposit.SingleJobMonitorFactory;
import au.csiro.casda.deposit.jdbc.SimpleJdbcRepository;
import au.csiro.casda.deposit.services.NgasService;
import au.csiro.casda.deposit.services.VoToolsService;
import au.csiro.casda.entity.CasdaDepositableEntity;
import au.csiro.casda.entity.DummyObservationDepositableArtefact;
import au.csiro.casda.entity.observation.Observation;
import au.csiro.casda.jobmanager.JavaProcessJobFactory;
import au.csiro.casda.jobmanager.JobManager;

/**
 * Tests for the SimpleDepositStateFactory. Uses the DummyDepositable and DummyDepositStateFactory to exercise the
 * factory logic and state progression.
 * <p>
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
public class CasdaDepositStateFactoryTest
{
    private static class DummyDepositable extends CasdaDepositableEntity
    {

        @Override
        public boolean isDepositing()
        {
            return false;
        }

        @Override
        public String getUniqueIdentifier()
        {
            return null; // Not needed for this test
        }

    }

    private DepositStateFactory stateFactory;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private DepositState state;

    private Depositable depositable;

    @Mock
    private JobManager jobManager;

    @Mock
    private NgasService ngasService;

    @Mock
    private CasdaToolProcessJobBuilderFactory factory;

    @Mock
    private SingleJobMonitorFactory singleJobMonitorFactory;

    @Mock
    private VoToolsService voToolsService;

    @Mock
    private SimpleJdbcRepository simpleJdbcRepository;

    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
        this.stateFactory = new CasdaDepositStateFactory(ngasService, jobManager, factory, new JavaProcessJobFactory(),
                singleJobMonitorFactory, voToolsService, simpleJdbcRepository, "", "observation", "level7",
                "{\"stageCommand\"}", "SIMPLE", "stageCommandAndArgs", "{\"registerCommand\"}", "SIMPLE",
                "registerCommandAndArgs", "archiveStatus", "archivePut",
                " {\"stage_artefact\", \"1\", \"register_artefact\", \"4\" }", "");
    }

    @Test
    public void testCreateUndepositedStateForObservation()
    {
        DepositState.Type type = DepositState.Type.UNDEPOSITED;
        initialiseObservationDepositableWithState(type);
        checkStateFields(type);
    }
    
    @Test
    public void testCreatePriorityDepositingStateForObservation()
    {
        DepositState.Type type = DepositState.Type.PRIORITY_DEPOSITING;
        initialiseObservationDepositableWithState(type);
        checkStateFields(type);
    }

    @Test
    public void testCreateDepositingStateForObservation()
    {
        DepositState.Type type = DepositState.Type.DEPOSITING;
        initialiseObservationDepositableWithState(type);
        checkStateFields(type);
    }

    @Test
    public void createStateForProcessingForObservationShouldThrowException()
    {
        checkIllegalStateExceptionThrownForDepositableWithState(new Observation(), DepositState.Type.PROCESSING);
    }

    @Test
    public void createStateForProcessedForObservationShouldThrowException()
    {
        checkIllegalStateExceptionThrownForDepositableWithState(new Observation(), DepositState.Type.PROCESSED);
    }
    
    @Test
    public void createStateForEncapsulatingForObservationShouldThrowException()
    {
        checkIllegalStateExceptionThrownForDepositableWithState(new Observation(), DepositState.Type.ENCAPSULATING);
    }
    
    @Test
    public void createStateForEncapsulatedForObservationShouldThrowException()
    {
        checkIllegalStateExceptionThrownForDepositableWithState(new Observation(), DepositState.Type.ENCAPSULATED);
    }

    @Test
    public void createStateForStagingForObservationShouldThrowException()
    {
        checkIllegalStateExceptionThrownForDepositableWithState(new Observation(), DepositState.Type.STAGING);
    }

    @Test
    public void createStateForStagedForObservationShouldThrowException()
    {
        checkIllegalStateExceptionThrownForDepositableWithState(new Observation(), DepositState.Type.STAGED);
    }

    @Test
    public void createStateForRegisteringForObservationShouldThrowException()
    {
        checkIllegalStateExceptionThrownForDepositableWithState(new Observation(), DepositState.Type.REGISTERING);
    }

    @Test
    public void createStateForRegisteredForObservationShouldThrowException()
    {
        checkIllegalStateExceptionThrownForDepositableWithState(new Observation(), DepositState.Type.REGISTERED);
    }

    @Test
    public void testCreateNotifyingStateForObservation()
    {
        DepositState.Type type = DepositState.Type.NOTIFYING;
        initialiseObservationDepositableWithState(type);
        checkStateFields(type);
    }

    @Test
    public void testCreateDepositedStateForObservation()
    {
        DepositState.Type type = DepositState.Type.DEPOSITED;
        initialiseObservationDepositableWithState(type);
        checkStateFields(type);
    }

    @Test
    public void testCreateFailedStateForObservation()
    {
        DepositState.Type type = DepositState.Type.FAILED;
        initialiseObservationDepositableWithState(type);
        checkStateFields(type);
    }

    @Test
    public void testCreateUndepositedStateForDepositableArtefact()
    {
        DepositState.Type type = DepositState.Type.UNDEPOSITED;
        initialiseObservationDepositableWithState(type);
        checkStateFields(type);
    }
    
    @Test
    public void createStateForPriorityDepositingForDepositableArtefactShouldThrowException()
    {
        checkIllegalStateExceptionThrownForDepositableWithState(
                new DummyObservationDepositableArtefact(new Observation(1234), "filename.xml"),
                DepositState.Type.PRIORITY_DEPOSITING);
    }

    @Test
    public void createStateForDepositingForDepositableArtefactShouldThrowException()
    {
        checkIllegalStateExceptionThrownForDepositableWithState(
                new DummyObservationDepositableArtefact(new Observation(1234), "filename.xml"),
                DepositState.Type.DEPOSITING);
    }

    @Test
    public void testCreateProcessingStateForDepositableArtefact()
    {
        DepositState.Type type = DepositState.Type.PROCESSING;
        initialiseDepositableArtefactDepositableWithState(type);
        checkStateFields(type);
    }

    @Test
    public void testCreateProcessedStateForDepositableArtefact()
    {
        DepositState.Type type = DepositState.Type.PROCESSED;
        initialiseDepositableArtefactDepositableWithState(type);
        checkStateFields(type);
    }

    @Test
    public void testCreateEncapsulatingStateForDepositableArtefact()
    {
        DepositState.Type type = DepositState.Type.ENCAPSULATING;
        initialiseDepositableArtefactDepositableWithState(type);
        checkStateFields(type);
    }

    @Test
    public void testCreateEncapsulatedStateForDepositableArtefact()
    {
        DepositState.Type type = DepositState.Type.ENCAPSULATED;
        initialiseDepositableArtefactDepositableWithState(type);
        checkStateFields(type);
    }

    @Test
    public void testCreateStagingStateForDepositableArtefact()
    {
        DepositState.Type type = DepositState.Type.STAGING;
        initialiseDepositableArtefactDepositableWithState(type);
        checkStateFields(type);
    }

    @Test
    public void testCreateStagedStateForDepositableArtefact()
    {
        DepositState.Type type = DepositState.Type.STAGED;
        initialiseDepositableArtefactDepositableWithState(type);
        checkStateFields(type);
    }

    @Test
    public void testCreateRegisteringStateForDepositableArtefact()
    {
        DepositState.Type type = DepositState.Type.REGISTERING;
        initialiseDepositableArtefactDepositableWithState(type);
        checkStateFields(type);
    }

    @Test
    public void testCreateRegisteredStateForDepositableArtefact()
    {
        DepositState.Type type = DepositState.Type.REGISTERED;
        initialiseDepositableArtefactDepositableWithState(type);
        checkStateFields(type);
    }

    @Test
    public void createStateForNotifiyingForDepositableArtefactShouldThrowException()
    {
        checkIllegalStateExceptionThrownForDepositableWithState(
                new DummyObservationDepositableArtefact(new Observation(1234), "filename.xml"),
                DepositState.Type.NOTIFYING);
    }

    @Test
    public void testCreateDepositedStateForDepositableArtefact()
    {
        DepositState.Type type = DepositState.Type.DEPOSITED;
        initialiseDepositableArtefactDepositableWithState(type);
        checkStateFields(type);
    }

    @Test
    public void testCreateFailedStateForDepositableArtefact()
    {
        DepositState.Type type = DepositState.Type.FAILED;
        initialiseDepositableArtefactDepositableWithState(type);
        checkStateFields(type);
    }

    @Test
    public void createAnyStateForNonObservationNonDepositableArtefactShouldThrowException()
    {
        Depositable depositable = new DummyDepositable();
        for (DepositState.Type type : DepositState.Type.values())
        {
            thrown.expect(IllegalStateException.class);
            thrown.expectMessage("Illegal state type '" + type.toString() + "' for depositable 'DummyDepositable'");

            stateFactory.createState(type, depositable);
        }

    }

    private void initialiseObservationDepositableWithState(Type type)
    {
        depositable = new Observation();
        state = stateFactory.createState(type, depositable);
    }

    private void initialiseDepositableArtefactDepositableWithState(Type type)
    {
        depositable = spy(new DummyObservationDepositableArtefact(new Observation(1234), "filename.xml"));
        doReturn("uniqueid").when(depositable).getUniqueIdentifier();
        state = stateFactory.createState(type, depositable);
    }

    private void checkStateFields(Type type)
    {
        assertThat(state.getType(), sameInstance(type));
        assertThat(state.getDepositable(), sameInstance(depositable));
        assertThat(state.getDepositStateFactory(), sameInstance(stateFactory));
    }

    private void checkIllegalStateExceptionThrownForDepositableWithState(Depositable depositable, Type type)
    {
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("Illegal state type '" + type.toString() + "' for depositable '"
                + depositable.getClass().getSimpleName() + "'");

        stateFactory.createState(type, depositable);
    }

}