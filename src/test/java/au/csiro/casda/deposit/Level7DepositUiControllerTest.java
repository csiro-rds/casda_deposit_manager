package au.csiro.casda.deposit;

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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import au.csiro.casda.ResourceNotFoundException;
import au.csiro.casda.datadeposit.ChildDepositableArtefact;
import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.deposit.jpa.Level7CollectionRepository;
import au.csiro.casda.deposit.services.Level7CollectionService;
import au.csiro.casda.entity.observation.Catalogue;
import au.csiro.casda.entity.observation.CatalogueType;
import au.csiro.casda.entity.observation.Level7Collection;

/**
 * Tests the Level7Collection Deposit UI Controller.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
public class Level7DepositUiControllerTest
{
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @InjectMocks
    private Level7DepositUiController controller;

    @Mock
    private Level7CollectionService level7CollectionService;

    @Mock
    private Level7CollectionRepository level7CollectionRepository;

    @Mock
    private FlashHelper flashHelper;

    /**
     * Set up the ui controller before each test.
     * 
     * @throws Exception
     *             any exception thrown during set up
     */
    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDepositStatusReturnsDepositStatusPage()
    {
        Model model = spy(new ExtendedModelMap());

        when(level7CollectionService.findLevel7CollectionsByDepositStateType(any(EnumSet.class))).thenReturn(
                new ArrayList<>());

        String newPage = controller.level7CollectionDepositStatus(model);
        assertEquals(Level7DepositUiController.LEVEL7_DEPOSIT_STATUS_PAGE, newPage);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDepositStatusSetsModelKeysForNoLevel7Collections()
    {
        Model model = spy(new ExtendedModelMap());

        when(level7CollectionService.findLevel7CollectionsByDepositStateType(any(EnumSet.class))).thenReturn(
                new ArrayList<>());
        when(level7CollectionService.findRecentlyCompletedLevel7Collections()).thenReturn(new ArrayList<>());

        String newPage = controller.level7CollectionDepositStatus(model);
        assertEquals(Level7DepositUiController.LEVEL7_DEPOSIT_STATUS_PAGE, newPage);

        Set<String> expectedModelKeys =
                new HashSet<>(Arrays.asList(Level7DepositUiController.DEPOSITING_LEVEL7_COLLECTIONS_MODEL_KEY,
                        Level7DepositUiController.DEPOSITED_LEVEL7_COLLECTIONS_MODEL_KEY,
                        Level7DepositUiController.FAILED_LEVEL7_COLLECTIONS_MODEL_KEY,
                        Level7DepositUiController.FAILED_LEVEL7_COLLECTION_DEPOSITABLES_MODEL_KEY,
                        Level7DepositUiController.DEPOSITED_LEVEL7_COLLECTIONS_MAX_AGE_MODEL_KEY));
        assertEquals(expectedModelKeys, model.asMap().keySet());
        assertEquals(Arrays.asList(new Level7Collection[0]),
                model.asMap().get(Level7DepositUiController.FAILED_LEVEL7_COLLECTIONS_MODEL_KEY));
        assertEquals(Arrays.asList(new Level7Collection[0]),
                model.asMap().get(Level7DepositUiController.DEPOSITING_LEVEL7_COLLECTIONS_MODEL_KEY));
        assertEquals(Arrays.asList(new Level7Collection[0]),
                model.asMap().get(Level7DepositUiController.DEPOSITED_LEVEL7_COLLECTIONS_MODEL_KEY));
        Map<Integer, List<ChildDepositableArtefact>> failureMap =
                (Map<Integer, List<ChildDepositableArtefact>>) model.asMap().get(
                        Level7DepositUiController.FAILED_LEVEL7_COLLECTION_DEPOSITABLES_MODEL_KEY);
        assertTrue(failureMap.isEmpty());

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDepositStatusEndpointIncludesFailedAndFailingLevel7CollectionsInFailedLevel7CollectionDepositables()
    {
        Model model = spy(new ExtendedModelMap());

        Level7Collection failedLevel7Collection = new Level7Collection(22222);
        Catalogue depositable1 = spy(new Catalogue(CatalogueType.LEVEL7));
        depositable1.setFilename("file1.pdf");
        failedLevel7Collection.addCatalogue(depositable1);
        when(depositable1.isFailedDeposit()).thenReturn(true);
        Catalogue depositable2 = spy(new Catalogue(CatalogueType.LEVEL7));
        depositable2.setFilename("file2.pdf");
        when(depositable2.isFailedDeposit()).thenReturn(true);
        failedLevel7Collection.addCatalogue(depositable2);

        List<Level7Collection> failedLevel7List = new ArrayList<>();
        failedLevel7List.add(failedLevel7Collection);
        when(level7CollectionService.findLevel7CollectionsByDepositStateType(DepositState.Type.FAILED)).thenReturn(
                failedLevel7List);

        List<Level7Collection> activeLevel7List = new ArrayList<>();
        Level7Collection activeLevel7Collection = new Level7Collection(33333);
        activeLevel7List.add(activeLevel7Collection);
        Level7Collection failingLevel7Collection = new Level7Collection(44444);
        Catalogue depositable3 = spy(new Catalogue(CatalogueType.LEVEL7));
        depositable3.setFilename("file3.pdf");
        failingLevel7Collection.addCatalogue(depositable3);
        when(depositable3.isFailedDeposit()).thenReturn(true);
        activeLevel7List.add(failingLevel7Collection);
        when(
                level7CollectionService.findLevel7CollectionsByDepositStateType(EnumSet.of(DepositState.Type.STAGING,
                        DepositState.Type.DEPOSITING, DepositState.Type.NOTIFYING))).thenReturn(activeLevel7List);

        when(level7CollectionService.findRecentlyCompletedLevel7Collections()).thenReturn(new ArrayList<>(0));

        controller.level7CollectionDepositStatus(model);

        Map<Integer, List<ChildDepositableArtefact>> failureMap =
                (Map<Integer, List<ChildDepositableArtefact>>) model.asMap().get(
                        Level7DepositUiController.FAILED_LEVEL7_COLLECTION_DEPOSITABLES_MODEL_KEY);
        assertEquals(2, failureMap.keySet().size());
        assertTrue(failureMap.keySet().contains(failingLevel7Collection.getDapCollectionId()));
        assertEquals(Arrays.asList(depositable1, depositable2),
                failureMap.get(failedLevel7Collection.getDapCollectionId()));
        assertTrue(failureMap.keySet().contains(failedLevel7Collection.getDapCollectionId()));
        assertEquals(Arrays.asList(depositable3), failureMap.get(failingLevel7Collection.getDapCollectionId()));
    }

    @Test
    public void testDepositStatusEndpointHasDepositingLevel7CollectionsSortedByCollectionIdDescending()
    {
        Model model = spy(new ExtendedModelMap());

        List<Level7Collection> depositingLevel7Collections = new ArrayList<>();

        Level7Collection level7Collection1 = new Level7Collection(11111);
        Level7Collection level7Collection2 = new Level7Collection(22222);
        Level7Collection level7Collection3 = new Level7Collection(33333);

        // Note order
        depositingLevel7Collections.add(level7Collection3);
        depositingLevel7Collections.add(level7Collection1);
        depositingLevel7Collections.add(level7Collection2);

        when(
                level7CollectionService.findLevel7CollectionsByDepositStateType(EnumSet.of(DepositState.Type.STAGING,
                        DepositState.Type.DEPOSITING, DepositState.Type.NOTIFYING))).thenReturn(
                depositingLevel7Collections);

        controller.level7CollectionDepositStatus(model);

        assertEquals(Arrays.asList(new Level7Collection[] { level7Collection3, level7Collection2, level7Collection1 }),
                model.asMap().get(Level7DepositUiController.DEPOSITING_LEVEL7_COLLECTIONS_MODEL_KEY));
    }

    @Test
    public void testDepositStatusEndpointHasDepositedLevel7CollectionsSortedByCollectionIdDescending()
    {
        Model model = spy(new ExtendedModelMap());

        List<Level7Collection> depositedLevel7Collections = new ArrayList<>();

        Level7Collection level7Collection1 = new Level7Collection(11111);
        Level7Collection level7Collection2 = new Level7Collection(22222);
        Level7Collection level7Collection3 = new Level7Collection(33333);

        // Note order
        depositedLevel7Collections.add(level7Collection3);
        depositedLevel7Collections.add(level7Collection1);
        depositedLevel7Collections.add(level7Collection2);

        when(level7CollectionService.findRecentlyCompletedLevel7Collections()).thenReturn(depositedLevel7Collections);

        controller.level7CollectionDepositStatus(model);

        assertEquals(Arrays.asList(new Level7Collection[] { level7Collection3, level7Collection2, level7Collection1 }),
                model.asMap().get(Level7DepositUiController.DEPOSITED_LEVEL7_COLLECTIONS_MODEL_KEY));
    }

    @Test
    public void testDepositStatusEndpointHasFailedLevel7CollectionsSortedByCollectionIdDescending()
    {
        Model model = spy(new ExtendedModelMap());

        List<Level7Collection> failedLevel7Collections = new ArrayList<>();

        Level7Collection level7Collection1 = new Level7Collection(11111);
        Level7Collection level7Collection2 = new Level7Collection(22222);
        Level7Collection level7Collection3 = new Level7Collection(33333);

        // Note order
        failedLevel7Collections.add(level7Collection3);
        failedLevel7Collections.add(level7Collection1);
        failedLevel7Collections.add(level7Collection2);

        when(level7CollectionService.findLevel7CollectionsByDepositStateType(DepositState.Type.FAILED)).thenReturn(
                failedLevel7Collections);

        controller.level7CollectionDepositStatus(model);

        assertEquals(Arrays.asList(new Level7Collection[] { level7Collection3, level7Collection2, level7Collection1 }),
                model.asMap().get(Level7DepositUiController.FAILED_LEVEL7_COLLECTIONS_MODEL_KEY));
    }

    @Test
    public void showLevel7CollectionWithUnknownLevel7CollectionThrowsResourceNotFoundException()
            throws ResourceNotFoundException
    {
        long collectionId = 1111;

        exception.expect(ResourceNotFoundException.class);
        exception.expectMessage("No level 7 collection with collection id '" + collectionId + "'");

        when(level7CollectionRepository.findByDapCollectionId(anyLong())).thenReturn(null);

        Model model = spy(new ExtendedModelMap());
        controller.showLevel7Collection(model, collectionId);
    }

    @Test
    public void showLevel7CollectionWithKnownCollectionIdShouldReturnShowPage() throws ResourceNotFoundException
    {
        long collectionId = 1111;

        when(level7CollectionRepository.findByDapCollectionId(anyLong()))
                .thenReturn(new Level7Collection(collectionId));

        Model model = spy(new ExtendedModelMap());
        assertEquals(Level7DepositUiController.LEVEL7_SHOW_PAGE, controller.showLevel7Collection(model, collectionId));
    }

    @Test
    public void showLevel7CollectionWithKnownCollectionIdShouldSetMatchingLevel7CollectionInModel()
            throws ResourceNotFoundException
    {
        long collectionId = 1111;
        Level7Collection level7Collection = new Level7Collection(collectionId);
        when(level7CollectionRepository.findByDapCollectionId(anyInt())).thenReturn(level7Collection);

        Model model = spy(new ExtendedModelMap());
        controller.showLevel7Collection(model, collectionId);
        assertTrue(model.asMap().keySet().contains(Level7DepositUiController.LEVEL7_COLLECTION_MODEL_KEY));
        assertEquals(level7Collection, model.asMap().get(Level7DepositUiController.LEVEL7_COLLECTION_MODEL_KEY));
    }

    @Test
    public void showLevel7CollectionWithKnownCollectionIdShouldSetMatchingLevel7CollectionDepositableArtefactsInModel()
            throws ResourceNotFoundException
    {
        long collectionId = 1111;

        Level7Collection level7Collection = new Level7Collection(collectionId);

        Catalogue catalogue4 = new Catalogue(CatalogueType.LEVEL7);
        catalogue4.setFilename("level7BCatalogue2.xml");
        level7Collection.addCatalogue(catalogue4);

        Catalogue catalogue1 = new Catalogue(CatalogueType.LEVEL7);
        catalogue1.setFilename("level7ACatalogue1.xml");
        level7Collection.addCatalogue(catalogue1);

        Catalogue catalogue2 = new Catalogue(CatalogueType.LEVEL7);
        catalogue2.setFilename("level7ACatalogue2.xml");
        level7Collection.addCatalogue(catalogue2);

        Catalogue catalogue3 = new Catalogue(CatalogueType.LEVEL7);
        catalogue3.setFilename("level7BCatalogue1.xml");
        level7Collection.addCatalogue(catalogue3);

        when(level7CollectionRepository.findByDapCollectionId(anyLong())).thenReturn(level7Collection);

        Model model = spy(new ExtendedModelMap());
        controller.showLevel7Collection(model, collectionId);
        assertTrue(model.asMap().keySet()
                .contains(Level7DepositUiController.LEVEL7_COLLECTION_DEPOSITABLE_ARTEFACTS_MODEL_KEY));
        assertEquals(Arrays.asList(catalogue1, catalogue2, catalogue3, catalogue4),
                model.asMap().get(Level7DepositUiController.LEVEL7_COLLECTION_DEPOSITABLE_ARTEFACTS_MODEL_KEY));
    }

}
