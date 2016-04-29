package au.csiro.casda.deposit.jpa;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.test.context.ContextConfiguration;

import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.datadeposit.DepositedDepositState;
import au.csiro.casda.deposit.TestAppConfig;
import au.csiro.casda.dto.ObservationProjectDataProductsDTO;
import au.csiro.casda.entity.observation.Catalogue;
import au.csiro.casda.entity.observation.CatalogueType;
import au.csiro.casda.entity.observation.ImageCube;
import au.csiro.casda.entity.observation.MeasurementSet;
import au.csiro.casda.entity.observation.Observation;
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
 * Test case for DepositManagerObservationRepository.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
@ContextConfiguration(classes = { TestAppConfig.class })
public class ObservationRepositoryTest extends AbstractPersistenceTest
{
    @Autowired
    private ObservationRepository depositManagerObservationRepository;

    private TestProjectRepository projectRepository;

    private TestObservationRepository observationRepository;

    protected DepositStateFactory depositStateFactory;

    public ObservationRepositoryTest() throws Exception
    {
        super();
    }

    /**
     * Template method subclasses can override to initialise a Repository using the instance's EntityManager.
     * 
     * @param rfs
     *            a RepositoryFactorySupport instance
     */
    protected void initializeRepositories(RepositoryFactorySupport rfs)
    {
        projectRepository = rfs.getRepository(TestProjectRepository.class);
        observationRepository = rfs.getRepository(TestObservationRepository.class);
    }

    @Test
    public void testFindProjectBlocksWithReleasedImageCubesAfterDateForUnknownProject()
    {
        List<ObservationProjectDataProductsDTO> results =
                depositManagerObservationRepository.findProjectBlocksWithReleasedImageCubesAfterDate(
                        RandomStringUtils.randomAlphabetic(20), DateTime.now());
        assertThat(results, empty());
    }

    @Test
    public void testFindProjectBlocksWithReleasedCataloguesAfterDateForUnknownProject()
    {
        List<ObservationProjectDataProductsDTO> results =
                depositManagerObservationRepository.findProjectBlocksWithReleasedCataloguesAfterDate(
                        RandomStringUtils.randomAlphabetic(20), DateTime.now());
        assertThat(results, empty());
    }

    @Test
    public void testFindProjectBlocksWithReleasedMeasurementSetsAfterDateForUnknownProject()
    {
        List<ObservationProjectDataProductsDTO> results =
                depositManagerObservationRepository.findProjectBlocksWithReleasedMeasurementSetsAfterDate(
                        RandomStringUtils.randomAlphabetic(20), DateTime.now());
        assertThat(results, empty());
    }

    @Test
    public void testFindProjectBlocksWithReleasedImageCubesAfterDateForKnownProject()
    {
        String opalCode = "AS007";
        Project project = new Project(opalCode);
        projectRepository.save(project);

        commit();

        assertThat(observationRepository.count(), is(0L));
        assertThat(projectRepository.count(), is(1L));

        List<ObservationProjectDataProductsDTO> results =
                depositManagerObservationRepository.findProjectBlocksWithReleasedImageCubesAfterDate(opalCode,
                        DateTime.now());
        assertThat(results, empty());
    }

    @Test
    public void testFindProjectBlocksWithReleasedCataloguesAfterDateForKnownProject()
    {
        String opalCode = "AS007";
        Project project = new Project(opalCode);
        projectRepository.save(project);

        commit();

        assertThat(observationRepository.count(), is(0L));
        assertThat(projectRepository.count(), is(1L));

        List<ObservationProjectDataProductsDTO> results =
                depositManagerObservationRepository.findProjectBlocksWithReleasedCataloguesAfterDate(opalCode,
                        DateTime.now());
        assertThat(results, empty());
    }

    @Test
    public void testFindProjectBlocksWithReleasedMeasurementSetsAfterDateForKnownProject()
    {
        String opalCode = "AS007";
        Project project = new Project(opalCode);
        projectRepository.save(project);

        commit();

        assertThat(observationRepository.count(), is(0L));
        assertThat(projectRepository.count(), is(1L));

        List<ObservationProjectDataProductsDTO> results =
                depositManagerObservationRepository.findProjectBlocksWithReleasedMeasurementSetsAfterDate(opalCode,
                        DateTime.now());
        assertThat(results, empty());
    }

    @Test
    public void testFindProjectBlocksWithReleasedImageCubesForKnownProject()
    {
        String opalCode = "AS007";
        Integer sbid = RandomUtils.nextInt(1, Integer.MAX_VALUE);
        DateTime releaseDate = DateTime.now().minusDays(RandomUtils.nextInt(9, 365) + 1);

        Project project = new Project(opalCode);
        Observation observation = ProjectRepositoryTest.createDefaultObservation(sbid);
        ImageCube imageCube = ProjectRepositoryTest.createDefaultImageCube(project);
        imageCube.setReleasedDate(releaseDate);
        observation.addImageCube(imageCube);

        observationRepository.save(observation);

        commit();

        assertThat(observationRepository.count(), is(1L));
        assertThat(projectRepository.count(), is(1L));

        List<ObservationProjectDataProductsDTO> results = null;

        results =
                depositManagerObservationRepository.findProjectBlocksWithReleasedImageCubesAfterDate(opalCode,
                        releaseDate.minusDays(1));
        assertThat(results.size(), is(1));
        assertThat(results.get(0), is(new ObservationProjectDataProductsDTO(sbid, opalCode, observation.getObsStart(),
                project.getPrincipalFirstName(), project.getPrincipalLastName())));

        results =
                depositManagerObservationRepository.findProjectBlocksWithReleasedImageCubesAfterDate(opalCode,
                        releaseDate);
        assertThat(results, is(empty()));

        results =
                depositManagerObservationRepository.findProjectBlocksWithReleasedImageCubesAfterDate(opalCode,
                        releaseDate.plusDays(1));
        assertThat(results, is(empty()));
    }

    @Test
    public void testFindProjectBlocksWithReleasedMeasurementSetsForKnownProject()
    {
        String opalCode = "AS007";
        Integer sbid = RandomUtils.nextInt(1, Integer.MAX_VALUE);
        DateTime releaseDate = DateTime.now().minusDays(RandomUtils.nextInt(9, 365) + 1);

        Project project = new Project(opalCode);
        Observation observation = ProjectRepositoryTest.createDefaultObservation(sbid);
        MeasurementSet measurementSet = ProjectRepositoryTest.createDefaultMeasurementSet(project);
        measurementSet.setReleasedDate(releaseDate);
        observation.addMeasurementSet(measurementSet);

        observationRepository.save(observation);

        commit();

        assertThat(observationRepository.count(), is(1L));
        assertThat(projectRepository.count(), is(1L));

        List<ObservationProjectDataProductsDTO> results = null;

        results =
                depositManagerObservationRepository.findProjectBlocksWithReleasedMeasurementSetsAfterDate(opalCode,
                        releaseDate.minusDays(1));
        assertThat(results.size(), is(1));
        assertThat(results.get(0), is(new ObservationProjectDataProductsDTO(sbid, opalCode, observation.getObsStart(),
                project.getPrincipalFirstName(), project.getPrincipalLastName())));

        results =
                depositManagerObservationRepository.findProjectBlocksWithReleasedMeasurementSetsAfterDate(opalCode,
                        releaseDate);
        assertThat(results, is(empty()));

        results =
                depositManagerObservationRepository.findProjectBlocksWithReleasedMeasurementSetsAfterDate(opalCode,
                        releaseDate.plusDays(1));
        assertThat(results, is(empty()));
    }

    @Test
    public void testFindProjectBlocksWithReleasedCataloguesForKnownProject()
    {
        String opalCode = "AS007";
        Integer sbid = RandomUtils.nextInt(1, Integer.MAX_VALUE);
        DateTime releaseDate = DateTime.now().minusDays(RandomUtils.nextInt(9, 365) + 1);

        Project project = new Project(opalCode);
        Observation observation = ProjectRepositoryTest.createDefaultObservation(sbid);
        Catalogue catalogue = ProjectRepositoryTest.createDefaultCatalogue(project, CatalogueType.CONTINUUM_ISLAND);
        catalogue.setReleasedDate(releaseDate);
        observation.addCatalogue(catalogue);

        observationRepository.save(observation);

        commit();

        assertThat(observationRepository.count(), is(1L));
        assertThat(projectRepository.count(), is(1L));

        List<ObservationProjectDataProductsDTO> results = null;

        results =
                depositManagerObservationRepository.findProjectBlocksWithReleasedCataloguesAfterDate(opalCode,
                        releaseDate.minusDays(1));
        assertThat(results.size(), is(1));
        assertThat(results.get(0), is(new ObservationProjectDataProductsDTO(sbid, opalCode, observation.getObsStart(),
                project.getPrincipalFirstName(), project.getPrincipalLastName())));

        results =
                depositManagerObservationRepository.findProjectBlocksWithReleasedCataloguesAfterDate(opalCode,
                        releaseDate);
        assertThat(results, is(empty()));

        results =
                depositManagerObservationRepository.findProjectBlocksWithReleasedCataloguesAfterDate(opalCode,
                        releaseDate.plusDays(1));
        assertThat(results, is(empty()));
    }

    @Test
    public void findProjectBlocksForReleasedImageCubesMultipleResults()
    {
        String opalCode = "AS007";
        Integer sbid1 = RandomUtils.nextInt(1, Integer.MAX_VALUE);
        Integer sbid2 = RandomUtils.nextInt(1, Integer.MAX_VALUE);
        DateTime releaseDate1 = DateTime.now().minusDays(RandomUtils.nextInt(9, 365) + 1);
        DateTime releaseDate2 = releaseDate1.minusDays(2);

        Project project = new Project(opalCode);
        Observation observation1 = ProjectRepositoryTest.createDefaultObservation(sbid1);
        ImageCube imageCube = ProjectRepositoryTest.createDefaultImageCube(project);
        imageCube.setReleasedDate(releaseDate1);
        observation1.addImageCube(imageCube);
        observation1.addCatalogue(ProjectRepositoryTest.createDefaultCatalogue(project,
                CatalogueType.CONTINUUM_COMPONENT));
        observation1.addMeasurementSet(ProjectRepositoryTest.createDefaultMeasurementSet(project));
        Observation observation2 = ProjectRepositoryTest.createDefaultObservation(sbid2);
        ImageCube imageCube2 = ProjectRepositoryTest.createDefaultImageCube(project);
        imageCube2.setReleasedDate(releaseDate2);
        observation2.addImageCube(imageCube2);

        Integer sbid3 = RandomUtils.nextInt(1, Integer.MAX_VALUE);
        Project project2 = new Project(opalCode + "1");
        Observation observation3 = ProjectRepositoryTest.createDefaultObservation(sbid3);
        ImageCube imageCube3 = ProjectRepositoryTest.createDefaultImageCube(project2);
        imageCube3.setReleasedDate(releaseDate1);
        observation3.addImageCube(imageCube3);

        observationRepository.save(observation1);
        observationRepository.save(observation2);
        observationRepository.save(observation3);

        commit();

        assertThat(observationRepository.count(), is(3L));
        assertThat(projectRepository.count(), is(2L));

        List<ObservationProjectDataProductsDTO> results = null;

        ObservationProjectDataProductsDTO one =
                new ObservationProjectDataProductsDTO(sbid1, opalCode, observation1.getObsStart(),
                        project.getPrincipalFirstName(), project.getPrincipalLastName());
        ObservationProjectDataProductsDTO two =
                new ObservationProjectDataProductsDTO(sbid2, opalCode, observation2.getObsStart(),
                        project.getPrincipalFirstName(), project.getPrincipalLastName());

        results =
                depositManagerObservationRepository.findProjectBlocksWithReleasedImageCubesAfterDate(opalCode,
                        releaseDate2.minusDays(1));
        assertThat(results.size(), is(2));
        assertThat(results, containsInAnyOrder(one, two));

        results =
                depositManagerObservationRepository.findProjectBlocksWithReleasedImageCubesAfterDate(opalCode,
                        releaseDate2);
        assertThat(results.size(), is(1));
        assertThat(results, containsInAnyOrder(one));

        results =
                depositManagerObservationRepository.findProjectBlocksWithReleasedImageCubesAfterDate(opalCode,
                        releaseDate1.minusDays(1));
        assertThat(results.size(), is(1));
        assertThat(results, containsInAnyOrder(one));

        results =
                depositManagerObservationRepository.findProjectBlocksWithReleasedImageCubesAfterDate(opalCode,
                        releaseDate1);
        assertThat(results, is(empty()));

        results =
                depositManagerObservationRepository.findProjectBlocksWithReleasedImageCubesAfterDate(opalCode,
                        releaseDate1.plusDays(1));
        assertThat(results, is(empty()));
    }

    @Test
    public void findProjectBlocksForReleasedMeasurementSetsMultipleResults()
    {
        String opalCode = "AS007";
        Integer sbid1 = RandomUtils.nextInt(1, Integer.MAX_VALUE);
        Integer sbid2 = RandomUtils.nextInt(1, Integer.MAX_VALUE);
        DateTime releaseDate1 = DateTime.now().minusDays(RandomUtils.nextInt(9, 365) + 1);
        DateTime releaseDate2 = releaseDate1.minusDays(2);

        Project project = new Project(opalCode);
        Observation observation1 = ProjectRepositoryTest.createDefaultObservation(sbid1);
        MeasurementSet measurementSet = ProjectRepositoryTest.createDefaultMeasurementSet(project);
        measurementSet.setReleasedDate(releaseDate1);
        observation1.addMeasurementSet(measurementSet);
        observation1.addCatalogue(ProjectRepositoryTest.createDefaultCatalogue(project,
                CatalogueType.CONTINUUM_COMPONENT));
        observation1.addImageCube(ProjectRepositoryTest.createDefaultImageCube(project));
        Observation observation2 = ProjectRepositoryTest.createDefaultObservation(sbid2);
        MeasurementSet measurementSet2 = ProjectRepositoryTest.createDefaultMeasurementSet(project);
        measurementSet2.setReleasedDate(releaseDate2);
        observation2.addMeasurementSet(measurementSet2);

        Integer sbid3 = RandomUtils.nextInt(1, Integer.MAX_VALUE);
        Project project2 = new Project(opalCode + "1");
        Observation observation3 = ProjectRepositoryTest.createDefaultObservation(sbid3);
        MeasurementSet measurementSet3 = ProjectRepositoryTest.createDefaultMeasurementSet(project2);
        measurementSet3.setReleasedDate(releaseDate1);
        observation3.addMeasurementSet(measurementSet3);

        observationRepository.save(observation1);
        observationRepository.save(observation2);
        observationRepository.save(observation3);

        commit();

        assertThat(observationRepository.count(), is(3L));
        assertThat(projectRepository.count(), is(2L));

        List<ObservationProjectDataProductsDTO> results = null;

        ObservationProjectDataProductsDTO one =
                new ObservationProjectDataProductsDTO(sbid1, opalCode, observation1.getObsStart(),
                        project.getPrincipalFirstName(), project.getPrincipalLastName());
        ObservationProjectDataProductsDTO two =
                new ObservationProjectDataProductsDTO(sbid2, opalCode, observation2.getObsStart(),
                        project.getPrincipalFirstName(), project.getPrincipalLastName());

        results =
                depositManagerObservationRepository.findProjectBlocksWithReleasedMeasurementSetsAfterDate(opalCode,
                        releaseDate2.minusDays(1));
        assertThat(results.size(), is(2));
        assertThat(results, containsInAnyOrder(one, two));

        results =
                depositManagerObservationRepository.findProjectBlocksWithReleasedMeasurementSetsAfterDate(opalCode,
                        releaseDate2);
        assertThat(results.size(), is(1));
        assertThat(results, containsInAnyOrder(one));

        results =
                depositManagerObservationRepository.findProjectBlocksWithReleasedMeasurementSetsAfterDate(opalCode,
                        releaseDate1.minusDays(1));
        assertThat(results.size(), is(1));
        assertThat(results, containsInAnyOrder(one));

        results =
                depositManagerObservationRepository.findProjectBlocksWithReleasedMeasurementSetsAfterDate(opalCode,
                        releaseDate1);
        assertThat(results, is(empty()));

        results =
                depositManagerObservationRepository.findProjectBlocksWithReleasedMeasurementSetsAfterDate(opalCode,
                        releaseDate1.plusDays(1));
        assertThat(results, is(empty()));
    }

    @Test
    public void findProjectBlocksForReleasedCataloguesMultipleResults()
    {
        String opalCode = "AS007";
        Integer sbid1 = RandomUtils.nextInt(1, Integer.MAX_VALUE);
        Integer sbid2 = RandomUtils.nextInt(1, Integer.MAX_VALUE);
        DateTime releaseDate1 = DateTime.now().minusDays(RandomUtils.nextInt(9, 365) + 1);
        DateTime releaseDate2 = releaseDate1.minusDays(2);

        Project project = new Project(opalCode);
        Observation observation1 = ProjectRepositoryTest.createDefaultObservation(sbid1);
        Catalogue catalogue = ProjectRepositoryTest.createDefaultCatalogue(project, CatalogueType.CONTINUUM_ISLAND);
        catalogue.setReleasedDate(releaseDate1);
        observation1.addCatalogue(catalogue);
        observation1.addImageCube(ProjectRepositoryTest.createDefaultImageCube(project));
        observation1.addMeasurementSet(ProjectRepositoryTest.createDefaultMeasurementSet(project));
        Observation observation2 = ProjectRepositoryTest.createDefaultObservation(sbid2);
        Catalogue catalogue2 = ProjectRepositoryTest.createDefaultCatalogue(project, CatalogueType.CONTINUUM_COMPONENT);
        catalogue2.setReleasedDate(releaseDate2);
        observation2.addCatalogue(catalogue2);

        Integer sbid3 = RandomUtils.nextInt(1, Integer.MAX_VALUE);
        Project project2 = new Project(opalCode + "1");
        Observation observation3 = ProjectRepositoryTest.createDefaultObservation(sbid3);
        Catalogue catalogue3 =
                ProjectRepositoryTest.createDefaultCatalogue(project2, CatalogueType.POLARISATION_COMPONENT);
        catalogue3.setReleasedDate(releaseDate1);
        observation3.addCatalogue(catalogue3);

        observationRepository.save(observation1);
        observationRepository.save(observation2);
        observationRepository.save(observation3);

        commit();

        assertThat(observationRepository.count(), is(3L));
        assertThat(projectRepository.count(), is(2L));

        List<ObservationProjectDataProductsDTO> results = null;

        ObservationProjectDataProductsDTO one =
                new ObservationProjectDataProductsDTO(sbid1, opalCode, observation1.getObsStart(),
                        project.getPrincipalFirstName(), project.getPrincipalLastName());
        ObservationProjectDataProductsDTO two =
                new ObservationProjectDataProductsDTO(sbid2, opalCode, observation2.getObsStart(),
                        project.getPrincipalFirstName(), project.getPrincipalLastName());

        results =
                depositManagerObservationRepository.findProjectBlocksWithReleasedCataloguesAfterDate(opalCode,
                        releaseDate2.minusDays(1));
        assertThat(results.size(), is(2));
        assertThat(results, containsInAnyOrder(one, two));

        results =
                depositManagerObservationRepository.findProjectBlocksWithReleasedCataloguesAfterDate(opalCode,
                        releaseDate2);
        assertThat(results.size(), is(1));
        assertThat(results, containsInAnyOrder(one));

        results =
                depositManagerObservationRepository.findProjectBlocksWithReleasedCataloguesAfterDate(opalCode,
                        releaseDate1.minusDays(1));
        assertThat(results.size(), is(1));
        assertThat(results, containsInAnyOrder(one));

        results =
                depositManagerObservationRepository.findProjectBlocksWithReleasedCataloguesAfterDate(opalCode,
                        releaseDate1);
        assertThat(results, is(empty()));

        results =
                depositManagerObservationRepository.findProjectBlocksWithReleasedCataloguesAfterDate(opalCode,
                        releaseDate1.plusDays(1));
        assertThat(results, is(empty()));
    }

    @Test
    public void testFindEarliestObservationStartDateForProject() throws Exception
    {
        String opalCode = "ABC123";
        Integer sbid = 123456798;

        Project project = new Project(opalCode);
        Observation observationOne = ProjectRepositoryTest.createDefaultObservation(sbid);
        observationOne.setObsStart(new DateTime(654321));
        observationOne.addImageCube(ProjectRepositoryTest.createDefaultImageCube(project));

        Observation observationTwo = ProjectRepositoryTest.createDefaultObservation(sbid + 1);
        observationTwo.setObsStart(new DateTime(123456));
        observationTwo.addImageCube(ProjectRepositoryTest.createDefaultImageCube(project));

        Observation observationThree = ProjectRepositoryTest.createDefaultObservation(sbid + 2);
        observationThree.setObsStart(new DateTime(123456));
        observationThree.addImageCube(ProjectRepositoryTest.createDefaultImageCube(project));

        Project project2 = new Project(opalCode + "1");
        Observation observationFour = ProjectRepositoryTest.createDefaultObservation(sbid + 3);
        observationFour.setObsStart(new DateTime(123123));
        observationFour.addImageCube(ProjectRepositoryTest.createDefaultImageCube(project2));

        observationRepository.save(observationOne);
        observationRepository.save(observationTwo);
        observationRepository.save(observationThree);
        observationRepository.save(observationFour);

        commit();

        assertThat(observationRepository.count(), is(4L));
        assertThat(projectRepository.count(), is(2L));

        DateTime result = depositManagerObservationRepository.findEarliestObservationStartDateForProject("ABC123");
        assertEquals(123456L, result.getMillis());
        DateTime result2 = depositManagerObservationRepository.findEarliestObservationStartDateForProject("ABC1231");
        assertEquals(123123L, result2.getMillis());
    }

    @Test
    public void testFindUnreleasedProjectBlocksImageCubes()
    {
        String opalCode = "ABC123";
        Integer sbid = 123456798;

        Project project = new Project(opalCode);
        Observation observationOne = ProjectRepositoryTest.createDefaultObservation(sbid);
        setObservationDeposited(observationOne);
        ImageCube imageCube = ProjectRepositoryTest.createDefaultImageCube(project);
        imageCube.setFilename("amanda.txt");
        observationOne.addImageCube(imageCube);
        ImageCube imageCube2 = ProjectRepositoryTest.createDefaultImageCube(project);
        imageCube.setFilename("amanda.txt");
        observationOne.addImageCube(imageCube2);

        Observation observationTwo = ProjectRepositoryTest.createDefaultObservation(sbid + 1);
        setObservationDeposited(observationTwo);
        ImageCube imageCube3 = ProjectRepositoryTest.createDefaultImageCube(project);
        imageCube3.setReleasedDate(DateTime.now());
        observationTwo.addImageCube(imageCube3);
        Catalogue catalogue = ProjectRepositoryTest.createDefaultCatalogue(project, CatalogueType.CONTINUUM_COMPONENT);
        observationTwo.addCatalogue(catalogue);
        MeasurementSet measurementSet = ProjectRepositoryTest.createDefaultMeasurementSet(project);
        observationTwo.addMeasurementSet(measurementSet);

        Observation observationThree = ProjectRepositoryTest.createDefaultObservation(sbid + 2);
        setObservationDeposited(observationThree);
        observationThree.addImageCube(ProjectRepositoryTest.createDefaultImageCube(project));
        Catalogue catalogue2 = ProjectRepositoryTest.createDefaultCatalogue(project, CatalogueType.CONTINUUM_COMPONENT);
        catalogue2.setReleasedDate(DateTime.now());
        observationThree.addCatalogue(catalogue2);
        MeasurementSet measurementSet2 = ProjectRepositoryTest.createDefaultMeasurementSet(project);
        observationThree.addMeasurementSet(measurementSet2);

        Project project2 = new Project(opalCode + "1");
        Observation observationFour = ProjectRepositoryTest.createDefaultObservation(sbid + 3);
        setObservationDeposited(observationFour);
        observationFour.addImageCube(ProjectRepositoryTest.createDefaultImageCube(project2));

        observationRepository.save(observationOne);
        observationRepository.save(observationTwo);
        observationRepository.save(observationThree);
        observationRepository.save(observationFour);

        commit();

        assertThat(observationRepository.count(), is(4L));
        assertThat(projectRepository.count(), is(2L));

        List<ObservationProjectDataProductsDTO> projectBlocks =
                depositManagerObservationRepository.findDepositedProjectBlocksWithUnreleasedImageCubes();
        assertThat(projectBlocks.size(), is(3));

        ObservationProjectDataProductsDTO expectedOne =
                new ObservationProjectDataProductsDTO(sbid, opalCode, observationOne.getObsStart(),
                        project.getPrincipalFirstName(), project.getPrincipalLastName());
        ObservationProjectDataProductsDTO expectedTwo =
                new ObservationProjectDataProductsDTO(sbid + 2, opalCode, observationThree.getObsStart(),
                        project.getPrincipalFirstName(), project.getPrincipalLastName());
        ObservationProjectDataProductsDTO expectedThree =
                new ObservationProjectDataProductsDTO(sbid + 3, project2.getOpalCode(), observationFour.getObsStart(),
                        project.getPrincipalFirstName(), project.getPrincipalLastName());

        assertThat(projectBlocks, containsInAnyOrder(expectedOne, expectedTwo, expectedThree));
    }

    @Test
    public void testFindUnreleasedProjectBlocksMeasurementSets()
    {
        String opalCode = "ABC123";
        Integer sbid = 123456798;

        Project project = new Project(opalCode);
        Observation observationOne = ProjectRepositoryTest.createDefaultObservation(sbid);
        setObservationDeposited(observationOne);
        ImageCube imageCube = ProjectRepositoryTest.createDefaultImageCube(project);
        imageCube.setFilename("amanda.txt");
        observationOne.addImageCube(imageCube);
        ImageCube imageCube2 = ProjectRepositoryTest.createDefaultImageCube(project);
        imageCube.setFilename("amanda.txt");
        observationOne.addImageCube(imageCube2);

        Observation observationTwo = ProjectRepositoryTest.createDefaultObservation(sbid + 1);
        setObservationDeposited(observationTwo);
        ImageCube imageCube3 = ProjectRepositoryTest.createDefaultImageCube(project);
        imageCube3.setReleasedDate(DateTime.now());
        observationTwo.addImageCube(imageCube3);
        Catalogue catalogue = ProjectRepositoryTest.createDefaultCatalogue(project, CatalogueType.CONTINUUM_COMPONENT);
        observationTwo.addCatalogue(catalogue);
        MeasurementSet measurementSet = ProjectRepositoryTest.createDefaultMeasurementSet(project);
        measurementSet.setReleasedDate(DateTime.now());
        observationTwo.addMeasurementSet(measurementSet);

        Observation observationThree = ProjectRepositoryTest.createDefaultObservation(sbid + 2);
        setObservationDeposited(observationThree);
        observationThree.addImageCube(ProjectRepositoryTest.createDefaultImageCube(project));
        Catalogue catalogue2 = ProjectRepositoryTest.createDefaultCatalogue(project, CatalogueType.CONTINUUM_COMPONENT);
        catalogue2.setReleasedDate(DateTime.now());
        observationThree.addCatalogue(catalogue2);
        MeasurementSet measurementSet2 = ProjectRepositoryTest.createDefaultMeasurementSet(project);
        observationThree.addMeasurementSet(measurementSet2);

        Project project2 = new Project(opalCode + "1");
        Observation observationFour = ProjectRepositoryTest.createDefaultObservation(sbid + 3);
        setObservationDeposited(observationFour);
        observationFour.addImageCube(ProjectRepositoryTest.createDefaultImageCube(project2));
        observationFour.addMeasurementSet(ProjectRepositoryTest.createDefaultMeasurementSet(project2));

        observationRepository.save(observationOne);
        observationRepository.save(observationTwo);
        observationRepository.save(observationThree);
        observationRepository.save(observationFour);

        commit();

        assertThat(observationRepository.count(), is(4L));
        assertThat(projectRepository.count(), is(2L));

        List<ObservationProjectDataProductsDTO> projectBlocks =
                depositManagerObservationRepository.findDepositedProjectBlocksWithUnreleasedMeasurementSets();
        assertThat(projectBlocks.size(), is(2));

        ObservationProjectDataProductsDTO expectedTwo =
                new ObservationProjectDataProductsDTO(sbid + 2, opalCode, observationThree.getObsStart(),
                        project.getPrincipalFirstName(), project.getPrincipalLastName());
        ObservationProjectDataProductsDTO expectedThree =
                new ObservationProjectDataProductsDTO(sbid + 3, project2.getOpalCode(), observationFour.getObsStart(),
                        project2.getPrincipalFirstName(), project2.getPrincipalLastName());

        assertThat(projectBlocks, containsInAnyOrder(expectedTwo, expectedThree));
    }

    @Test
    public void testFindUnreleasedProjectBlocksCatalogues()
    {
        String opalCode = "ABC123";
        Integer sbid = 123456798;

        Project project = new Project(opalCode);
        Observation observationOne = ProjectRepositoryTest.createDefaultObservation(sbid);
        setObservationDeposited(observationOne);
        ImageCube imageCube = ProjectRepositoryTest.createDefaultImageCube(project);
        imageCube.setFilename("amanda.txt");
        observationOne.addImageCube(imageCube);
        ImageCube imageCube2 = ProjectRepositoryTest.createDefaultImageCube(project);
        imageCube.setFilename("amanda.txt");
        observationOne.addImageCube(imageCube2);

        Observation observationTwo = ProjectRepositoryTest.createDefaultObservation(sbid + 1);
        setObservationDeposited(observationTwo);
        ImageCube imageCube3 = ProjectRepositoryTest.createDefaultImageCube(project);
        imageCube3.setReleasedDate(DateTime.now());
        observationTwo.addImageCube(imageCube3);
        Catalogue catalogue = ProjectRepositoryTest.createDefaultCatalogue(project, CatalogueType.CONTINUUM_COMPONENT);
        observationTwo.addCatalogue(catalogue);
        MeasurementSet measurementSet = ProjectRepositoryTest.createDefaultMeasurementSet(project);
        observationTwo.addMeasurementSet(measurementSet);

        Observation observationThree = ProjectRepositoryTest.createDefaultObservation(sbid + 2);
        setObservationDeposited(observationThree);
        observationThree.addImageCube(ProjectRepositoryTest.createDefaultImageCube(project));
        Catalogue catalogue2 = ProjectRepositoryTest.createDefaultCatalogue(project, CatalogueType.CONTINUUM_COMPONENT);
        catalogue2.setReleasedDate(DateTime.now());
        observationThree.addCatalogue(catalogue2);
        MeasurementSet measurementSet2 = ProjectRepositoryTest.createDefaultMeasurementSet(project);
        observationThree.addMeasurementSet(measurementSet2);

        Project project2 = new Project(opalCode + "1");
        Observation observationFour = ProjectRepositoryTest.createDefaultObservation(sbid + 3);
        setObservationDeposited(observationFour);
        observationFour.addImageCube(ProjectRepositoryTest.createDefaultImageCube(project2));
        observationFour.addCatalogue(ProjectRepositoryTest.createDefaultCatalogue(project2,
                CatalogueType.CONTINUUM_ISLAND));

        observationRepository.save(observationOne);
        observationRepository.save(observationTwo);
        observationRepository.save(observationThree);
        observationRepository.save(observationFour);

        commit();

        assertThat(observationRepository.count(), is(4L));
        assertThat(projectRepository.count(), is(2L));

        List<ObservationProjectDataProductsDTO> projectBlocks =
                depositManagerObservationRepository.findDepositedProjectBlocksWithUnreleasedCatalogues();
        assertThat(projectBlocks.size(), is(2));

        ObservationProjectDataProductsDTO expectedTwo =
                new ObservationProjectDataProductsDTO(sbid + 1, opalCode, observationTwo.getObsStart(),
                        project.getPrincipalFirstName(), project.getPrincipalLastName());
        ObservationProjectDataProductsDTO expectedThree =
                new ObservationProjectDataProductsDTO(sbid + 3, project2.getOpalCode(), observationFour.getObsStart(),
                        project2.getPrincipalFirstName(), project2.getPrincipalLastName());

        assertThat(projectBlocks, containsInAnyOrder(expectedTwo, expectedThree));
    }

    private void setObservationDeposited(Observation obs)
    {
        DepositState state = new DepositedDepositState(null, obs);
        obs.setDepositState(state);
    }
}
