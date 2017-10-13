package au.csiro.casda.deposit.manager;

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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.lang3.RandomUtils;
import org.apache.logging.log4j.Level;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;

import au.csiro.casda.datadeposit.ChildDepositableArtefact;
import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositedDepositState;
import au.csiro.casda.datadeposit.UndepositedDepositState;
import au.csiro.casda.deposit.DepositManagerEvents;
import au.csiro.casda.deposit.Log4JTestAppender;
import au.csiro.casda.deposit.jpa.ObservationRepository;
import au.csiro.casda.entity.CasdaDepositableArtefactEntity;
import au.csiro.casda.entity.observation.ImageCube;
import au.csiro.casda.entity.observation.Observation;


/**
 * 
 * Test the concrete implementation of the casda state change listener/logger on the deposit manager side
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public class CasdaDepositLoggingTest
{
    private Log4JTestAppender testAppender;
    
    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        testAppender = Log4JTestAppender.createAppender();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testObservationDepositTimeoutLogging()
    {
        Integer sbid = RandomUtils.nextInt(11111, 99999);
        Observation observation = mock(Observation.class);
        when(observation.getSbid()).thenReturn(sbid);
        List<ChildDepositableArtefact> depositableArtefacts = new ArrayList<>();
        when(observation.getDepositableArtefacts()).thenReturn(depositableArtefacts);

        CasdaDepositableArtefactEntity artefact = createMockArtefact(new UndepositedDepositState(null, null));
        depositableArtefacts.add(artefact);

        List<Observation> depositingObservations = new ArrayList<>();
        depositingObservations.add(observation);
        ObservationRepository obsRepo = mock(ObservationRepository.class);
        when(obsRepo.findDepositingObservationsForDepositStateTypeOrdered
                (any(EnumSet.class))).thenReturn(depositingObservations);

        DateTime now = DateTime.now(DateTimeZone.UTC);

        when(observation.getDepositStarted()).thenReturn(now);
        when(artefact.getDepositStateChanged()).thenReturn(now);

        int observationTimeout = RandomUtils.nextInt(10000, 20000);
        int artefactTimeout = Integer.MAX_VALUE;
        CasdaDepositStatusProgressMonitor casdaDepositStatusPoller =
                new CasdaDepositStatusProgressMonitor(observationTimeout, artefactTimeout, obsRepo);

        // Before
        casdaDepositStatusPoller.checkDepositableStatuses(now.getMillis() + observationTimeout - 1);
        testAppender.verifyNoMessages();

        // On
        casdaDepositStatusPoller.checkDepositableStatuses(now.getMillis() + observationTimeout);
        testAppender.verifyNoMessages();

        // After
        casdaDepositStatusPoller.checkDepositableStatuses(now.getMillis() + observationTimeout + 1);
        testAppender.verifyLogMessage(Level.ERROR, DepositManagerEvents.E076.messageBuilder().add(sbid).toString());
    }

    @Test
    public void testObservationRedepositTimeoutLogging()
    {
        Integer sbid = RandomUtils.nextInt(11111, 99999);
        Observation observation = mock(Observation.class);
        when(observation.getSbid()).thenReturn(sbid);
        List<ChildDepositableArtefact> depositableArtefacts = new ArrayList<>();
        when(observation.getDepositableArtefacts()).thenReturn(depositableArtefacts);

        CasdaDepositableArtefactEntity artefact = createMockArtefact(new UndepositedDepositState(null, null));
        depositableArtefacts.add(artefact);

        List<Observation> depositingObservations = new ArrayList<>();
        depositingObservations.add(observation);
        ObservationRepository obsRepo = mock(ObservationRepository.class);
        when(obsRepo.findDepositingObservationsForDepositStateTypeOrdered
                (any(EnumSet.class))).thenReturn(depositingObservations);

        DateTime now = DateTime.now(DateTimeZone.UTC);
        DateTime yesterday = now.minus(3600*24);

        when(observation.getDepositStarted()).thenReturn(yesterday);
        when(observation.getRedepositStarted()).thenReturn(now);
        when(artefact.getDepositStateChanged()).thenReturn(now);

        int observationTimeout = RandomUtils.nextInt(10000, 20000);
        int artefactTimeout = Integer.MAX_VALUE;
        CasdaDepositStatusProgressMonitor casdaDepositStatusPoller =
                new CasdaDepositStatusProgressMonitor(observationTimeout, artefactTimeout, obsRepo);

        // Before
        casdaDepositStatusPoller.checkDepositableStatuses(now.getMillis() + observationTimeout - 1);
        testAppender.verifyNoMessages();

        // On
        casdaDepositStatusPoller.checkDepositableStatuses(now.getMillis() + observationTimeout);
        testAppender.verifyNoMessages();

        // After
        casdaDepositStatusPoller.checkDepositableStatuses(now.getMillis() + observationTimeout + 1);
        testAppender.verifyLogMessage(Level.ERROR, DepositManagerEvents.E076.messageBuilder().add(sbid).toString());
    }

    @Test
    public void testPollingArtifactTimeoutLogging()
    {
        Integer sbid = RandomUtils.nextInt(11111, 99999);
        Observation observation = mock(Observation.class);
        when(observation.getSbid()).thenReturn(sbid);
        List<ChildDepositableArtefact> depositableArtefacts = new ArrayList<>();
        when(observation.getDepositableArtefacts()).thenReturn(depositableArtefacts);

        CasdaDepositableArtefactEntity artefact = createMockArtefact(new UndepositedDepositState(null, null));
        depositableArtefacts.add(artefact);
        ImageCube finishedArtefact = new ImageCube();
        finishedArtefact.setDepositState(new DepositedDepositState(null, finishedArtefact));
        depositableArtefacts.add(finishedArtefact);

        List<Observation> depositingObservations = new ArrayList<>();
        depositingObservations.add(observation);
        ObservationRepository obsRepo = mock(ObservationRepository.class);
        when(obsRepo.findDepositingObservationsForDepositStateTypeOrdered
                (any(EnumSet.class))).thenReturn(depositingObservations);

        DateTime now = DateTime.now(DateTimeZone.UTC);

        when(observation.getDepositStarted()).thenReturn(now);
        when(artefact.getDepositStateChanged()).thenReturn(now);

        int observationTimeout = Integer.MAX_VALUE;
        int artefactTimeout = RandomUtils.nextInt(10000, 20000);
        CasdaDepositStatusProgressMonitor casdaDepositStatusPoller =
                new CasdaDepositStatusProgressMonitor(observationTimeout, artefactTimeout, obsRepo);

        // Before
        casdaDepositStatusPoller.checkDepositableStatuses(now.getMillis() + artefactTimeout - 1);
        testAppender.verifyNoMessages();

        // On
        casdaDepositStatusPoller.checkDepositableStatuses(now.getMillis() + artefactTimeout);
        testAppender.verifyNoMessages();

        // After
        casdaDepositStatusPoller.checkDepositableStatuses(now.getMillis() + artefactTimeout + 1);
        testAppender.verifyLogMessage(Level.ERROR,
                DepositManagerEvents.E074.messageBuilder().add(artefact.getFilename()).add(sbid).toString());
        // Only the undeposited artefact should have been reported
        testAppender.verifyNoMessages();
    }

    private CasdaDepositableArtefactEntity createMockArtefact(DepositState depositState)
    {
        CasdaDepositableArtefactEntity artefact = mock(CasdaDepositableArtefactEntity.class);
        when(artefact.getDepositState()).thenReturn(depositState);
        return artefact;
    }
}
