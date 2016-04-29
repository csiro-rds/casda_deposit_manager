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


import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import jdk.nashorn.internal.ir.annotations.Ignore;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositStateChangeListener;
import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.datadeposit.StagedDepositState;
import au.csiro.casda.datadeposit.StagingDepositState;
import au.csiro.casda.deposit.jpa.Level7CollectionRepository;
import au.csiro.casda.deposit.jpa.ProjectRepository;
import au.csiro.casda.dto.DepositStateDTO;
import au.csiro.casda.dto.DepositableArtefactDTO;
import au.csiro.casda.dto.ParentDepositableDTO;
import au.csiro.casda.entity.observation.Catalogue;
import au.csiro.casda.entity.observation.CatalogueType;
import au.csiro.casda.entity.observation.Level7Collection;
import au.csiro.casda.entity.observation.Project;

/**
 * Tests the level 7 deposit service.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public class Level7DepositServiceTest
{
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private File tempLevel7Folder;

    @Mock
    private Level7CollectionRepository level7CollectionRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private Level7DepositProgressor level7DepositProgressor;

    @Mock
    private DepositStateFactory depositStateFactory;

    @Mock
    private DepositStateChangeListener depositStateChangeListener;

    private Level7DepositService level7DepositService;

    @Before
    public void setup() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        tempLevel7Folder = tempFolder.newFolder("level7");
        level7DepositService =
                spy(new Level7DepositService(tempLevel7Folder.toPath().toString(), level7CollectionRepository,
                        projectRepository, level7DepositProgressor, depositStateFactory, depositStateChangeListener));
    }

    @Test
    public void testInitiateLevel7CollectionDepositMustBeNew() throws Exception
    {
        exception.expect(Level7DepositService.CollectionIllegalStateException.class);
        exception.expectMessage("Level 7 collection with id '123' deposit has already been initiated");

        when(level7CollectionRepository.findByDapCollectionId(123L)).thenReturn(new Level7Collection(123L));

        level7DepositService.initiateLevel7CollectionDeposit("ABC123", 123);
    }

    @Test
    public void testInitiateLevel7CollectionDepositCreatesNewProjectIfNoneExists() throws Exception
    {
        when(projectRepository.findByOpalCode("ABC123")).thenReturn(null);

        File collectionFolder = new File(tempLevel7Folder, "123");
        collectionFolder.mkdirs();
        File file = new File(collectionFolder, "foo.xml");
        FileUtils.writeStringToFile(file, "some data");
        File checksum = new File(collectionFolder, file.getName() + ".checksum");
        FileUtils.writeStringToFile(checksum, "a checksum");

        when(level7CollectionRepository.save(any(Level7Collection.class))).then(returnsFirstArg());

        Level7Collection level7Collection = level7DepositService.initiateLevel7CollectionDeposit("ABC123", 123);

        Project project = level7Collection.getProject();
        assertNotNull(project);
        assertFalse(project.getKnownProject());
        assertEquals("ABC123", project.getOpalCode());
    }

    @Test
    public void testInitiateLevel7CollectionDepositMustHaveItemsForNoDirectory() throws Exception
    {
        exception.expect(Level7DepositService.CollectionIllegalStateException.class);
        exception.expectMessage("Level 7 collection with id '123' has no items to deposit");

        when(level7CollectionRepository.findByDapCollectionId(123L)).thenReturn(null);

        level7DepositService.initiateLevel7CollectionDeposit("ABC123", 123);
    }

    @Test
    public void testInitiateLevel7CollectionDepositMustHaveItemsForEmptyDirectory() throws Exception
    {
        File collectionDir = new File(tempLevel7Folder, "515199");
        collectionDir.mkdirs();
        assertTrue(collectionDir.exists());

        exception.expect(Level7DepositService.CollectionIllegalStateException.class);
        exception.expectMessage("Level 7 collection with id '515199' has no items to deposit");

        level7DepositService.initiateLevel7CollectionDeposit("ABC213", 515199);
    }

    @Test
    public void testInitiateLevel7CollectionDepositMustHaveItemsForDirectoryWithOnlyChecksumFiles() throws Exception
    {
        File collectionDir = new File(tempLevel7Folder, "515199");
        collectionDir.mkdirs();
        assertTrue(collectionDir.exists());
        File checksum = new File(collectionDir, "foo.xml.checksum");
        FileUtils.writeStringToFile(checksum, "some data");

        exception.expect(Level7DepositService.CollectionIllegalStateException.class);
        exception.expectMessage("Level 7 collection with id '515199' has no items to deposit");

        level7DepositService.initiateLevel7CollectionDeposit("ABC213", 515199);
    }

    @Test
    public void testInitiateLevel7CollectionDepositUsesKnownProjectIfExists() throws Exception
    {
        Project knownProject = new Project("ABC123");
        knownProject.setKnownProject(Boolean.TRUE);

        when(projectRepository.findByOpalCode("ABC123")).thenReturn(knownProject);

        File collectionFolder = new File(tempLevel7Folder, "124");
        collectionFolder.mkdirs();
        File file = new File(collectionFolder, "foo.xml");
        FileUtils.writeStringToFile(file, "some data");
        File checksum = new File(collectionFolder, file.getName() + ".checksum");
        FileUtils.writeStringToFile(checksum, "a checksum");

        when(level7CollectionRepository.save(any(Level7Collection.class))).then(returnsFirstArg());

        Level7Collection level7Collection = level7DepositService.initiateLevel7CollectionDeposit("ABC123", 124);

        Project project = level7Collection.getProject();
        assertEquals(knownProject, project);
    }

    @Test
    public void testInitiateLevel7CollectionDepositCreatesCatalogueRecordForAllFiles() throws Exception
    {
        when(projectRepository.findByOpalCode("ABC123")).thenReturn(null);

        File collectionFolder = new File(tempLevel7Folder, "125");
        collectionFolder.mkdirs();

        File catalogueFile = new File(collectionFolder, "sample.votable");
        catalogueFile.createNewFile();

        File catalogueFile2 = new File(collectionFolder, "sample2.xml");
        catalogueFile2.createNewFile();

        File catalogueFile3 = new File(collectionFolder, "sample3.vot");
        catalogueFile3.createNewFile();

        File catalogueFile4 = new File(collectionFolder, "sample4.something");
        catalogueFile4.createNewFile();

        when(level7CollectionRepository.save(any(Level7Collection.class))).then(returnsFirstArg());

        Level7Collection level7Collection = level7DepositService.initiateLevel7CollectionDeposit("ABC123", 125);

        Project project = level7Collection.getProject();
        assertEquals("ABC123", project.getOpalCode());
        assertEquals(125, level7Collection.getDapCollectionId());

        List<String> filenames =
                level7Collection.getCatalogues().stream().map(catalogue -> catalogue.getFilename())
                        .collect(Collectors.toList());

        assertThat(filenames.size(), equalTo(4));
        assertThat(filenames, containsInAnyOrder("sample.votable", "sample2.xml", "sample3.vot", "sample4.something"));

        for (Catalogue catalogue : level7Collection.getCatalogues())
        {
            assertEquals("votable", catalogue.getFormat());
            assertNull(catalogue.getFilesize());
            assertEquals(CatalogueType.LEVEL7, catalogue.getCatalogueType());
        }

        verify(level7CollectionRepository, times(1)).save(eq(level7Collection));
    }

    @Test
    public void testInitiateLevel7CollectionDepositDoesNotCreateCatalogueRecordsForChecksumFiles() throws Exception
    {
        when(projectRepository.findByOpalCode("ABC123")).thenReturn(null);

        File collectionFolder = new File(tempLevel7Folder, "125");
        collectionFolder.mkdirs();

        File catalogueFile1 = new File(collectionFolder, "sample.votable");
        catalogueFile1.createNewFile();
        File checksumFile1 = new File(collectionFolder, catalogueFile1.getName() + ".checksum");
        FileUtils.writeStringToFile(checksumFile1, "checksum1");

        File catalogueFile2 = new File(collectionFolder, "sample2.xml");
        catalogueFile2.createNewFile();
        /* File checksumFile2 = */new File(collectionFolder, catalogueFile2.getName() + ".checksum");
        // checksumFile2 deliberately not created

        File catalogueFile3 = new File(collectionFolder, "sample3.vot");
        // catalogueFile3 deliberately not created
        File checksumFile3 = new File(collectionFolder, catalogueFile3.getName() + ".checksum");
        FileUtils.writeStringToFile(checksumFile3, "checksum3");

        when(level7CollectionRepository.save(any(Level7Collection.class))).then(returnsFirstArg());

        Level7Collection level7Collection = level7DepositService.initiateLevel7CollectionDeposit("ABC123", 125);

        Project project = level7Collection.getProject();
        assertEquals("ABC123", project.getOpalCode());
        assertEquals(125, level7Collection.getDapCollectionId());

        List<String> filenames =
                level7Collection.getCatalogues().stream().map(catalogue -> catalogue.getFilename())
                        .collect(Collectors.toList());

        assertThat(filenames.size(), equalTo(2));
        assertThat(filenames, containsInAnyOrder("sample.votable", "sample2.xml"));

        for (Catalogue catalogue : level7Collection.getCatalogues())
        {
            assertEquals("votable", catalogue.getFormat());
            assertNull(catalogue.getFilesize());
            assertEquals(CatalogueType.LEVEL7, catalogue.getCatalogueType());
        }

        verify(level7CollectionRepository, times(1)).save(eq(level7Collection));
    }

    @Test
    public void testProgressCollections() throws Exception
    {
        Level7Collection collectionOne = new Level7Collection(111);
        collectionOne.setDepositState(new StagingDepositState(null, null));
        Level7Collection collectionTwo = new Level7Collection(222);
        collectionOne.setDepositState(new StagedDepositState(null, null));

        List<Level7Collection> depositingCollections = Arrays.asList(collectionOne, collectionTwo);

        when(level7CollectionRepository.findDepositingLevel7Collections()).thenReturn(depositingCollections);
        when(level7CollectionRepository.findByDapCollectionId(111L)).thenReturn(collectionOne);
        when(level7CollectionRepository.findByDapCollectionId(222L)).thenReturn(collectionTwo);

        level7DepositService.progressCollections();

        verify(level7DepositProgressor, times(2)).progressCollection(any(Long.class));
        verify(level7DepositProgressor, times(1)).progressCollection(111L);
        verify(level7DepositProgressor, times(1)).progressCollection(222L);
    }

    @Test
    public void testSaveFileForLevel7CollectionDeposit() throws IOException
    {
        File collectionFolder = new File(tempLevel7Folder, "125");
        assertThat(collectionFolder.exists(), is(not(true)));

        tempLevel7Folder = tempFolder.newFolder("testSaveFileForLevel7CollectionDeposit");

        File file1 = new File(tempLevel7Folder, "fox");
        List<String> file1contents = Arrays.asList("The quick", "brown", "fox");
        FileUtils.writeLines(file1, file1contents);

        File file2 = new File(tempLevel7Folder, "dog");
        List<String> file2contents = Arrays.asList("jumped", "over the", "lazy", "dog");
        FileUtils.writeLines(file2, file2contents);

        FileInputStream fis1 = new FileInputStream(file1);
        level7DepositService.saveFileForLevel7CollectionDeposit(125L, file1.getName(), fis1);
        fis1.close();
        assertThat(collectionFolder.exists(), is(true));
        assertThat(new File(collectionFolder, file1.getName()).exists(), is(true));
        assertThat(FileUtils.readLines(new File(collectionFolder, file1.getName())), equalTo(file1contents));

        FileInputStream fis2 = new FileInputStream(file2);
        level7DepositService.saveFileForLevel7CollectionDeposit(125L, file2.getName(), fis2);
        fis1.close();
        assertThat(collectionFolder.exists(), is(true));
        assertThat(new File(collectionFolder, file1.getName()).exists(), is(true));
        assertThat(FileUtils.readLines(new File(collectionFolder, file1.getName())), equalTo(file1contents));
        assertThat(new File(collectionFolder, file2.getName()).exists(), is(true));
        assertThat(FileUtils.readLines(new File(collectionFolder, file2.getName())), equalTo(file2contents));
    }

    @Test
    public void testGetLevel7CollectionSummaryForNoCollectionInDatabaseNoDepositDirectory() throws Exception
    {
        long collectionId = RandomUtils.nextLong(1, Long.MAX_VALUE);
        String opalCode = RandomStringUtils.random(6);

        when(level7CollectionRepository.findByDapCollectionId(collectionId)).thenReturn(null);

        assertThat(level7DepositService.getLevel7CollectionSummary(opalCode, collectionId), is(nullValue()));
    }

    @Test
    public void testGetLevel7CollectionSummaryForNoCollectionInDatabaseEmptyDepositDirectory() throws Exception
    {
        long collectionId = RandomUtils.nextLong(1, Long.MAX_VALUE);
        String opalCode = RandomStringUtils.random(6);

        File collectionFolder = new File(tempLevel7Folder, Long.toString(collectionId));
        collectionFolder.mkdirs();

        when(level7CollectionRepository.findByDapCollectionId(collectionId)).thenReturn(null);

        ParentDepositableDTO level7CollectionSummary =
                level7DepositService.getLevel7CollectionSummary(opalCode, collectionId);
        assertThat(level7CollectionSummary, is(notNullValue()));
        assertThat(level7CollectionSummary.getDepositState(), is(DepositStateDTO.BUILDING_DEPOSIT));
        assertThat(level7CollectionSummary.getDepositableTypeDescription(), equalTo("Level 7 Collection"));
        assertThat(level7CollectionSummary.getDepositableArtefacts().length, is(0));
    }

    @Test
    public void testGetLevel7CollectionSummaryForNoCollectionInDatabasePopulatedDepositDirectory() throws Exception
    {
        long collectionId = RandomUtils.nextLong(1, Long.MAX_VALUE);
        String opalCode = RandomStringUtils.random(6);

        File collectionFolder = new File(tempLevel7Folder, Long.toString(collectionId));
        collectionFolder.mkdirs();

        File catalogueFile1 = new File(collectionFolder, "sample.votable");
        catalogueFile1.createNewFile();
        File checksumFile1 = new File(collectionFolder, catalogueFile1.getName() + ".checksum");
        FileUtils.writeStringToFile(checksumFile1, "checksum1");

        File catalogueFile2 = new File(collectionFolder, "sample2.xml");
        catalogueFile2.createNewFile();
        /* File checksumFile2 = */new File(collectionFolder, catalogueFile2.getName() + ".checksum");
        // checksumFile2 deliberately not created

        File catalogueFile3 = new File(collectionFolder, "sample3.vot");
        // catalogueFile3 deliberately not created
        File checksumFile3 = new File(collectionFolder, catalogueFile3.getName() + ".checksum");
        FileUtils.writeStringToFile(checksumFile3, "checksum3");

        when(level7CollectionRepository.findByDapCollectionId(collectionId)).thenReturn(null);

        ParentDepositableDTO level7CollectionSummary =
                level7DepositService.getLevel7CollectionSummary(opalCode, collectionId);
        assertThat(level7CollectionSummary, is(notNullValue()));
        assertThat(level7CollectionSummary.getDepositState(), is(DepositStateDTO.BUILDING_DEPOSIT));
        assertThat(level7CollectionSummary.getDepositableTypeDescription(), equalTo("Level 7 Collection"));
        assertThat(level7CollectionSummary.getDepositableArtefacts().length, is(2));

        DepositableArtefactDTO catalogueFile1Dto =
                level7CollectionSummary.getDepositableArtefactForFilename(catalogueFile1.getName());
        assertThat(catalogueFile1Dto, is(notNullValue()));
        assertThat(catalogueFile1Dto.getDepositState(), is(DepositStateDTO.BUILDING_DEPOSIT));
        assertThat(catalogueFile1Dto.getDepositableTypeDescription(), equalTo("Level 7 Catalogue"));
        assertThat(catalogueFile1Dto.getFilename(), equalTo(catalogueFile1.getName()));
        assertThat(catalogueFile1Dto.getFilesizeInBytes(), equalTo(catalogueFile1.length()));
        assertThat(catalogueFile1Dto.getChecksum(), equalTo(FileUtils.readFileToString(checksumFile1)));

        DepositableArtefactDTO catalogueFile2Dto =
                level7CollectionSummary.getDepositableArtefactForFilename(catalogueFile2.getName());
        assertThat(catalogueFile2Dto, is(notNullValue()));
        assertThat(catalogueFile2Dto.getDepositState(), is(DepositStateDTO.BUILDING_DEPOSIT));
        assertThat(catalogueFile2Dto.getDepositableTypeDescription(), equalTo("Level 7 Catalogue"));
        assertThat(catalogueFile2Dto.getFilename(), equalTo(catalogueFile2.getName()));
        assertThat(catalogueFile2Dto.getFilesizeInBytes(), equalTo(catalogueFile2.length()));
        assertThat(catalogueFile2Dto.getChecksum(), is(nullValue()));
    }

    @Test
    public void testGetLevel7CollectionSummaryForCollectionInDatabaseWithDifferentProjectCode() throws Exception
    {
        long collectionId = RandomUtils.nextLong(1, Long.MAX_VALUE);
        String opalCode = RandomStringUtils.random(6);
        DepositState depositingDepositState = mock(DepositState.class);
        when(depositingDepositState.getType()).thenReturn(DepositState.Type.DEPOSITING);

        Project project = new Project(opalCode);
        Level7Collection collection = new Level7Collection();
        collection.setId(collectionId);
        collection.setProject(project);
        collection.setDepositState(depositingDepositState);

        Catalogue catalogue1 = new Catalogue();
        catalogue1.setCatalogueType(CatalogueType.LEVEL7);
        catalogue1.setProject(project);
        catalogue1.setFilename("catalogue1");
        catalogue1.setDepositState(depositingDepositState);
        catalogue1.setFilesize(RandomUtils.nextLong(1, Long.MAX_VALUE));
        collection.addCatalogue(catalogue1);

        when(level7CollectionRepository.findByDapCollectionId(collectionId)).thenReturn(collection);

        exception.expect(Level7DepositService.CollectionProjectCodeMismatchException.class);
        exception.expectMessage("Level 7 collection matching collection id '" + collectionId
                + "' had a different project code '" + opalCode + "' (expected '" + opalCode + "FOO" + "')");

        level7DepositService.getLevel7CollectionSummary(opalCode + "FOO", collectionId);
    }

    @Test
    public void testGetLevel7CollectionSummaryForCollectionInDatabaseNoDepositDirectory() throws Exception
    {
        long collectionId = RandomUtils.nextLong(1, Long.MAX_VALUE);
        String opalCode = RandomStringUtils.random(6);
        DepositState depositingDepositState = mock(DepositState.class);
        when(depositingDepositState.getType()).thenReturn(DepositState.Type.DEPOSITING);

        Project project = new Project(opalCode);
        Level7Collection collection = new Level7Collection();
        collection.setProject(project);
        collection.setDepositState(depositingDepositState);

        Catalogue catalogue1 = new Catalogue();
        catalogue1.setCatalogueType(CatalogueType.LEVEL7);
        catalogue1.setProject(project);
        catalogue1.setFilename("catalogue1");
        catalogue1.setDepositState(depositingDepositState);
        catalogue1.setFilesize(RandomUtils.nextLong(1, Long.MAX_VALUE));
        collection.addCatalogue(catalogue1);

        Catalogue catalogue2 = new Catalogue();
        catalogue2.setCatalogueType(CatalogueType.LEVEL7);
        catalogue2.setProject(project);
        catalogue2.setFilename("catalogue2");
        catalogue2.setDepositState(depositingDepositState);
        catalogue2.setFilesize(RandomUtils.nextLong(1, Long.MAX_VALUE));
        collection.addCatalogue(catalogue2);

        when(level7CollectionRepository.findByDapCollectionId(collectionId)).thenReturn(collection);

        ParentDepositableDTO level7CollectionSummary =
                level7DepositService.getLevel7CollectionSummary(opalCode, collectionId);
        assertThat(level7CollectionSummary, is(notNullValue()));
        assertThat(level7CollectionSummary, is(notNullValue()));
        assertThat(level7CollectionSummary.getDepositState(), is(DepositStateDTO.DEPOSITING));
        assertThat(level7CollectionSummary.getDepositableTypeDescription(), equalTo("Level 7 Collection"));
        assertThat(level7CollectionSummary.getDepositableArtefacts().length, is(2));

        DepositableArtefactDTO catalogueFile1Dto =
                level7CollectionSummary.getDepositableArtefactForFilename(catalogue1.getFilename());
        assertThat(catalogueFile1Dto, is(notNullValue()));
        assertThat(catalogueFile1Dto.getDepositState(), is(DepositStateDTO.DEPOSITING));
        assertThat(catalogueFile1Dto.getDepositableTypeDescription(), equalTo("Level 7 Catalogue"));
        assertThat(catalogueFile1Dto.getFilename(), equalTo(catalogue1.getFilename()));
        assertThat(catalogueFile1Dto.getFilesizeInBytes(), equalTo(catalogue1.getFilesize()));
        assertThat(catalogueFile1Dto.getChecksum(), is(nullValue()));

        DepositableArtefactDTO catalogueFile2Dto =
                level7CollectionSummary.getDepositableArtefactForFilename(catalogue2.getFilename());
        assertThat(catalogueFile2Dto, is(notNullValue()));
        assertThat(catalogueFile2Dto.getDepositState(), is(DepositStateDTO.DEPOSITING));
        assertThat(catalogueFile2Dto.getDepositableTypeDescription(), equalTo("Level 7 Catalogue"));
        assertThat(catalogueFile2Dto.getFilename(), equalTo(catalogue2.getFilename()));
        assertThat(catalogueFile2Dto.getFilesizeInBytes(), equalTo(catalogue2.getFilesize()));
        assertThat(catalogueFile2Dto.getChecksum(), is(nullValue()));
    }

    @Test
    public void testGetLevel7CollectionSummaryForCollectionInDatabaseEmptyDepositDirectory() throws Exception
    {
        long collectionId = RandomUtils.nextLong(1, Long.MAX_VALUE);
        String opalCode = RandomStringUtils.random(6);
        DepositState depositingDepositState = mock(DepositState.class);
        when(depositingDepositState.getType()).thenReturn(DepositState.Type.DEPOSITING);

        Project project = new Project(opalCode);
        Level7Collection collection = new Level7Collection();
        collection.setProject(project);
        collection.setDepositState(depositingDepositState);

        Catalogue catalogue1 = new Catalogue();
        catalogue1.setCatalogueType(CatalogueType.LEVEL7);
        catalogue1.setProject(project);
        catalogue1.setFilename("catalogue1");
        catalogue1.setDepositState(depositingDepositState);
        catalogue1.setFilesize(RandomUtils.nextLong(1, Long.MAX_VALUE));
        collection.addCatalogue(catalogue1);

        Catalogue catalogue2 = new Catalogue();
        catalogue2.setCatalogueType(CatalogueType.LEVEL7);
        catalogue2.setProject(project);
        catalogue2.setFilename("catalogue2");
        catalogue2.setDepositState(depositingDepositState);
        catalogue2.setFilesize(RandomUtils.nextLong(1, Long.MAX_VALUE));
        collection.addCatalogue(catalogue2);

        when(level7CollectionRepository.findByDapCollectionId(collectionId)).thenReturn(collection);

        File collectionFolder = new File(tempLevel7Folder, Long.toString(collectionId));
        collectionFolder.mkdirs();

        ParentDepositableDTO level7CollectionSummary =
                level7DepositService.getLevel7CollectionSummary(opalCode, collectionId);
        assertThat(level7CollectionSummary, is(notNullValue()));
        assertThat(level7CollectionSummary, is(notNullValue()));
        assertThat(level7CollectionSummary.getDepositState(), is(DepositStateDTO.DEPOSITING));
        assertThat(level7CollectionSummary.getDepositableTypeDescription(), equalTo("Level 7 Collection"));
        assertThat(level7CollectionSummary.getDepositableArtefacts().length, is(2));

        DepositableArtefactDTO catalogue1Dto =
                level7CollectionSummary.getDepositableArtefactForFilename(catalogue1.getFilename());
        assertThat(catalogue1Dto, is(notNullValue()));
        assertThat(catalogue1Dto.getDepositState(), is(DepositStateDTO.DEPOSITING));
        assertThat(catalogue1Dto.getDepositableTypeDescription(), equalTo("Level 7 Catalogue"));
        assertThat(catalogue1Dto.getFilename(), equalTo(catalogue1.getFilename()));
        assertThat(catalogue1Dto.getFilesizeInBytes(), equalTo(catalogue1.getFilesize()));
        assertThat(catalogue1Dto.getChecksum(), is(nullValue()));

        DepositableArtefactDTO catalogue2Dto =
                level7CollectionSummary.getDepositableArtefactForFilename(catalogue2.getFilename());
        assertThat(catalogue2Dto, is(notNullValue()));
        assertThat(catalogue2Dto.getDepositState(), is(DepositStateDTO.DEPOSITING));
        assertThat(catalogue2Dto.getDepositableTypeDescription(), equalTo("Level 7 Catalogue"));
        assertThat(catalogue2Dto.getFilename(), equalTo(catalogue2.getFilename()));
        assertThat(catalogue2Dto.getFilesizeInBytes(), equalTo(catalogue2.getFilesize()));
        assertThat(catalogue2Dto.getChecksum(), is(nullValue()));
    }

    @Test
    public void testGetLevel7CollectionSummaryForCollectionInDatabasePopulatedDepositDirectory() throws Exception
    {
        long collectionId = RandomUtils.nextLong(1, Long.MAX_VALUE);
        String opalCode = RandomStringUtils.random(6);
        DepositState depositingDepositState = mock(DepositState.class);
        when(depositingDepositState.getType()).thenReturn(DepositState.Type.DEPOSITING);

        Project project = new Project(opalCode);
        Level7Collection collection = new Level7Collection();
        collection.setProject(project);
        collection.setDepositState(depositingDepositState);

        Catalogue catalogue1 = new Catalogue();
        catalogue1.setCatalogueType(CatalogueType.LEVEL7);
        catalogue1.setProject(project);
        catalogue1.setFilename("catalogue1");
        catalogue1.setDepositState(depositingDepositState);
        catalogue1.setFilesize(RandomUtils.nextLong(1, Long.MAX_VALUE));
        collection.addCatalogue(catalogue1);

        Catalogue catalogue2 = new Catalogue();
        catalogue2.setCatalogueType(CatalogueType.LEVEL7);
        catalogue2.setProject(project);
        catalogue2.setFilename("catalogue2");
        catalogue2.setDepositState(depositingDepositState);
        catalogue2.setFilesize(RandomUtils.nextLong(1, Long.MAX_VALUE));
        collection.addCatalogue(catalogue2);

        when(level7CollectionRepository.findByDapCollectionId(collectionId)).thenReturn(collection);

        File collectionFolder = new File(tempLevel7Folder, Long.toString(collectionId));
        collectionFolder.mkdirs();

        File catalogueFile1 = new File(collectionFolder, "sample.votable");
        catalogueFile1.createNewFile();
        File checksumFile1 = new File(collectionFolder, catalogueFile1.getName() + ".checksum");
        FileUtils.writeStringToFile(checksumFile1, "checksum1");

        File catalogueFile2 = new File(collectionFolder, "sample2.xml");
        catalogueFile2.createNewFile();
        /* File checksumFile2 = */new File(collectionFolder, catalogueFile2.getName() + ".checksum");
        // checksumFile2 deliberately not created

        File catalogueFile3 = new File(collectionFolder, "sample3.vot");
        // catalogueFile3 deliberately not created
        File checksumFile3 = new File(collectionFolder, catalogueFile3.getName() + ".checksum");
        FileUtils.writeStringToFile(checksumFile3, "checksum3");

        // Matches catalogue1
        File catalogueFile4 = new File(collectionFolder, catalogue1.getFilename());
        catalogueFile4.createNewFile();
        File checksumFile4 = new File(collectionFolder, catalogueFile4.getName() + ".checksum");
        FileUtils.writeStringToFile(checksumFile4, "checksum4");

        // Matches catalogue2
        File catalogueFile5 = new File(collectionFolder, catalogue2.getFilename());
        catalogueFile5.createNewFile();
        /* File checksumFile5 = */new File(collectionFolder, catalogueFile5.getName() + ".checksum");
        // checksumFile5 deliberately not created

        ParentDepositableDTO level7CollectionSummary =
                level7DepositService.getLevel7CollectionSummary(opalCode, collectionId);
        assertThat(level7CollectionSummary, is(notNullValue()));
        assertThat(level7CollectionSummary, is(notNullValue()));
        assertThat(level7CollectionSummary.getDepositState(), is(DepositStateDTO.DEPOSITING));
        assertThat(level7CollectionSummary.getDepositableTypeDescription(), equalTo("Level 7 Collection"));
        assertThat(level7CollectionSummary.getDepositableArtefacts().length, is(4));

        DepositableArtefactDTO catalogue1Dto =
                level7CollectionSummary.getDepositableArtefactForFilename(catalogue1.getFilename());
        assertThat(catalogue1Dto, is(notNullValue()));
        assertThat(catalogue1Dto.getDepositState(), is(DepositStateDTO.DEPOSITING));
        assertThat(catalogue1Dto.getDepositableTypeDescription(), equalTo("Level 7 Catalogue"));
        assertThat(catalogue1Dto.getFilename(), equalTo(catalogue1.getFilename()));
        assertThat(catalogue1Dto.getFilesizeInBytes(), equalTo(catalogue1.getFilesize()));
        assertThat(catalogue1Dto.getChecksum(), equalTo(FileUtils.readFileToString(checksumFile4)));

        DepositableArtefactDTO catalogue2Dto =
                level7CollectionSummary.getDepositableArtefactForFilename(catalogue2.getFilename());
        assertThat(catalogue2Dto, is(notNullValue()));
        assertThat(catalogue2Dto.getDepositState(), is(DepositStateDTO.DEPOSITING));
        assertThat(catalogue2Dto.getDepositableTypeDescription(), equalTo("Level 7 Catalogue"));
        assertThat(catalogue2Dto.getFilename(), equalTo(catalogue2.getFilename()));
        assertThat(catalogue2Dto.getFilesizeInBytes(), equalTo(catalogue2.getFilesize()));
        assertThat(catalogue2Dto.getChecksum(), is(nullValue()));

        DepositableArtefactDTO catalogueFile1Dto =
                level7CollectionSummary.getDepositableArtefactForFilename(catalogueFile1.getName());
        assertThat(catalogueFile1Dto, is(notNullValue()));
        assertThat(catalogueFile1Dto.getDepositState(), is(DepositStateDTO.BUILDING_DEPOSIT));
        assertThat(catalogueFile1Dto.getDepositableTypeDescription(), equalTo("Level 7 Catalogue"));
        assertThat(catalogueFile1Dto.getFilename(), equalTo(catalogueFile1.getName()));
        assertThat(catalogueFile1Dto.getFilesizeInBytes(), equalTo(catalogueFile1.length()));
        assertThat(catalogueFile1Dto.getChecksum(), equalTo(FileUtils.readFileToString(checksumFile1)));

        DepositableArtefactDTO catalogueFile2Dto =
                level7CollectionSummary.getDepositableArtefactForFilename(catalogueFile2.getName());
        assertThat(catalogueFile2Dto, is(notNullValue()));
        assertThat(catalogueFile2Dto.getDepositState(), is(DepositStateDTO.BUILDING_DEPOSIT));
        assertThat(catalogueFile2Dto.getDepositableTypeDescription(), equalTo("Level 7 Catalogue"));
        assertThat(catalogueFile2Dto.getFilename(), equalTo(catalogueFile2.getName()));
        assertThat(catalogueFile2Dto.getFilesizeInBytes(), equalTo(catalogueFile2.length()));
        assertThat(catalogueFile2Dto.getChecksum(), is(nullValue()));
    }

    @Test
    public void testRecoverFailedLevel7CollectionDepositForUnknownCollection() throws Exception
    {
        long collectionId = RandomUtils.nextLong(1, Long.MAX_VALUE);
        String opalCode = RandomStringUtils.random(6);

        when(level7CollectionRepository.findByDapCollectionId(collectionId)).thenReturn(null);

        exception.expect(Level7DepositService.UnknownCollectionException.class);
        exception.expectMessage("Could not find level 7 collection with id '" + collectionId + "'");

        level7DepositService.recoverFailedLevel7CollectionDeposit(opalCode, collectionId);
    }

    @Ignore
    @Test
    public void testRecoverFailedLevel7CollectionDepositForMismatchedProjectCode() throws Exception
    {
        long collectionId = RandomUtils.nextLong(1, Long.MAX_VALUE);
        String opalCode = RandomStringUtils.random(6);
        DepositState depositingDepositState = mock(DepositState.class);
        when(depositingDepositState.getType()).thenReturn(DepositState.Type.DEPOSITING);

        Project project = new Project(opalCode);
        Level7Collection collection = new Level7Collection();
        collection.setId(collectionId);
        collection.setProject(project);
        collection.setDepositState(depositingDepositState);

        when(level7CollectionRepository.findByDapCollectionId(collectionId)).thenReturn(collection);

        exception.expect(Level7DepositService.CollectionProjectCodeMismatchException.class);
        exception.expectMessage("Level 7 collection matching collection id '" + collectionId
                + "' had a different project code '" + opalCode + "' (expected '" + opalCode + "FOO" + "')");

        level7DepositService.recoverFailedLevel7CollectionDeposit(opalCode + "FOO", collectionId);
    }

    @Test
    public void testRecoverFailedLevel7CollectionDepositForUnfailedCollection() throws Exception
    {
        long collectionId = RandomUtils.nextLong(1, Long.MAX_VALUE);
        String opalCode = RandomStringUtils.random(6);
        DepositState depositingDepositState = mock(DepositState.class);
        when(depositingDepositState.getType()).thenReturn(DepositState.Type.DEPOSITING);

        Project project = new Project(opalCode);
        Level7Collection collection = new Level7Collection();
        collection.setId(collectionId);
        collection.setProject(project);
        collection.setDepositState(depositingDepositState);

        when(level7CollectionRepository.findByDapCollectionId(collectionId)).thenReturn(collection);

        exception.expect(Level7DepositService.CollectionIllegalStateException.class);
        exception.expectMessage("Expected deposit of collection with id '" + collectionId
                + "' to have failed (but is 'Depositing')");

        level7DepositService.recoverFailedLevel7CollectionDeposit(opalCode, collectionId);
    }

    @Test
    public void testRecoverFailedLevel7CollectionDeposit() throws Exception
    {
        long collectionId = RandomUtils.nextLong(1, Long.MAX_VALUE);
        String opalCode = RandomStringUtils.random(6);
        DepositState failedDepositState = mock(DepositState.class);
        when(failedDepositState.getType()).thenReturn(DepositState.Type.FAILED);

        Project project = new Project(opalCode);
        Level7Collection collection = spy(new Level7Collection());
        collection.setProject(project);
        collection.setDepositState(failedDepositState);

        when(level7CollectionRepository.findByDapCollectionId(collectionId)).thenReturn(collection);

        level7DepositService.recoverFailedLevel7CollectionDeposit(opalCode, collectionId);

        verify(collection).recoverDeposit();
        verify(level7CollectionRepository).save(collection);
    }
}
