package au.csiro.casda.deposit.state;

import static org.hamcrest.text.MatchesPattern.matchesPattern;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.logging.log4j.Level;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositState.Type;
import au.csiro.casda.datadeposit.DepositableArtefact;
import au.csiro.casda.deposit.DepositManagerEvents;
import au.csiro.casda.deposit.Log4JTestAppender;
import au.csiro.casda.deposit.services.NgasService;
import au.csiro.casda.deposit.services.NgasService.ServiceCallException;
import au.csiro.casda.entity.CasdaDepositableArtefactEntity;
import au.csiro.casda.entity.observation.Catalogue;
import au.csiro.casda.entity.observation.Observation;

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
 * Test the concrete implementation of the casda state change listener/logger
 * <p>
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
public class CasdaObservationDepositLoggingTest
{
    private Log4JTestAppender testAppender;

    @Mock
    private NgasService ngasService;

    private CasdaObservationDepositStateChangeListener depositStateChangeListener;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        testAppender = Log4JTestAppender.createAppender();
        MockitoAnnotations.initMocks(this);
        depositStateChangeListener = new CasdaObservationDepositStateChangeListener(ngasService);
    }

    private CasdaDepositableArtefactEntity createDepositableArtefactWithObservationParentInState(Type depositStateType)
    {
        Catalogue depositableArtefact = new Catalogue();
        depositableArtefact.setFilename(RandomStringUtils.random(20));

        DepositState depositState = mock(DepositState.class);
        when(depositState.getType()).thenReturn(depositStateType);

        depositableArtefact.setDepositState(depositState);

        Observation observation = createObservationDepositableWithState(depositStateType);
        observation.getCatalogues().add(depositableArtefact);
        depositableArtefact.setParent(observation);
        return depositableArtefact;
    }

    private Observation createObservationDepositableWithState(Type depositStateType)
    {
        Observation observation = new Observation();
        observation.setSbid(12345);

        DepositState depositState = mock(DepositState.class);
        when(depositState.getType()).thenReturn(depositStateType);
        observation.setDepositState(depositState);
        for (DepositableArtefact depositableArtefact : observation.getDepositableArtefacts())
        {
            depositableArtefact.setDepositState(depositState);
        }

        return observation;
    }

    @Test
    public void testProgressObservation()
    {
        Type initialDepositStateType = DepositState.Type.PROCESSING;
        Observation observation = createObservationDepositableWithState(initialDepositStateType);

        DepositState.Type nextDepositStateType = DepositState.Type.PROCESSED;
        DepositState nextDepositState = mock(DepositState.class);
        when(nextDepositState.getType()).thenReturn(nextDepositStateType);

        observation.setDepositStateChangeListener(depositStateChangeListener);
        observation.setDepositState(nextDepositState);

        String logMessage = DepositManagerEvents.E075.messageBuilder().add(((Observation) observation).getSbid())
                .add(nextDepositStateType.name()).toString();
        testAppender.verifyLogMessage(Level.INFO, Matchers.allOf(containsString(logMessage),
                matchesPattern(".*\\[fileId: observations-" + observation.getSbid() + "\\].*")),
                sameInstance((Throwable) null));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDepositedObservation() throws ServiceCallException
    {
        long fileSizeInKbytes = RandomUtils.nextLong(1, 1000000);
        NgasService.Status ngasStatus = mock(NgasService.Status.class);
        doReturn(ngasStatus).when(ngasService).getStatus(Mockito.anyString());
        doReturn(fileSizeInKbytes * FileUtils.ONE_KB).when(ngasStatus).getUncompressedFileSizeBytes();

        Type initialDepositStateType = DepositState.Type.DEPOSITING;
        Observation observation = createObservationDepositableWithState(initialDepositStateType);

        DepositState.Type nextDepositStateType = DepositState.Type.DEPOSITED;
        DepositState nextDepositState = mock(DepositState.class);
        when(nextDepositState.getType()).thenReturn(nextDepositStateType);

        for (DepositableArtefact depositableArtefact : observation.getDepositableArtefacts())
        {
            depositableArtefact.setDepositStateChangeListener(depositStateChangeListener);
            depositableArtefact.setDepositState(nextDepositState);
        }
        observation.setDepositStateChangeListener(depositStateChangeListener);
        observation.setDepositState(nextDepositState);

        String logMessage;
        logMessage =
                DepositManagerEvents.E050.messageBuilder().add("observation.xml").add(observation.getSbid())
                        .add(nextDepositStateType.name()).toString();
        testAppender.verifyLogMessage(Level.INFO, Matchers.allOf(containsString(logMessage),
                matchesPattern(".*\\[startTime: .*\\].*"), matchesPattern(".*\\[endTime: .*\\].*"),
                matchesPattern(".*\\[source: RTC\\].*"), matchesPattern(".*\\[destination: ARCHIVE\\].*"),
                matchesPattern(".*\\[volumeKB: " + fileSizeInKbytes + "\\].*"),
                matchesPattern(".*\\[fileId: observations-" + observation.getSbid() + "-observation.xml\\].*")),
                sameInstance((Throwable) null));
        testAppender.verifyLogMessage(Level.INFO, DepositManagerEvents.E075.messageBuilder().add(observation.getSbid())
                .add(nextDepositStateType.name()).toString());
        logMessage = DepositManagerEvents.E077.messageBuilder().add(observation.getSbid()).toString();
        testAppender.verifyLogMessage(Level.INFO, Matchers.allOf(containsString(logMessage),
                matchesPattern(".*\\[startTime: .*\\].*"), matchesPattern(".*\\[endTime: .*\\].*"),
                matchesPattern(".*\\[source: RTC\\].*"), matchesPattern(".*\\[destination: ARCHIVE\\].*"),
                matchesPattern(".*\\[volumeKB: " + fileSizeInKbytes + "\\].*"),
                matchesPattern(".*\\[fileId: observations-" + observation.getSbid() + "\\].*")),
                sameInstance((Throwable) null));
    }

    @Test
    public void testFailedObservation()
    {
        Type initialDepositStateType = DepositState.Type.DEPOSITING;
        Observation observation = createObservationDepositableWithState(initialDepositStateType);

        DepositState.Type nextDepositStateType = DepositState.Type.FAILED;
        DepositState nextDepositState = mock(DepositState.class);
        when(nextDepositState.getType()).thenReturn(nextDepositStateType);

        observation.setDepositStateChangeListener(depositStateChangeListener);
        observation.setDepositState(nextDepositState);

        String logMessage = DepositManagerEvents.E070.messageBuilder().add(((Observation) observation).getSbid()).toString();
        testAppender.verifyLogMessage(Level.ERROR, Matchers.allOf(containsString(logMessage),
                matchesPattern(".*\\[fileId: observations-" + observation.getSbid() + "\\].*")),
                sameInstance((Throwable) null));
    }

    @Test
    public void testProgressArtifact()
    {
        Type initialDepositStateType = DepositState.Type.UNDEPOSITED;
        CasdaDepositableArtefactEntity artefact =
                createDepositableArtefactWithObservationParentInState(initialDepositStateType);

        DepositState.Type nextDepositStateType = DepositState.Type.PROCESSING;
        DepositState nextDepositState = mock(DepositState.class);
        when(nextDepositState.getType()).thenReturn(nextDepositStateType);

        artefact.setDepositStateChangeListener(depositStateChangeListener);
        artefact.setDepositState(nextDepositState);

        String logMessage = DepositManagerEvents.E050.messageBuilder()
                .add(artefact.getFilename()).add(artefact.getParent().getUniqueId()).add(nextDepositStateType.name())
                .toString();
        testAppender.verifyLogMessage(Level.INFO, Matchers.allOf(containsString(logMessage),
                matchesPattern(".*\\[fileId: "  + artefact.getFileId() + "\\].*")),
                sameInstance((Throwable) null));
    }

    @Test
    public void testFailedArtifact()
    {
        Type initialDepositStateType = DepositState.Type.PROCESSING;
        CasdaDepositableArtefactEntity artefact =
                createDepositableArtefactWithObservationParentInState(initialDepositStateType);

        DepositState.Type nextDepositStateType = DepositState.Type.FAILED;
        DepositState nextDepositState = mock(DepositState.class);
        when(nextDepositState.getType()).thenReturn(nextDepositStateType);

        artefact.setDepositStateChangeListener(depositStateChangeListener);
        artefact.setDepositState(nextDepositState);

        String logMessage = DepositManagerEvents.E052.messageBuilder().add(artefact.getFilename())
                .add(artefact.getParent().getUniqueId()).add(initialDepositStateType.name()).toString();
        testAppender.verifyLogMessage(Level.ERROR, Matchers.allOf(containsString(logMessage),
                matchesPattern(".*\\[fileId: "  + artefact.getFileId() + "\\].*")),
                sameInstance((Throwable) null));
    }

}
