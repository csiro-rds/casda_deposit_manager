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
import au.csiro.casda.deposit.DepositManagerEvents;
import au.csiro.casda.deposit.Log4JTestAppender;
import au.csiro.casda.deposit.services.NgasService;
import au.csiro.casda.deposit.services.NgasService.ServiceCallException;
import au.csiro.casda.entity.CasdaDepositableArtefactEntity;
import au.csiro.casda.entity.observation.Catalogue;
import au.csiro.casda.entity.observation.CatalogueType;
import au.csiro.casda.entity.observation.Level7Collection;
import au.csiro.casda.entity.observation.Project;

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
public class CasdaLevel7DepositLoggingTest
{
    private Log4JTestAppender testAppender;

    @Mock
    private NgasService ngasService;

    private CasdaLevel7DepositStateChangeListener depositStateChangeListener;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        testAppender = Log4JTestAppender.createAppender();
        MockitoAnnotations.initMocks(this);
        depositStateChangeListener = new CasdaLevel7DepositStateChangeListener(ngasService);
    }

    private CasdaDepositableArtefactEntity createDepositableArtefactWithLevel7ParentInState(Type depositStateType,
            String filename)
    {
        Catalogue depositableArtefact = new Catalogue(CatalogueType.LEVEL7);
        depositableArtefact.setFilename(filename);

        DepositState depositState = mock(DepositState.class);
        when(depositState.getType()).thenReturn(depositStateType);

        depositableArtefact.setDepositState(depositState);

        Level7Collection level7Collection = createLevel7CollectionWithState(depositStateType);
        level7Collection.getCatalogues().add(depositableArtefact);
        depositableArtefact.setParent(level7Collection);
        return depositableArtefact;
    }

    private Level7Collection createLevel7CollectionWithState(Type depositStateType)
    {
        Level7Collection level7Collection = new Level7Collection(54321);
        level7Collection.setProject(new Project("ABC123"));

        DepositState depositState = mock(DepositState.class);
        when(depositState.getType()).thenReturn(depositStateType);
        level7Collection.setDepositState(depositState);

        return level7Collection;
    }

    @Test
    public void testProgressLevel7Collection()
    {
        Type initialDepositStateType = DepositState.Type.PROCESSING;
        Level7Collection level7Collection = createLevel7CollectionWithState(initialDepositStateType);

        DepositState.Type nextDepositStateType = DepositState.Type.PROCESSED;
        DepositState nextDepositState = mock(DepositState.class);
        when(nextDepositState.getType()).thenReturn(nextDepositStateType);

        level7Collection.setDepositStateChangeListener(depositStateChangeListener);
        level7Collection.setDepositState(nextDepositState);

        String logMessage =
                DepositManagerEvents.E119.messageBuilder().add(((Level7Collection) level7Collection).getUniqueId())
                        .add(nextDepositStateType.name())
                        .addFileId(((Level7Collection) level7Collection).getUniqueIdentifier().replace("/", "-"))
                        .addCustomMessage("Project code: " + level7Collection.getProject().getOpalCode()).toString();
        testAppender.verifyLogMessage(Level.INFO, logMessage);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDepositedLevel7Collection() throws ServiceCallException
    {
        long fileSizeInKbytes = RandomUtils.nextLong(1, 1000000);
        NgasService.Status ngasStatus = mock(NgasService.Status.class);
        doReturn(ngasStatus).when(ngasService).getStatus(Mockito.anyString());
        doReturn(fileSizeInKbytes * FileUtils.ONE_KB).when(ngasStatus).getUncompressedFileSizeBytes();

        Type initialDepositStateType = DepositState.Type.DEPOSITING;
        CasdaDepositableArtefactEntity catalogue =
                createDepositableArtefactWithLevel7ParentInState(initialDepositStateType, RandomStringUtils.random(10));
        Level7Collection level7Collection = (Level7Collection) catalogue.getParent();

        DepositState.Type nextDepositStateType = DepositState.Type.DEPOSITED;
        DepositState nextDepositState = mock(DepositState.class);
        when(nextDepositState.getType()).thenReturn(nextDepositStateType);

        catalogue.setDepositStateChangeListener(depositStateChangeListener);
        catalogue.setDepositState(nextDepositState);
        level7Collection.setDepositStateChangeListener(depositStateChangeListener);
        level7Collection.setDepositState(nextDepositState);

        String logMessage;
        logMessage =
                DepositManagerEvents.E120.messageBuilder().add(catalogue.getFilename())
                        .add(catalogue.getParent().getUniqueId()).add(nextDepositStateType.name()).toString();
        testAppender.verifyLogMessage(Level.INFO, Matchers.allOf(containsString(logMessage),
                matchesPattern(".*\\[startTime: .*\\].*"), matchesPattern(".*\\[endTime: .*\\].*"),
                matchesPattern(".*\\[source: RTC\\].*"), matchesPattern(".*\\[destination: ARCHIVE\\].*"),
                matchesPattern(".*\\[volumeKB: " + fileSizeInKbytes + "\\].*"), matchesPattern(".*\\[fileId: level7-"
                        + level7Collection.getDapCollectionId() + "-catalogues-null\\].*")),
                sameInstance((Throwable) null));
        logMessage =
                DepositManagerEvents.E119.messageBuilder().add(((Level7Collection) level7Collection).getUniqueId())
                        .add(nextDepositStateType.name())
                        .addFileId(((Level7Collection) level7Collection).getUniqueIdentifier().replace("/", "-"))
                        .addCustomMessage("Project code: " + level7Collection.getProject().getOpalCode()).toString();
        testAppender.verifyLogMessage(Level.INFO, logMessage);
        logMessage =
                DepositManagerEvents.E115.messageBuilder().add(((Level7Collection) level7Collection).getUniqueId())
                        .toString();
        testAppender.verifyLogMessage(Level.INFO, Matchers.allOf(containsString(logMessage),
                matchesPattern(".*\\[startTime: .*\\].*"), matchesPattern(".*\\[endTime: .*\\].*"),
                matchesPattern(".*\\[source: RTC\\].*"), matchesPattern(".*\\[destination: ARCHIVE\\].*"),
                matchesPattern(".*\\[volumeKB: " + fileSizeInKbytes + "\\].*"), matchesPattern(".*\\[fileId: level7-"
                        + level7Collection.getDapCollectionId() + "\\].*")), sameInstance((Throwable) null));
    }

    @Test
    public void testFailedLevel7Collection()
    {
        Type initialDepositStateType = DepositState.Type.DEPOSITING;
        Level7Collection level7Collection = createLevel7CollectionWithState(initialDepositStateType);

        DepositState.Type nextDepositStateType = DepositState.Type.FAILED;
        DepositState nextDepositState = mock(DepositState.class);
        when(nextDepositState.getType()).thenReturn(nextDepositStateType);

        level7Collection.setDepositStateChangeListener(depositStateChangeListener);
        level7Collection.setDepositState(nextDepositState);

        String logMessage =
                DepositManagerEvents.E114.messageBuilder().add(((Level7Collection) level7Collection).getUniqueId())
                        .addFileId(((Level7Collection) level7Collection).getUniqueIdentifier().replace("/", "-"))
                        .addCustomMessage("Project code: " + level7Collection.getProject().getOpalCode()).toString();
        testAppender.verifyLogMessage(Level.ERROR, logMessage);
    }

    @Test
    public void testProgressLevel7Artifact()
    {
        Type initialDepositStateType = DepositState.Type.UNDEPOSITED;
        CasdaDepositableArtefactEntity artefact =
                createDepositableArtefactWithLevel7ParentInState(initialDepositStateType, RandomStringUtils.random(10));

        DepositState.Type nextDepositStateType = DepositState.Type.PROCESSING;
        DepositState nextDepositState = mock(DepositState.class);
        when(nextDepositState.getType()).thenReturn(nextDepositStateType);

        artefact.setDepositStateChangeListener(depositStateChangeListener);
        artefact.setDepositState(nextDepositState);

        String logMessage =
                DepositManagerEvents.E120.messageBuilder().add(artefact.getFilename())
                        .add(artefact.getParent().getUniqueId()).add(nextDepositStateType.name()).toString();
        testAppender.verifyLogMessage(
                Level.INFO,
                Matchers.allOf(containsString(logMessage), matchesPattern(".*\\[fileId: " + artefact.getFileId()
                        + "\\].*")), sameInstance((Throwable) null));
    }

    @Test
    public void testFailedLevel7Artifact()
    {
        Type initialDepositStateType = DepositState.Type.PROCESSING;
        CasdaDepositableArtefactEntity artefact =
                createDepositableArtefactWithLevel7ParentInState(initialDepositStateType, RandomStringUtils.random(10));

        DepositState.Type nextDepositStateType = DepositState.Type.FAILED;
        DepositState nextDepositState = mock(DepositState.class);
        when(nextDepositState.getType()).thenReturn(nextDepositStateType);

        artefact.setDepositStateChangeListener(depositStateChangeListener);
        artefact.setDepositState(nextDepositState);

        String logMessage =
                DepositManagerEvents.E121.messageBuilder().add(artefact.getFilename())
                        .add(artefact.getParent().getUniqueId()).add(initialDepositStateType.name()).toString();
        testAppender.verifyLogMessage(
                Level.ERROR,
                Matchers.allOf(containsString(logMessage), matchesPattern(".*\\[fileId: " + artefact.getFileId()
                        + "\\].*")), sameInstance((Throwable) null));
    }

}
