package au.csiro.casda.deposit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.hamcrest.CoreMatchers.is;

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


import static org.hamcrest.Matchers.containsString;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Level;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.web.servlet.view.RedirectView;

import au.csiro.casda.ResourceNotFoundException;
import au.csiro.casda.datadeposit.ChildDepositableArtefact;
import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositState.Type;
import au.csiro.casda.datadeposit.IntermediateDepositState;
import au.csiro.casda.deposit.exception.ArtefactInvalidStateRecoveryException;
import au.csiro.casda.deposit.exception.ObservationNotFailedRecoveryException;
import au.csiro.casda.deposit.exception.ObservationNotFoundException;
import au.csiro.casda.deposit.jpa.ObservationRepository;
import au.csiro.casda.deposit.manager.DepositManagerService;
import au.csiro.casda.deposit.services.ObservationDepositRecoveryService;
import au.csiro.casda.deposit.services.ObservationService;
import au.csiro.casda.entity.observation.Catalogue;
import au.csiro.casda.entity.observation.CatalogueType;
import au.csiro.casda.entity.observation.Cubelet;
import au.csiro.casda.entity.observation.EvaluationFile;
import au.csiro.casda.entity.observation.ImageCube;
import au.csiro.casda.entity.observation.MeasurementSet;
import au.csiro.casda.entity.observation.MomentMap;
import au.csiro.casda.entity.observation.Observation;
import au.csiro.casda.entity.observation.Project;
import au.csiro.casda.entity.observation.Spectrum;
import au.csiro.casda.entity.observation.Thumbnail;

/**
 * Tests the Observation Deposit UI Controller.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
public class ObservationDepositUiControllerTest
{
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @InjectMocks
    private ObservationDepositUiController controller;

    @Mock
    private ObservationService observationService;

    @Mock
    private ObservationDepositRecoveryService recoveryService;

    @Mock
    private ObservationRepository observationRepository;

    @Mock
    private DepositManagerService depositManagerService;

    @Mock
    private FlashHelper flashHelper;
    
    @Mock
    private HttpServletRequest httpServletRequest;

    private Log4JTestAppender testAppender;

    /**
     * Set up the ui controller before each test.
     * 
     * @throws Exception
     *             any exception thrown during set up
     */
    @Before
    public void setUp() throws Exception
    {
        testAppender = Log4JTestAppender.createAppender();
        MockitoAnnotations.initMocks(this);
        when(httpServletRequest.getRequestURL()).thenReturn(new StringBuffer("localhost:8080/hello/"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDepositStatusReturnsDepositStatusPage()
    {
        Model model = spy(new ExtendedModelMap());

        when(observationService.findObservationsByDepositStateType(any(EnumSet.class))).thenReturn(new ArrayList<>());

        String newPage = controller.observationDepositStatus(httpServletRequest, model);
        assertEquals(ObservationDepositUiController.OBSERVATION_DEPOSIT_STATUS_PAGE, newPage);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDepositStatusSetsModelKeysForNoObservations()
    {
        Model model = spy(new ExtendedModelMap());

        when(observationService.findObservationsByDepositStateType(any(EnumSet.class))).thenReturn(new ArrayList<>());
        when(observationService.findRecentlyCompletedObservations()).thenReturn(new ArrayList<>());

        String newPage = controller.observationDepositStatus(httpServletRequest, model);
        assertEquals(ObservationDepositUiController.OBSERVATION_DEPOSIT_STATUS_PAGE, newPage);

        Set<String> expectedModelKeys =
                new HashSet<>(Arrays.asList(ObservationDepositUiController.DEPOSITING_PARENT_DEPOSITABLES_MODEL_KEY,
                        ObservationDepositUiController.INVALID_OBSERVATIONS_MODEL_KEY,
                        ObservationDepositUiController.DEPOSITED_PARENT_DEPOSITABLES_MODEL_KEY,
                        ObservationDepositUiController.FAILED_PARENT_DEPOSITABLES_MODEL_KEY,
                        ObservationDepositUiController.FAILED_DEPOSITABLES_MODEL_KEY,
                        ObservationDepositUiController.DEPOSITED_PARENT_DEPOSITABLES_MAX_AGE_MODEL_KEY,
                        ObservationDepositUiController.FAILED_OBSERVATIONS_MAX_AGE_MODEL_KEY,
                        ObservationDepositUiController.DEPOSIT_STATUS_URL));
        assertEquals(expectedModelKeys, model.asMap().keySet());
        assertEquals(Arrays.asList(new Observation[0]),
                model.asMap().get(ObservationDepositUiController.FAILED_PARENT_DEPOSITABLES_MODEL_KEY));
        assertEquals(Arrays.asList(new Observation[0]),
                model.asMap().get(ObservationDepositUiController.DEPOSITING_PARENT_DEPOSITABLES_MODEL_KEY));
        assertEquals(Arrays.asList(new Observation[0]),
                model.asMap().get(ObservationDepositUiController.DEPOSITED_PARENT_DEPOSITABLES_MODEL_KEY));
        Map<Integer, List<ChildDepositableArtefact>> failureMap =
                (Map<Integer, List<ChildDepositableArtefact>>) model.asMap().get(
                        ObservationDepositUiController.FAILED_DEPOSITABLES_MODEL_KEY);
        assertTrue(failureMap.isEmpty());

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDepositStatusEndpointIncludesFailedAndFailingObservationsInFailedObservationDepositables()
    {
        Model model = spy(new ExtendedModelMap());

        Observation failedObservation = new Observation(22222);
        EvaluationFile depositable1 = spy(new EvaluationFile());
        depositable1.setFilename("file1.pdf");
        failedObservation.addEvaluationFile(depositable1);
        when(depositable1.isFailedDeposit()).thenReturn(true);
        EvaluationFile depositable2 = spy(new EvaluationFile());
        depositable2.setFilename("file2.pdf");
        when(depositable2.isFailedDeposit()).thenReturn(true);
        failedObservation.addEvaluationFile(depositable2);

        List<Observation> failedObsList = new ArrayList<>();
        failedObsList.add(failedObservation);
        when(observationService.findObservationsFailedSince(any())).thenReturn(failedObsList);

        List<Observation> activeObsList = new ArrayList<>();
        Observation activeObservation = new Observation(33333);
        activeObsList.add(activeObservation);
        Observation failingObservation = new Observation(44444);
        EvaluationFile depositable3 = spy(new EvaluationFile());
        depositable3.setFilename("file3.pdf");
        failingObservation.addEvaluationFile(depositable3);
        when(depositable3.isFailedDeposit()).thenReturn(true);
        activeObsList.add(failingObservation);
        when(
                observationService.findObservationsByDepositStateType(EnumSet.of(
                		DepositState.Type.STAGING,
                        DepositState.Type.DEPOSITING, 
                        DepositState.Type.ARCHIVING, 
                        DepositState.Type.PRIORITY_DEPOSITING, 
                        DepositState.Type.NOTIFYING))).thenReturn(activeObsList);

        when(observationService.findRecentlyCompletedObservations()).thenReturn(new ArrayList<>(0));

        controller.observationDepositStatus(httpServletRequest, model);

        Map<Integer, List<ChildDepositableArtefact>> failureMap =
                (Map<Integer, List<ChildDepositableArtefact>>) model.asMap().get(
                        ObservationDepositUiController.FAILED_DEPOSITABLES_MODEL_KEY);
        assertTrue(failureMap.keySet().contains(failingObservation.getSbid()));
        assertEquals(Arrays.asList(depositable1, depositable2), failureMap.get(failedObservation.getSbid()));
        assertTrue(failureMap.keySet().contains(failedObservation.getSbid()));
        assertEquals(Arrays.asList(depositable3), failureMap.get(failingObservation.getSbid()));
        assertEquals(2, failureMap.keySet().size());
    }

    @Test
    public void testDepositStatusEndpointHasDepositingObservationsSortedBySbidDescending()
    {
        Model model = spy(new ExtendedModelMap());

        List<Observation> depositingObservations = new ArrayList<>();

        Observation observation1 = new Observation(11111);
        Observation observation2 = new Observation(22222);
        Observation observation3 = new Observation(33333);

        // Note order
        depositingObservations.add(observation3);
        depositingObservations.add(observation1);
        depositingObservations.add(observation2);

        when(
                observationService.findObservationsByDepositStateType(EnumSet.of(
                		DepositState.Type.STAGING,
                		DepositState.Type.PRIORITY_DEPOSITING, 
                        DepositState.Type.DEPOSITING, 
                		DepositState.Type.ARCHIVING, 
                		DepositState.Type.NOTIFYING))).thenReturn(depositingObservations);

        controller.observationDepositStatus(httpServletRequest, model);

        assertEquals(Arrays.asList(new Observation[] { observation3, observation2, observation1 }),
                model.asMap().get(ObservationDepositUiController.DEPOSITING_PARENT_DEPOSITABLES_MODEL_KEY));
    }

    @Test
    public void testDepositStatusEndpointHasDepositedObservationsSortedBySbidDescending()
    {
        Model model = spy(new ExtendedModelMap());

        List<Observation> depositedObservations = new ArrayList<>();

        Observation observation1 = new Observation(11111);
        Observation observation2 = new Observation(22222);
        Observation observation3 = new Observation(33333);

        // Note order
        depositedObservations.add(observation3);
        depositedObservations.add(observation1);
        depositedObservations.add(observation2);

        when(observationService.findRecentlyCompletedObservations()).thenReturn(depositedObservations);

        controller.observationDepositStatus(httpServletRequest, model);

        assertEquals(Arrays.asList(new Observation[] { observation3, observation2, observation1 }),
                model.asMap().get(ObservationDepositUiController.DEPOSITED_PARENT_DEPOSITABLES_MODEL_KEY));
    }

    @Test
    public void testDepositStatusEndpointHasFailedObservationsSortedBySbidDescending()
    {
        Model model = spy(new ExtendedModelMap());

        List<Observation> failedObservations = new ArrayList<>();

        Observation observation1 = new Observation(11111);
        Observation observation2 = new Observation(22222);
        Observation observation3 = new Observation(33333);

        // Note order
        failedObservations.add(observation3);
        failedObservations.add(observation1);
        failedObservations.add(observation2);

        when(observationService.findObservationsFailedSince(any())).thenReturn(
                failedObservations);

        controller.observationDepositStatus(httpServletRequest, model);

        assertEquals(Arrays.asList(new Observation[] { observation3, observation2, observation1 }),
                model.asMap().get(ObservationDepositUiController.FAILED_PARENT_DEPOSITABLES_MODEL_KEY));
    }

    @Test
    public void showObservationWithUnknownObservationThrowsResourceNotFoundException() throws ResourceNotFoundException
    {
        int sbid = 1111;

        exception.expect(ResourceNotFoundException.class);
        exception.expectMessage("No observation with SBID '" + sbid + "'");

        when(observationRepository.findBySbid(anyInt())).thenReturn(null);

        Model model = spy(new ExtendedModelMap());
        controller.showObservation(model, sbid);
    }

    @Test
    public void showObservationWithKnownSbidShouldReturnShowPage() throws ResourceNotFoundException
    {
        int sbid = 1111;

        when(observationRepository.findBySbid(anyInt())).thenReturn(new Observation(sbid));

        Model model = spy(new ExtendedModelMap());
        assertEquals(ObservationDepositUiController.OBSERVATION_SHOW_PAGE, controller.showObservation(model, sbid));
    }

    @Test
    public void showObservationWithSpectraShouldSummariseSpectra() throws ResourceNotFoundException
    {
        int sbid = 1111;
        Project project = new Project("AA000"); 
        Observation obs = new Observation(sbid);
        Spectrum spectrum = new Spectrum(project);
        DepositState depositState = mock(DepositState.class);
        when(depositState.getType()).thenReturn(DepositState.Type.PROCESSING);
        spectrum.setDepositState(depositState);
        obs.addSpectra(spectrum);
        
        Thumbnail thumb = new Thumbnail();
        DepositState stateEncapsulating = mock(DepositState.class);
        when(stateEncapsulating.getType()).thenReturn(DepositState.Type.ENCAPSULATING);
        thumb.setDepositState(stateEncapsulating);
        spectrum.setThumbnail(thumb);

        MomentMap mom = new MomentMap(project);
        DateTime releasedDate = new DateTime();
        mom.setReleasedDate(releasedDate);
        DepositState stateDeposited = mock(DepositState.class);
        when(stateDeposited.getType()).thenReturn(DepositState.Type.DEPOSITED);
        mom.setDepositState(stateDeposited);
        obs.addMomentMap(mom);
        
        Cubelet cube = new Cubelet(project);
        cube.setReleasedDate(releasedDate);
        cube.setDepositState(stateDeposited);
        obs.addCubelet(cube);
        

        when(observationRepository.findBySbid(anyInt())).thenReturn(obs);

        Model model = spy(new ExtendedModelMap());
        assertEquals(ObservationDepositUiController.OBSERVATION_SHOW_PAGE, controller.showObservation(model, sbid));
        Map<String, Object> resultsMap = model.asMap();
        @SuppressWarnings("unchecked")
        Collection<DepositableStatusSummary> summaries = (Collection<DepositableStatusSummary>) resultsMap
                .get(ObservationDepositUiController.ARTEFACT_SUMMARIES_MODEL_KEY);
        for (DepositableStatusSummary statusSummary : summaries)
        {
            if (statusSummary.getDescription().equals("Spectrum"))
            {
                assertThat(statusSummary.getDescription(), is("Spectrum"));
                assertThat(statusSummary.getDepositStateDescription(), is("Processing"));
                assertThat(statusSummary.getProjectCode(), is("AA000"));
                assertThat(statusSummary.getNumArtefacts(), is(1));
            }
            else if (statusSummary.getDescription().equals("Moment Map"))
            {
                assertThat(statusSummary.getDescription(), is("Moment Map"));
                assertThat(statusSummary.getDepositStateDescription(), is("Deposited"));
                assertThat(statusSummary.getProjectCode(), is("AA000"));
                assertThat(statusSummary.getNumArtefacts(), is(1));
                assertThat(statusSummary.getReleasedDate(), is(releasedDate));
            }
            else if (statusSummary.getDescription().equals("Cubelet"))
            {
                assertThat(statusSummary.getDescription(), is("Cubelet"));
                assertThat(statusSummary.getDepositStateDescription(), is("Deposited"));
                assertThat(statusSummary.getProjectCode(), is("AA000"));
                assertThat(statusSummary.getNumArtefacts(), is(1));
                assertThat(statusSummary.getReleasedDate(), is(releasedDate));
            }
            else
            {
                assertThat(statusSummary.getDescription(), is("Thumbnail"));
                assertThat(statusSummary.getDepositStateDescription(), is("Encapsulating"));
                assertThat(statusSummary.getProjectCode(), is("N/A"));
                assertThat(statusSummary.getNumArtefacts(), is(1));
            }
        }
        assertThat(summaries.size(), is(4));
    }

    @Test
    public void showObservationWithKnownSbidShouldSetMatchingObservationInModel() throws ResourceNotFoundException
    {
        int sbid = 1111;
        Observation observation = new Observation(sbid);
        when(observationRepository.findBySbid(anyInt())).thenReturn(observation);

        Model model = spy(new ExtendedModelMap());
        controller.showObservation(model, sbid);
        assertTrue(model.asMap().keySet().contains(ObservationDepositUiController.OBSERVATION_MODEL_KEY));
        assertEquals(observation, model.asMap().get(ObservationDepositUiController.OBSERVATION_MODEL_KEY));
    }

    @Test
    public void showObservationWithKnownSbidShouldSetMatchingObservationDepositableArtefactsInModel()
            throws ResourceNotFoundException
    {
        int sbid = 1111;

        Observation observation = new Observation(sbid);

        ImageCube imageCube2 = new ImageCube();
        imageCube2.setFilename("imageCube2.fits");
        observation.addImageCube(imageCube2);

        ImageCube imageCube1 = new ImageCube();
        imageCube1.setFilename("imageCube1.fits");
        observation.addImageCube(imageCube1);

        Catalogue catalogue4 = new Catalogue(CatalogueType.CONTINUUM_ISLAND);
        catalogue4.setFilename("continuumIslandCatalogue2.xml");
        observation.addCatalogue(catalogue4);

        Catalogue catalogue1 = new Catalogue(CatalogueType.CONTINUUM_COMPONENT);
        catalogue1.setFilename("continuumComponentCatalogue1.xml");
        observation.addCatalogue(catalogue1);

        Catalogue catalogue2 = new Catalogue(CatalogueType.CONTINUUM_COMPONENT);
        catalogue2.setFilename("continuumComponentCatalogue2.xml");
        observation.addCatalogue(catalogue2);

        Catalogue catalogue3 = new Catalogue(CatalogueType.CONTINUUM_ISLAND);
        catalogue3.setFilename("continuumIslandCatalogue1.xml");
        observation.addCatalogue(catalogue3);

        MeasurementSet measurementSet2 = new MeasurementSet();
        measurementSet2.setFilename("measurementSet2.tar");
        observation.addMeasurementSet(measurementSet2);

        MeasurementSet measurementSet1 = new MeasurementSet();
        measurementSet1.setFilename("measurementSet1.tar");
        observation.addMeasurementSet(measurementSet1);

        when(observationRepository.findBySbid(anyInt())).thenReturn(observation);

        Model model = spy(new ExtendedModelMap());
        controller.showObservation(model, sbid);
        assertTrue(model.asMap().keySet()
                .contains(ObservationDepositUiController.DEPOSITABLE_ARTEFACTS_MODEL_KEY));
        assertEquals(Arrays.asList(observation.getObservationMetadataFileDepositable(), catalogue1, catalogue2,
                catalogue3, catalogue4, imageCube1, imageCube2, measurementSet1,
                measurementSet2),
                model.asMap().get(ObservationDepositUiController.DEPOSITABLE_ARTEFACTS_MODEL_KEY));
    }

    @Test
    public void recoverObservationWithUnknownObservationThrowsResourceNotFoundException()
            throws ResourceNotFoundException
    {
        int sbid = 1111;

        exception.expect(ResourceNotFoundException.class);
        exception.expectMessage("No observation with SBID '" + sbid + "'");

        when(observationRepository.findBySbid(anyInt())).thenReturn(null);

        controller.recoverDeposit(mock(HttpServletRequest.class), sbid);
    }

    @Test
    public void successfulRecoverObservationWithKnownObservationShouldRedirectToObservationShowPage()
            throws ResourceNotFoundException
    {
        int sbid = 1111;

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(observationRepository.findBySbid(anyInt())).thenReturn(new Observation(sbid));

        assertEquals(new RedirectView(ObservationDepositUiController.getPathForShowObservation(sbid)).getUrl(),
                controller.recoverDeposit(httpServletRequest, sbid).getUrl());
    }

    @Test
    public void successfulRecoverObservationWithKnownObservationShouldSetFlashMessage()
            throws ResourceNotFoundException
    {
        int sbid = 1111;

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(observationRepository.findBySbid(anyInt())).thenReturn(new Observation(sbid));

        controller.recoverDeposit(httpServletRequest, sbid);

        verify(flashHelper).flash(httpServletRequest, "success", "Deposit resumed.");
    }

    @Test
    public void successfulRecoverObservationWithKnownObservationShouldLogError() throws Exception
    {
        int sbid = 1111;

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(observationRepository.findBySbid(anyInt())).thenReturn(new Observation(sbid));

        controller.recoverDeposit(httpServletRequest, sbid);

        testAppender.verifyLogMessage(Level.INFO, "Deposit manager observation " + sbid
                + " requested to resume deposit.");
        testAppender.verifyLogMessage(Level.INFO, DepositManagerEvents.E097.messageBuilder().add(sbid).toString());
    }

    @Test
    public void unsuccessfulRecoverObservationWithKnownObservationShouldRedirectToObservationShowPage()
            throws Exception
    {
        int sbid = 1111;

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(observationRepository.findBySbid(anyInt())).thenReturn(new Observation(sbid));
        doThrow(new ObservationNotFailedRecoveryException(sbid)).when(recoveryService).recoverObservation(anyInt());

        assertEquals(new RedirectView(ObservationDepositUiController.getPathForShowObservation(sbid)).getUrl(),
                controller.recoverDeposit(httpServletRequest, sbid).getUrl());
    }

    @Test
    public void unsuccessfulRecoverObservationWithKnownObservationShouldSetFlashMessage() throws Exception
    {
        int sbid = 1111;

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(observationRepository.findBySbid(anyInt())).thenReturn(new Observation(sbid));
        doThrow(new ObservationNotFailedRecoveryException(sbid)).when(recoveryService).recoverObservation(anyInt());

        controller.recoverDeposit(httpServletRequest, sbid);

        verify(flashHelper).flash(httpServletRequest, "error", "Request to resume deposit failed.");
    }

    @Test
    public void unsuccessfulRecoverObservationWithKnownObservationShouldLogError() throws Exception
    {
        int sbid = 1111;

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(observationRepository.findBySbid(anyInt())).thenReturn(new Observation(sbid));
        doThrow(new ObservationNotFailedRecoveryException(sbid)).when(recoveryService).recoverObservation(anyInt());

        controller.recoverDeposit(httpServletRequest, sbid);

        testAppender.verifyLogMessage(Level.INFO, "Deposit manager observation " + sbid
                + " requested to resume deposit.");
        testAppender.verifyLogMessage(Level.ERROR, DepositManagerEvents.E094.messageBuilder().add(sbid).toString());
    }

    @Test
    public void recoverArtefactWithUnknownObservationLogsError() throws Exception
    {
        int sbid = 1111;
        String fileId = "some-file.xml";

        doThrow(new ObservationNotFoundException(sbid)).when(recoveryService).recoverArtefact(sbid, fileId);

        controller.recoverDeposit(mock(HttpServletRequest.class), sbid, fileId);
        
        testAppender.verifyLogMessage(Level.INFO, "Deposit manager observation " + sbid + " artefact " + fileId
                + " requested to resume deposit.");
        String message = DepositManagerEvents.E099.messageBuilder().add(sbid).toString();
        testAppender.verifyLogMessage(Level.ERROR, message);
        assertThat(message, containsString(Integer.toString(sbid)));
    }

    @Test
    public void recoverArtefactWithKnownObservationIllegalEventLogsMessage() throws Exception
    {
        int sbid = 1111;
        String fileId = "some-file.xml";

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        Observation observation = createTestObservation(sbid, fileId, true, true);
        when(observationRepository.findBySbid(anyInt())).thenReturn(observation);
        doThrow(mock(DepositState.IllegalEventException.class)).when(recoveryService).recoverArtefact(anyInt(),
                anyString());

        controller.recoverDeposit(httpServletRequest, sbid, fileId);

        testAppender.verifyLogMessage(Level.INFO, "Deposit manager observation " + sbid + " artefact " + fileId
                + " requested to resume deposit.");
        String message = DepositManagerEvents.E137.messageBuilder().add(fileId).add(sbid).toString();
        testAppender.verifyLogMessage(Level.ERROR, message);
        assertThat(message, containsString(Integer.toString(sbid)));
        assertThat(message, containsString(fileId));
    }

    @Test
    public void successfulRecoverArtefactWithKnownObservationShouldRedirectToObservationShowPage()
            throws ResourceNotFoundException
    {
        int sbid = 1111;
        String fileId = "some-file.xml";

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        Observation observation = createTestObservation(sbid, fileId, false, true);
        when(observationRepository.findBySbid(anyInt())).thenReturn(observation);

        assertEquals(new RedirectView(ObservationDepositUiController.getPathForShowObservation(sbid)).getUrl(),
                controller.recoverDeposit(httpServletRequest, sbid, fileId).getUrl());
    }

    @Test
    public void successfulRecoverArtefactWithKnownObservationShouldSetFlashMessage() throws ResourceNotFoundException
    {
        int sbid = 1111;
        String fileId = "some-file.xml";

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        Observation observation = createTestObservation(sbid, fileId, false, true);
        when(observationRepository.findBySbid(anyInt())).thenReturn(observation);

        controller.recoverDeposit(httpServletRequest, sbid, fileId);

        verify(flashHelper).flash(httpServletRequest, "success", "Deposit of artefact some-file.xml resumed.");
    }

    @Test
    public void successfulRecoverArtefactWithKnownObservationShouldLogInfo() throws Exception
    {
        int sbid = 1111;
        String fileId = "some-file.xml";

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        Observation observation = createTestObservation(sbid, fileId, false, true);
        when(observationRepository.findBySbid(anyInt())).thenReturn(observation);

        controller.recoverDeposit(httpServletRequest, sbid, fileId);

        testAppender.verifyLogMessage(Level.INFO, "Deposit manager observation " + sbid + " artefact " + fileId
                + " requested to resume deposit.");
        String message = DepositManagerEvents.E138.messageBuilder().add(fileId).add(sbid).toString();
        testAppender.verifyLogMessage(Level.INFO, message);
        assertThat(message, containsString(Integer.toString(sbid)));
        assertThat(message, containsString(fileId));
    }

    @Test
    public void unsuccessfulRecoverArtefactWithKnownObservationShouldRedirectToObservationShowPage() throws Exception
    {
        int sbid = 1111;
        String fileId = "some-file.xml";

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        Observation observation = createTestObservation(sbid, fileId, true, true);
        when(observationRepository.findBySbid(anyInt())).thenReturn(observation);
        doThrow(new ArtefactInvalidStateRecoveryException(sbid, fileId, "Observation is in a failed state")).when(
                recoveryService).recoverArtefact(anyInt(), anyString());

        assertEquals(new RedirectView(ObservationDepositUiController.getPathForShowObservation(sbid)).getUrl(),
                controller.recoverDeposit(httpServletRequest, sbid, fileId).getUrl());
    }

    @Test
    public void unsuccessfulRecoverArtefactWithKnownObservationShouldSetFlashMessage() throws Exception
    {
        int sbid = 1111;
        String fileId = "some-file.xml";

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        Observation observation = createTestObservation(sbid, fileId, true, true);
        when(observationRepository.findBySbid(anyInt())).thenReturn(observation);
        doThrow(new ArtefactInvalidStateRecoveryException(sbid, fileId, "Observation is in a failed state")).when(
                recoveryService).recoverArtefact(anyInt(), anyString());

        controller.recoverDeposit(httpServletRequest, sbid, fileId);

        verify(flashHelper).flash(httpServletRequest, "error",
                "Request to resume deposit of artefact " + fileId + " failed.");
    }

    @Test
    public void unsuccessfulRecoverArtefactWithKnownObservationShouldLogError() throws Exception
    {
        int sbid = 1111;
        String fileId = "some-file.xml";

        ArtefactInvalidStateRecoveryException exception =
                new ArtefactInvalidStateRecoveryException(sbid, fileId, "Observation is in a failed state");

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        when(observationRepository.findBySbid(anyInt())).thenReturn(new Observation(sbid));
        doThrow(exception).when(recoveryService).recoverArtefact(anyInt(), anyString());

        controller.recoverDeposit(httpServletRequest, sbid, fileId);

        testAppender.verifyLogMessage(Level.INFO, "Deposit manager observation " + sbid + " artefact " + fileId
                + " requested to resume deposit.");
        String message = DepositManagerEvents.E136.messageBuilder().add(fileId).add(sbid).toString();
        testAppender.verifyLogMessage(Level.ERROR, message, exception);
        assertThat(message, containsString(fileId));
        assertThat(message, containsString(Integer.toString(sbid)));
    }

    private Observation createTestObservation(int sbid, String fileId, boolean observationFailed, boolean artefactFailed)
    {
        Observation observation = spy(new Observation(sbid));
        doReturn(observationFailed).when(observation).isFailedDeposit();
        List<ChildDepositableArtefact> depositableArtefacts = new ArrayList<>();
        ImageCube imageCube = spy(new ImageCube());
        doReturn(fileId).when(imageCube).getFileId();
        doReturn(artefactFailed).when(imageCube).isFailedDeposit();
        depositableArtefacts.add(imageCube);
        when(observation.getDepositableArtefacts()).thenReturn(depositableArtefacts);

        return observation;
    }

    @Test
    public void testSuccessfulRedepositObservation() throws Exception
    {
        int sbid = 1111;
        String fileId = "some-file.xml";

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        Observation observation = createTestObservation(sbid, fileId, false, false);
        observation.setDepositState(new IntermediateDepositState(Type.DEPOSITED, null, observation));
        when(observationRepository.findBySbid(anyInt())).thenReturn(observation);
        when(depositManagerService.redepositObservation(anyInt())).thenReturn(true);

        controller.redeposit(httpServletRequest, sbid);

        testAppender.verifyLogMessage(Level.INFO, "Deposit manager observation " + sbid + " requested to redeposit.");
        String message = DepositManagerEvents.E153.messageBuilder().add(sbid).toString();
        testAppender.verifyLogMessage(Level.INFO, message);
        assertThat(message, containsString(Integer.toString(sbid)));

        verify(flashHelper).flash(httpServletRequest, "success", "Observation redeposit started.");

        assertEquals(new RedirectView(ObservationDepositUiController.getPathForShowObservation(sbid)).getUrl(),
                controller.recoverDeposit(httpServletRequest, sbid, fileId).getUrl());
    }

    @Test
    public void testFailedRedepositObservation() throws Exception
    {
        int sbid = 1111;
        String fileId = "some-file.xml";

        HttpServletRequest httpServletRequest = mock(HttpServletRequest.class);
        Observation observation = createTestObservation(sbid, fileId, false, false);
        observation.setDepositState(new IntermediateDepositState(Type.DEPOSITED, null, observation));
        when(observationRepository.findBySbid(anyInt())).thenReturn(observation);
        when(depositManagerService.redepositObservation(anyInt())).thenReturn(false);

        controller.redeposit(httpServletRequest, sbid);

        testAppender.verifyLogMessage(Level.INFO, "Deposit manager observation " + sbid + " requested to redeposit.");
        String message = DepositManagerEvents.E152.messageBuilder().add(sbid).toString();
        testAppender.verifyLogMessage(Level.ERROR, message);
        assertThat(message, containsString(Integer.toString(sbid)));

        verify(flashHelper).flash(httpServletRequest, "error", "Request to redeposit observation failed.");

        assertEquals(new RedirectView(ObservationDepositUiController.getPathForShowObservation(sbid)).getUrl(),
                controller.recoverDeposit(httpServletRequest, sbid, fileId).getUrl());
    }
}
