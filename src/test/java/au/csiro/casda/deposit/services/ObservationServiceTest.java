package au.csiro.casda.deposit.services;

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
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.jdbc.core.JdbcTemplate;

import au.csiro.casda.BadRequestException;
import au.csiro.casda.ResourceNotFoundException;
import au.csiro.casda.datadeposit.DepositState.Type;
import au.csiro.casda.deposit.jpa.ObservationRepository;
import au.csiro.casda.deposit.jpa.ValidationNoteRepository;
import au.csiro.casda.dto.DataProductDTO;
import au.csiro.casda.dto.DataProductDTO.DataProductType;
import au.csiro.casda.dto.DataProductDTO.QualityLevel;
import au.csiro.casda.dto.ObservationProjectDataProductsDTO;
import au.csiro.casda.dto.QualityFlagDTO;
import au.csiro.casda.dto.ValidationNoteDTO;
import au.csiro.casda.entity.QualityFlag;
import au.csiro.casda.entity.ValidationNote;
import au.csiro.casda.entity.observation.Catalogue;
import au.csiro.casda.entity.observation.CatalogueType;
import au.csiro.casda.entity.observation.ImageCube;
import au.csiro.casda.entity.observation.MeasurementSet;
import au.csiro.casda.entity.observation.Observation;
import au.csiro.casda.entity.observation.Project;
import au.csiro.casda.services.dto.Message.MessageCode;
import au.csiro.casda.services.dto.MessageDTO;

/**
 * Tests for the observation service.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 *
 */
public class ObservationServiceTest
{
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private ObservationRepository mockObservationRepository;

    @Mock
    private ValidationNoteRepository mockValidationNoteRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @Mock
    private QualityService mockQualityService;

    @InjectMocks
    private ObservationService observationService;

    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetUnreleasedProjectBlocks()
    {
        ObservationProjectDataProductsDTO one =
                new ObservationProjectDataProductsDTO(123, "one", DateTime.now(), "Bob", "Smith");
        ObservationProjectDataProductsDTO two =
                new ObservationProjectDataProductsDTO(123, "two", DateTime.now(), "Bob", "Smith");
        ObservationProjectDataProductsDTO three =
                new ObservationProjectDataProductsDTO(142, "one", DateTime.now(), "Bob", "Smith");
        ObservationProjectDataProductsDTO four =
                new ObservationProjectDataProductsDTO(152, "four", DateTime.now(), "Bob", "Smith");

        when(mockObservationRepository.findDepositedProjectBlocksWithUnreleasedImageCubes()).thenReturn(
                Arrays.asList(one, two));
        when(mockObservationRepository.findDepositedProjectBlocksWithUnreleasedCatalogues()).thenReturn(
                Arrays.asList(two, three, four));
        when(mockObservationRepository.findDepositedProjectBlocksWithUnreleasedMeasurementSets()).thenReturn(
                new ArrayList<>());

        Set<ObservationProjectDataProductsDTO> unreleasedProjectBlocks =
                observationService.getUnreleasedProjectBlocks();
        assertThat(unreleasedProjectBlocks, containsInAnyOrder(one, two, three, four));
    }

    @Test
    public void testGetReleasedProjectBlocksDate()
    {
        ObservationProjectDataProductsDTO one =
                new ObservationProjectDataProductsDTO(123, "one", DateTime.now(), "Bob", "Smith");
        ObservationProjectDataProductsDTO three =
                new ObservationProjectDataProductsDTO(142, "one", DateTime.now(), "Bob", "Smith");

        Long date = 123L;
        DateTime dateTime = new DateTime(date, DateTimeZone.UTC);
        DateTime defaultDateTime = new DateTime(0L, DateTimeZone.UTC);

        when(mockObservationRepository.findProjectBlocksWithReleasedImageCubesAfterDate(eq("one"), eq(defaultDateTime)))
                .thenReturn(new ArrayList<>());
        when(mockObservationRepository.findProjectBlocksWithReleasedCataloguesAfterDate(eq("one"), eq(defaultDateTime)))
                .thenReturn(Arrays.asList(three));
        when(
                mockObservationRepository.findProjectBlocksWithReleasedMeasurementSetsAfterDate(eq("one"),
                        eq(defaultDateTime))).thenReturn(new ArrayList<>());

        when(mockObservationRepository.findProjectBlocksWithReleasedImageCubesAfterDate(eq("one"), eq(dateTime)))
                .thenReturn(Arrays.asList(one, three));
        when(mockObservationRepository.findProjectBlocksWithReleasedCataloguesAfterDate(eq("one"), eq(dateTime)))
                .thenReturn(Arrays.asList(three));
        when(mockObservationRepository.findProjectBlocksWithReleasedMeasurementSetsAfterDate(eq("one"), eq(dateTime)))
                .thenReturn(new ArrayList<>());

        Set<ObservationProjectDataProductsDTO> releasedObservationsNullDate =
                observationService.getReleasedProjectBlocks("one", null);
        assertThat(releasedObservationsNullDate, containsInAnyOrder(three));

        Set<ObservationProjectDataProductsDTO> releasedObservationsWithDate =
                observationService.getReleasedProjectBlocks("one", 123L);
        assertThat(releasedObservationsWithDate, containsInAnyOrder(one, three));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testNullObservationFoundThrowsResourceNotFound() throws Exception
    {
        when(mockObservationRepository.findBySbid(123)).thenReturn(null);
        observationService.releaseProjectBlock("ABC123", 123, DateTime.now(DateTimeZone.UTC));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testObservationNotDeposited() throws Exception
    {
        Observation observation = spy(new Observation());
        Integer sbid = 123;
        observation.setSbid(sbid);
        doReturn(false).when(observation).isDeposited();
        when(mockObservationRepository.findBySbid(sbid)).thenReturn(observation);
        observationService.releaseProjectBlock("ABC123", sbid, DateTime.now(DateTimeZone.UTC));
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testObservationNoDataProducts() throws Exception
    {
        Integer sbid = 123;
        Observation observation = spy(new Observation(sbid));
        doReturn(true).when(observation).isDeposited();

        when(mockObservationRepository.findBySbid(sbid)).thenReturn(observation);

        observationService.releaseProjectBlock("ABC123", sbid, DateTime.now());
    }

    @Test(expected = BadRequestException.class)
    public void testObservationCatalogueNotValidated() throws Exception
    {
        Integer sbid = 123;
        Observation observation = spy(new Observation(sbid));
        doReturn(true).when(observation).isDeposited();
        Project project = new Project("ABC123");
        Catalogue catalogue = new Catalogue(CatalogueType.CONTINUUM_COMPONENT);
        catalogue.setProject(project);
        catalogue.setQualityLevel(au.csiro.casda.entity.observation.QualityLevel.GOOD);
        Catalogue catalogue2 = new Catalogue(CatalogueType.CONTINUUM_COMPONENT);
        catalogue2.setQualityLevel(au.csiro.casda.entity.observation.QualityLevel.NOT_VALIDATED);
        catalogue2.setProject(project);
        observation.addCatalogue(catalogue);
        observation.addCatalogue(catalogue2);

        when(mockObservationRepository.findBySbid(sbid)).thenReturn(observation);

        observationService.releaseProjectBlock("ABC123", sbid, DateTime.now());
    }

    @Test(expected = BadRequestException.class)
    public void testObservationImageCubeNotValidated() throws Exception
    {
        Integer sbid = 123;
        Observation observation = spy(new Observation(sbid));
        doReturn(true).when(observation).isDeposited();
        Project project = new Project("ABC123");
        ImageCube imageCube = new ImageCube(project);
        imageCube.setQualityLevel(au.csiro.casda.entity.observation.QualityLevel.GOOD);
        ImageCube imageCube2 = new ImageCube(project);
        imageCube2.setQualityLevel(au.csiro.casda.entity.observation.QualityLevel.NOT_VALIDATED);
        imageCube2.setProject(project);
        observation.addImageCube(imageCube);
        observation.addImageCube(imageCube2);

        when(mockObservationRepository.findBySbid(sbid)).thenReturn(observation);

        observationService.releaseProjectBlock("ABC123", sbid, DateTime.now());
    }

    @Test(expected = BadRequestException.class)
    public void testObservationMeasurementSetNotValidated() throws Exception
    {
        Integer sbid = 123;
        Observation observation = spy(new Observation(sbid));
        doReturn(true).when(observation).isDeposited();
        Project project = new Project("ABC123");
        MeasurementSet measurementSet = new MeasurementSet(project);
        measurementSet.setQualityLevel(au.csiro.casda.entity.observation.QualityLevel.GOOD);
        MeasurementSet measurementSet2 = new MeasurementSet(project);
        measurementSet2.setQualityLevel(au.csiro.casda.entity.observation.QualityLevel.NOT_VALIDATED);
        measurementSet2.setProject(project);
        observation.addMeasurementSet(measurementSet);
        observation.addMeasurementSet(measurementSet2);

        when(mockObservationRepository.findBySbid(sbid)).thenReturn(observation);

        observationService.releaseProjectBlock("ABC123", sbid, DateTime.now());
    }

    @Test
    public void testReleaseObservation() throws Exception
    {
        Observation observation = spy(new Observation());
        when(observation.getSbid()).thenReturn(123);
        when(observation.getDepositStateType()).thenReturn(Type.DEPOSITED);

        Project project = new Project("ABC123");
        Project project2 = new Project("ABC111");
        Catalogue catalogue = new Catalogue(CatalogueType.CONTINUUM_COMPONENT);
        catalogue.setId(122L);
        catalogue.setProject(project);
        catalogue.setQualityLevel(au.csiro.casda.entity.observation.QualityLevel.GOOD);
        Catalogue catalogue2 = new Catalogue(CatalogueType.CONTINUUM_ISLAND);
        catalogue2.setId(124L);
        catalogue2.setQualityLevel(au.csiro.casda.entity.observation.QualityLevel.NOT_VALIDATED);
        catalogue2.setProject(project2);
        Catalogue catalogue3 = new Catalogue(CatalogueType.POLARISATION_COMPONENT);
        catalogue3.setId(125L);
        catalogue3.setProject(project);
        catalogue3.setQualityLevel(au.csiro.casda.entity.observation.QualityLevel.BAD);
        observation.addCatalogue(catalogue);
        observation.addCatalogue(catalogue2);
        observation.addCatalogue(catalogue3);

        MeasurementSet measurementSet = new MeasurementSet(project);
        measurementSet.setQualityLevel(au.csiro.casda.entity.observation.QualityLevel.GOOD);
        MeasurementSet measurementSet2 = new MeasurementSet(project2);
        measurementSet2.setQualityLevel(au.csiro.casda.entity.observation.QualityLevel.NOT_VALIDATED);
        MeasurementSet measurementSet3 = new MeasurementSet(project);
        measurementSet3.setQualityLevel(au.csiro.casda.entity.observation.QualityLevel.GOOD);
        observation.addMeasurementSet(measurementSet);
        observation.addMeasurementSet(measurementSet2);
        observation.addMeasurementSet(measurementSet3);

        ImageCube imageCube = new ImageCube(project);
        imageCube.setQualityLevel(au.csiro.casda.entity.observation.QualityLevel.GOOD);
        ImageCube imageCube2 = new ImageCube(project2);
        imageCube2.setQualityLevel(au.csiro.casda.entity.observation.QualityLevel.NOT_VALIDATED);
        ImageCube imageCube3 = new ImageCube(project);
        imageCube3.setQualityLevel(au.csiro.casda.entity.observation.QualityLevel.UNCERTAIN);
        observation.addImageCube(imageCube);
        observation.addImageCube(imageCube2);
        observation.addImageCube(imageCube3);

        DateTime releaseDate = DateTime.now(DateTimeZone.UTC).minusHours(2);
        when(mockObservationRepository.findBySbid(Integer.valueOf(123))).thenReturn(observation);
        MessageDTO result = observationService.releaseProjectBlock("ABC123", Integer.valueOf(123), releaseDate);
        assertEquals(releaseDate, catalogue.getReleasedDate());
        assertNull(catalogue2.getReleasedDate());
        assertEquals(releaseDate, catalogue3.getReleasedDate());
        assertEquals(releaseDate, imageCube.getReleasedDate());
        assertNull(imageCube2.getReleasedDate());
        assertEquals(releaseDate, imageCube3.getReleasedDate());
        assertEquals(releaseDate, measurementSet.getReleasedDate());
        assertNull(measurementSet2.getReleasedDate());
        assertEquals(releaseDate, measurementSet3.getReleasedDate());

        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Long> idCaptor = ArgumentCaptor.forClass(Long.class);
        verify(jdbcTemplate, times(2)).update(queryCaptor.capture(), eq(releaseDate.toDate()), any(Date.class),
                idCaptor.capture());

        for (int i = 0; i < idCaptor.getAllValues().size(); i++)
        {
            Long idValue = idCaptor.getAllValues().get(i);
            String query = queryCaptor.getAllValues().get(i);
            if (idValue == 122L)
            {
                assertThat(query, startsWith("UPDATE casda.continuum_component"));
            }
            else if (idValue == 125L)
            {
                assertThat(query, startsWith("UPDATE casda.polarisation_component"));
            }
            else
            {
                fail("Catalogue id " + idValue + " shouldn't be updated in statement beginning with " + query);
            }
        }

        assertEquals(MessageCode.SUCCESS, result.getMessageCode());
    }

    @Test
    public void testGetActiveQualityFlags() throws Exception
    {
        List<QualityFlag> qualityFlags = new ArrayList<>();
        QualityFlag flag = new QualityFlag();
        flag.setId(1L);
        flag.setActive(true);
        flag.setCode("code1");
        flag.setLabel("label1");
        flag.setDisplayOrder(1);

        QualityFlag flag2 = new QualityFlag();
        flag2.setId(2L);
        flag2.setActive(true);
        flag2.setCode("code2");
        flag2.setLabel("label2");
        flag2.setDisplayOrder(2);

        qualityFlags.add(flag);
        qualityFlags.add(flag2);

        when(mockQualityService.findOrderedActiveQualityFlags()).thenReturn(qualityFlags);

        List<QualityFlagDTO> flags = observationService.getActiveQualityFlags();
        assertEquals(2, flags.size());
        for (QualityFlagDTO qualityFlag : flags)
        {
            if (qualityFlag.getId() == 1L)
            {
                assertEquals("code1", qualityFlag.getCode());
                assertEquals("label1", qualityFlag.getLabel());
            }
            else
            {
                assertEquals(2L, qualityFlag.getId());
                assertEquals("code2", qualityFlag.getCode());
                assertEquals("label2", qualityFlag.getLabel());
            }
        }
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetObservationProjectDataProductsNoObservation() throws Exception
    {
        when(mockObservationRepository.findBySbid(123)).thenReturn(null);
        observationService.getObservationProjectDataProducts(123, "ABC213");
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testGetObservationProjectDataProductsObservationProject() throws Exception
    {
        Observation observation = mock(Observation.class);
        Set<Project> projects = new HashSet<>();
        Project project = new Project("ABC124");
        projects.add(project);
        when(observation.getProjects()).thenReturn(projects);
        when(mockObservationRepository.findBySbid(123)).thenReturn(observation);
        observationService.getObservationProjectDataProducts(123, "ABC213");
    }

    @Test
    public void testGetObservationProjectDataProducts() throws Exception
    {
        DateTime now = DateTime.now();

        Observation observation = mock(Observation.class);
        when(observation.getId()).thenReturn(155L);
        when(observation.getObsStart()).thenReturn(now);
        when(observation.getSbid()).thenReturn(12345);

        Set<Project> projects = new HashSet<>();
        Project project = new Project("ABC123");
        project.setPrincipalFirstName("Bob");
        project.setPrincipalLastName("Smith");
        project.setId(12L);
        projects.add(project);
        Project project2 = new Project("ABC124");
        project2.setId(15L);

        projects.add(project);
        projects.add(project2);

        when(observation.getProjects()).thenReturn(projects);
        when(mockObservationRepository.findBySbid(12345)).thenReturn(observation);
        when(mockQualityService.getQualityFlagCodesForObservationProject(155L, 12L)).thenReturn(
                Arrays.asList("a", "b", "c"));

        ValidationNote validationNoteOne = new ValidationNote();
        validationNoteOne.setContent("content one");
        validationNoteOne.setCreated(DateTime.now(DateTimeZone.UTC));
        validationNoteOne.setId(12L);
        validationNoteOne.setPersonId("person123");
        validationNoteOne.setPersonName("Person Name");
        validationNoteOne.setProjectId(12L);
        validationNoteOne.setSbid(12345);

        when(mockValidationNoteRepository.findBySbidAndProjectIdOrderByCreatedAsc(12345, 12L)).thenReturn(
                Arrays.asList(validationNoteOne));

        Catalogue catalogue = spy(new Catalogue(CatalogueType.CONTINUUM_COMPONENT));
        catalogue.setId(12L);
        catalogue.setProject(project);
        doReturn("catalogue-file-id").when(catalogue).getFileId();
        Catalogue catalogue2 = new Catalogue(CatalogueType.CONTINUUM_ISLAND);
        catalogue2.setId(14L);
        catalogue2.setProject(project2);
        List<Catalogue> catalogues = new ArrayList<>();
        catalogues.add(catalogue);
        catalogues.add(catalogue2);

        when(observation.getCatalogues()).thenReturn(catalogues);

        ObservationProjectDataProductsDTO dataProducts =
                observationService.getObservationProjectDataProducts(12345, "ABC123");
        assertEquals(now.getMillis(), dataProducts.getObsStartDateMillis().longValue());
        assertEquals(1, dataProducts.getDataProducts().size());
        assertEquals("ABC123", dataProducts.getOpalCode());
        assertEquals("Bob", dataProducts.getPrincipalFirstName());
        assertEquals("Smith", dataProducts.getPrincipalLastName());
        assertEquals(12345, dataProducts.getSbid().intValue());
        DataProductDTO dataProduct = dataProducts.getDataProducts().get(0);
        assertEquals(DataProductType.CATALOGUE, dataProduct.getType());
        assertEquals(12L, dataProduct.getId());
        assertEquals(CatalogueType.CONTINUUM_COMPONENT, dataProduct.getCatalogueType());
        assertEquals(QualityLevel.NOT_VALIDATED, dataProduct.getQualityLevel());
        assertEquals("catalogue-file-id", dataProduct.getIdentifier());
        assertThat(dataProducts.getQualityFlagCodes(), containsInAnyOrder("a", "b", "c"));
        assertEquals(1, dataProducts.getValidationNotes().size());
        assertEquals("content one", dataProducts.getValidationNotes().get(0).getContent());

    }

    @Test(expected = ResourceNotFoundException.class)
    public void testUpdateObservationProjectDataProductsNoObservation() throws Exception
    {
        ObservationProjectDataProductsDTO details =
                new ObservationProjectDataProductsDTO(445, "ABC123", DateTime.now(DateTimeZone.UTC), "Bob", "Smith");
        when(mockObservationRepository.findBySbid(445)).thenReturn(null);
        observationService.updateObservationProjectDataProducts(details);
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testUpdateObservationProjectDataProductsNoProject() throws Exception
    {
        Observation observation = spy(new Observation());
        observation.setSbid(445);
        Set<Project> projects = new HashSet<>();
        Project project = new Project("ABC111");
        projects.add(project);
        when(observation.getProjects()).thenReturn(projects);
        ObservationProjectDataProductsDTO details =
                new ObservationProjectDataProductsDTO(445, "ABC123", DateTime.now(DateTimeZone.UTC), "Bob", "Smith");
        when(mockObservationRepository.findBySbid(445)).thenReturn(observation);
        observationService.updateObservationProjectDataProducts(details);
    }

    @Test
    public void testUpdateObservationProjectDataProductsNoDataProductsOrFlags() throws Exception
    {
        Observation observation = spy(new Observation());
        observation.setSbid(445);
        Set<Project> projects = new HashSet<>();
        Project project = new Project("ABC123");
        projects.add(project);
        when(observation.getProjects()).thenReturn(projects);
        ObservationProjectDataProductsDTO details =
                new ObservationProjectDataProductsDTO(445, "ABC123", DateTime.now(DateTimeZone.UTC), "Bob", "Smith");
        when(mockObservationRepository.findBySbid(445)).thenReturn(observation);
        observationService.updateObservationProjectDataProducts(details);

        verify(mockQualityService, never()).updateQualityLevelForCatalogueRows(any(DataProductDTO.class));
        verify(mockQualityService, never()).updateQualityFlagsForObservationProject(any(Long.class), any(Long.class),
                any());
        verify(mockObservationRepository, never()).save(any(Observation.class));
    }

    @Test
    public void testUpdateObservationProjectDataProductsFlags() throws Exception
    {
        Observation observation = spy(new Observation());
        observation.setSbid(445);
        observation.setId(182L);
        Set<Project> projects = new HashSet<>();
        Project project = new Project("ABC123");
        project.setId(167L);
        projects.add(project);
        when(observation.getProjects()).thenReturn(projects);
        ObservationProjectDataProductsDTO details =
                new ObservationProjectDataProductsDTO(445, "ABC123", DateTime.now(DateTimeZone.UTC), "Bob", "Smith");

        details.addQualityFlagCode("PIQ");
        details.addQualityFlagCode("AB");

        when(mockObservationRepository.findBySbid(445)).thenReturn(observation);
        observationService.updateObservationProjectDataProducts(details);

        verify(mockQualityService, never()).updateQualityLevelForCatalogueRows(any(DataProductDTO.class));
        verify(mockQualityService).updateQualityFlagsForObservationProject(eq(182L), eq(167L),
                eq(details.getQualityFlagCodes()));
        verify(mockObservationRepository, never()).save(any(Observation.class));
    }

    @Test
    public void testUpdateObservationProjectDataProductsNoMatchingDataProduct() throws Exception
    {
        for (DataProductType type : DataProductType.values())
        {
            Observation observation = spy(new Observation());
            observation.setSbid(445);
            observation.setId(182L);
            Set<Project> projects = new HashSet<>();
            Project project = new Project("ABC123");
            project.setId(167L);
            projects.add(project);
            when(observation.getProjects()).thenReturn(projects);
            ObservationProjectDataProductsDTO details =
                    new ObservationProjectDataProductsDTO(445, "ABC123", DateTime.now(DateTimeZone.UTC), "Bob", "Smith");

            details.addQualityFlagCode("PIQ");
            details.addQualityFlagCode("AB");

            DataProductDTO dataProduct =
                    new DataProductDTO(18L, type, null, "measurement-set-file-id", QualityLevel.GOOD);
            details.addDataProduct(dataProduct);

            when(mockObservationRepository.findBySbid(445)).thenReturn(observation);
            try
            {
                observationService.updateObservationProjectDataProducts(details);
                fail();
            }
            catch (ResourceNotFoundException e)
            {
                verify(mockQualityService, never()).updateQualityLevelForCatalogueRows(any(DataProductDTO.class));
                verify(mockQualityService, never()).updateQualityFlagsForObservationProject(eq(182L), eq(167L),
                        eq(details.getQualityFlagCodes()));
                verify(mockObservationRepository, never()).save(any(Observation.class));
            }
        }
    }

    @Test
    public void testUpdateObservationProjectDataProductsMatchingMeasurementSet() throws Exception
    {
        Observation observation = spy(new Observation());
        observation.setSbid(445);
        observation.setId(182L);
        Set<Project> projects = new HashSet<>();
        Project project = new Project("ABC123");
        project.setId(167L);
        projects.add(project);
        when(observation.getProjects()).thenReturn(projects);
        MeasurementSet measurementSet = new MeasurementSet();
        measurementSet.setId(18L);
        MeasurementSet measurementSet2 = new MeasurementSet();
        measurementSet2.setId(19L);
        MeasurementSet measurementSet3 = new MeasurementSet();
        measurementSet3.setId(20L);
        observation.addMeasurementSet(measurementSet);
        observation.addMeasurementSet(measurementSet2);
        observation.addMeasurementSet(measurementSet3);

        ObservationProjectDataProductsDTO details =
                new ObservationProjectDataProductsDTO(445, "ABC123", DateTime.now(DateTimeZone.UTC), "Bob", "Smith");

        details.addQualityFlagCode("PIQ");
        details.addQualityFlagCode("AB");

        DataProductDTO measurementSetA =
                new DataProductDTO(18L, DataProductType.MEASUREMENT_SET, null, "measurement-set-file-id",
                        QualityLevel.GOOD);
        DataProductDTO measurementSetB =
                new DataProductDTO(19L, DataProductType.MEASUREMENT_SET, null, "measurement-set-file-id",
                        QualityLevel.UNCERTAIN);
        details.addDataProduct(measurementSetA);
        details.addDataProduct(measurementSetB);

        when(mockObservationRepository.findBySbid(445)).thenReturn(observation);
        observationService.updateObservationProjectDataProducts(details);

        verify(mockQualityService, never()).updateQualityLevelForCatalogueRows(any(DataProductDTO.class));
        verify(mockQualityService).updateQualityFlagsForObservationProject(eq(182L), eq(167L),
                eq(details.getQualityFlagCodes()));
        verify(mockObservationRepository).save(eq(observation));

        assertEquals(au.csiro.casda.entity.observation.QualityLevel.GOOD, measurementSet.getQualityLevel());
        assertEquals(au.csiro.casda.entity.observation.QualityLevel.UNCERTAIN, measurementSet2.getQualityLevel());
        assertEquals(au.csiro.casda.entity.observation.QualityLevel.NOT_VALIDATED, measurementSet3.getQualityLevel());
    }

    @Test
    public void testUpdateObservationProjectDataProductsMatchingImageCube() throws Exception
    {
        Observation observation = spy(new Observation());
        observation.setSbid(445);
        observation.setId(182L);
        Set<Project> projects = new HashSet<>();
        Project project = new Project("ABC123");
        project.setId(167L);
        projects.add(project);
        when(observation.getProjects()).thenReturn(projects);
        ImageCube imageCube = new ImageCube();
        imageCube.setId(18L);
        ImageCube imageCube2 = new ImageCube();
        imageCube2.setId(19L);
        ImageCube imageCube3 = new ImageCube();
        imageCube3.setId(20L);
        observation.addImageCube(imageCube);
        observation.addImageCube(imageCube2);
        observation.addImageCube(imageCube3);

        ObservationProjectDataProductsDTO details =
                new ObservationProjectDataProductsDTO(445, "ABC123", DateTime.now(DateTimeZone.UTC), "Bob", "Smith");

        details.addQualityFlagCode("PIQ");
        details.addQualityFlagCode("AB");

        DataProductDTO measurementSetA =
                new DataProductDTO(18L, DataProductType.IMAGE_CUBE, null, "image-cube-file-id", QualityLevel.BAD);
        DataProductDTO measurementSetB =
                new DataProductDTO(19L, DataProductType.IMAGE_CUBE, null, "image-cube-file-id", QualityLevel.UNCERTAIN);

        details.addDataProduct(measurementSetA);
        details.addDataProduct(measurementSetB);

        when(mockObservationRepository.findBySbid(445)).thenReturn(observation);
        observationService.updateObservationProjectDataProducts(details);

        verify(mockQualityService, never()).updateQualityLevelForCatalogueRows(any(DataProductDTO.class));
        verify(mockQualityService).updateQualityFlagsForObservationProject(eq(182L), eq(167L),
                eq(details.getQualityFlagCodes()));
        verify(mockObservationRepository).save(eq(observation));

        assertEquals(au.csiro.casda.entity.observation.QualityLevel.BAD, imageCube.getQualityLevel());
        assertEquals(au.csiro.casda.entity.observation.QualityLevel.UNCERTAIN, imageCube2.getQualityLevel());
        assertEquals(au.csiro.casda.entity.observation.QualityLevel.NOT_VALIDATED, imageCube3.getQualityLevel());
    }

    @Test
    public void testUpdateObservationProjectDataProductsMatchingCatalogue() throws Exception
    {
        Observation observation = spy(new Observation());
        observation.setSbid(445);
        observation.setId(182L);
        Set<Project> projects = new HashSet<>();
        Project project = new Project("ABC123");
        project.setId(167L);
        projects.add(project);
        when(observation.getProjects()).thenReturn(projects);
        Catalogue catalogue = new Catalogue();
        catalogue.setId(18L);
        Catalogue catalogue2 = new Catalogue();
        catalogue2.setId(19L);
        Catalogue catalogue3 = new Catalogue();
        catalogue3.setId(20L);
        observation.addCatalogue(catalogue);
        observation.addCatalogue(catalogue2);
        observation.addCatalogue(catalogue3);

        ObservationProjectDataProductsDTO details =
                new ObservationProjectDataProductsDTO(445, "ABC123", DateTime.now(DateTimeZone.UTC), "Bob", "Smith");
        details.addQualityFlagCode("PIQ");
        details.addQualityFlagCode("AB");

        DataProductDTO measurementSetA =
                new DataProductDTO(18L, DataProductType.CATALOGUE, CatalogueType.CONTINUUM_COMPONENT,
                        "catalogue-file-id", QualityLevel.GOOD);
        DataProductDTO measurementSetB =
                new DataProductDTO(19L, DataProductType.CATALOGUE, CatalogueType.POLARISATION_COMPONENT,
                        "catalogue-file-id", QualityLevel.BAD);
        DataProductDTO measurementSetC =
                new DataProductDTO(20L, DataProductType.CATALOGUE, CatalogueType.SPECTRAL_LINE_ABSORPTION, 
                		"catalogue-file-id", QualityLevel.UNCERTAIN);

        details.addDataProduct(measurementSetA);
        details.addDataProduct(measurementSetB);
        details.addDataProduct(measurementSetC);

        when(mockObservationRepository.findBySbid(445)).thenReturn(observation);
        observationService.updateObservationProjectDataProducts(details);

        verify(mockQualityService).updateQualityLevelForCatalogueRows(eq(measurementSetA));
        verify(mockQualityService).updateQualityLevelForCatalogueRows(eq(measurementSetB));
        verify(mockQualityService).updateQualityLevelForCatalogueRows(eq(measurementSetC));
        verify(mockQualityService).updateQualityFlagsForObservationProject(eq(182L), eq(167L),
                eq(details.getQualityFlagCodes()));
        verify(mockObservationRepository).save(eq(observation));

        assertEquals(au.csiro.casda.entity.observation.QualityLevel.GOOD, catalogue.getQualityLevel());
        assertEquals(au.csiro.casda.entity.observation.QualityLevel.BAD, catalogue2.getQualityLevel());
        assertEquals(au.csiro.casda.entity.observation.QualityLevel.UNCERTAIN, catalogue3.getQualityLevel());
    }

    @Test
    public void testUpdateObservationProjectDataProductsMatchingMix() throws Exception
    {
        Observation observation = spy(new Observation());
        observation.setSbid(445);
        observation.setId(182L);
        Set<Project> projects = new HashSet<>();
        Project project = new Project("ABC123");
        project.setId(167L);
        projects.add(project);
        when(observation.getProjects()).thenReturn(projects);
        Catalogue catalogue = new Catalogue();
        catalogue.setId(18L);
        MeasurementSet measurementSet = new MeasurementSet();
        measurementSet.setId(19L);
        ImageCube imageCube = new ImageCube();
        imageCube.setId(20L);
        observation.addCatalogue(catalogue);
        observation.addMeasurementSet(measurementSet);
        observation.addImageCube(imageCube);

        ObservationProjectDataProductsDTO details =
                new ObservationProjectDataProductsDTO(445, "ABC123", DateTime.now(DateTimeZone.UTC), "Bob", "Smith");

        details.addQualityFlagCode("PIQ");
        details.addQualityFlagCode("AB");

        DataProductDTO measurementSetA =
                new DataProductDTO(19L, DataProductType.MEASUREMENT_SET, null, "catalogue-file-id", QualityLevel.GOOD);
        DataProductDTO catalogueA =
                new DataProductDTO(18L, DataProductType.CATALOGUE, CatalogueType.POLARISATION_COMPONENT,
                        "catalogue-file-id", QualityLevel.BAD);
        DataProductDTO imageCubeA =
                new DataProductDTO(20L, DataProductType.IMAGE_CUBE, null, "catalogue-file-id", QualityLevel.UNCERTAIN);
        details.addDataProduct(measurementSetA);
        details.addDataProduct(catalogueA);
        details.addDataProduct(imageCubeA);

        when(mockObservationRepository.findBySbid(445)).thenReturn(observation);
        observationService.updateObservationProjectDataProducts(details);

        verify(mockQualityService).updateQualityLevelForCatalogueRows(eq(catalogueA));
        verify(mockQualityService).updateQualityFlagsForObservationProject(eq(182L), eq(167L),
                eq(details.getQualityFlagCodes()));
        verify(mockObservationRepository).save(eq(observation));

        assertEquals(au.csiro.casda.entity.observation.QualityLevel.BAD, catalogue.getQualityLevel());
        assertEquals(au.csiro.casda.entity.observation.QualityLevel.GOOD, measurementSet.getQualityLevel());
        assertEquals(au.csiro.casda.entity.observation.QualityLevel.UNCERTAIN, imageCube.getQualityLevel());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testAddValidationNoteNoMatchingObservation() throws Exception
    {
        when(mockObservationRepository.findBySbid(445)).thenReturn(null);
        observationService.addValidationNote(445, "ABC123", new ValidationNoteDTO());
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testAddValidationNoteNoMatchingProject() throws Exception
    {
        Observation observation = spy(new Observation());
        observation.setSbid(445);
        observation.setId(182L);
        Set<Project> projects = new HashSet<>();
        Project project = new Project("ABC123");
        project.setId(167L);
        projects.add(project);
        when(observation.getProjects()).thenReturn(projects);

        when(mockObservationRepository.findBySbid(445)).thenReturn(observation);
        observationService.addValidationNote(445, "AAA111", new ValidationNoteDTO());
    }

    @Test
    public void testAddValidationNote() throws Exception
    {
        Observation observation = spy(new Observation());
        observation.setSbid(445);
        observation.setId(182L);
        Set<Project> projects = new HashSet<>();
        Project project = new Project("ABC123");
        project.setId(167L);
        projects.add(project);
        when(observation.getProjects()).thenReturn(projects);
        
        long newNoteId = 13154;

        when(mockObservationRepository.findBySbid(445)).thenReturn(observation);
        ArgumentCaptor<ValidationNote> validationNoteCaptor = ArgumentCaptor.forClass(ValidationNote.class);
        when(mockValidationNoteRepository.save(validationNoteCaptor.capture())).then(new Answer<ValidationNote>()
        {
            @Override
            public ValidationNote answer(InvocationOnMock invocation) throws Throwable
            {
                ValidationNote argument = (ValidationNote) invocation.getArguments()[0];
                argument.setId(newNoteId);
                return argument;
            }
        });

        ValidationNoteDTO validationNoteDto = new ValidationNoteDTO();
        validationNoteDto.setContent("content");
        validationNoteDto.setUserId("user111");
        validationNoteDto.setUserName("User Name");
        validationNoteDto.setCreatedDate(DateTime.now().minusDays(2).getMillis());
        observationService.addValidationNote(445, "ABC123", validationNoteDto);

        verify(mockValidationNoteRepository, times(1)).save(any(ValidationNote.class));
        ValidationNote savedNote = validationNoteCaptor.getValue();
        assertEquals(validationNoteDto.getContent(), savedNote.getContent());
        assertEquals(validationNoteDto.getUserId(), savedNote.getPersonId());
        assertEquals(validationNoteDto.getUserName(), savedNote.getPersonName());
        assertEquals(167L, savedNote.getProjectId().longValue());
        assertEquals(445, savedNote.getSbid().intValue());
        assertEquals(validationNoteDto.getCreatedDate().longValue(), savedNote.getCreated().getMillis());
    }

    @Test
    public void testAddValidationNoteSetsCreatedTime() throws Exception
    {
        Observation observation = spy(new Observation());
        observation.setSbid(445);
        observation.setId(182L);
        Set<Project> projects = new HashSet<>();
        Project project = new Project("ABC123");
        project.setId(167L);
        projects.add(project);
        when(observation.getProjects()).thenReturn(projects);
        
        long newNoteId = 1214;

        when(mockObservationRepository.findBySbid(445)).thenReturn(observation);
        ArgumentCaptor<ValidationNote> validationNoteCaptor = ArgumentCaptor.forClass(ValidationNote.class);
        when(mockValidationNoteRepository.save(validationNoteCaptor.capture())).then(new Answer<ValidationNote>()
        {
            @Override
            public ValidationNote answer(InvocationOnMock invocation) throws Throwable
            {
                ValidationNote argument = (ValidationNote) invocation.getArguments()[0];
                argument.setId(newNoteId);
                return argument;
            }
        });

        ValidationNoteDTO validationNoteDto = new ValidationNoteDTO();
        validationNoteDto.setContent("content one");
        validationNoteDto.setUserId("user123");
        validationNoteDto.setUserName("User Name1");
        ValidationNoteDTO response = observationService.addValidationNote(445, "ABC123", validationNoteDto);

        verify(mockValidationNoteRepository, times(1)).save(any(ValidationNote.class));
        ValidationNote savedNote = validationNoteCaptor.getValue();
        assertEquals(validationNoteDto.getContent(), savedNote.getContent());
        assertEquals(validationNoteDto.getUserId(), savedNote.getPersonId());
        assertEquals(validationNoteDto.getUserName(), savedNote.getPersonName());
        assertEquals(167L, savedNote.getProjectId().longValue());
        assertEquals(445, savedNote.getSbid().intValue());
        // the validation note didn't have the created time set, so make sure it was set to about now
        assertEquals(System.currentTimeMillis(), savedNote.getCreated().getMillis(), 100);
        assertEquals(response.getContent(), savedNote.getContent());
        assertEquals(newNoteId, savedNote.getId().longValue());
    }
}
