package au.csiro.casda.deposit.jpa;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
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

import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.joda.time.DateTime;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.test.context.ContextConfiguration;

import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.datadeposit.DepositedDepositState;
import au.csiro.casda.deposit.TestAppConfig;
import au.csiro.casda.entity.observation.Catalogue;
import au.csiro.casda.entity.observation.CatalogueType;
import au.csiro.casda.entity.observation.ImageCube;
import au.csiro.casda.entity.observation.MeasurementSet;
import au.csiro.casda.entity.observation.Observation;
import au.csiro.casda.entity.observation.Project;

/**
 * Test case for DepositManagerProjectRepository.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
@ContextConfiguration(classes = { TestAppConfig.class })
public class ProjectRepositoryTest extends AbstractPersistenceTest
{
    @Autowired
    private ProjectRepository depositManagerProjectRepository;

    private TestProjectRepository projectRepository;

    private TestObservationRepository observationRepository;

    public ProjectRepositoryTest() throws Exception
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
    public void findNewProjectCodesForNoProjects()
    {
        List<String> results = depositManagerProjectRepository.findNewProjectCodes();
        assertThat(results, empty());
    }

    @Test
    public void findNewProjectCodesForUnknownProjectWithNoObservations()
    {
        String opalCode = "AS007";
        Project project = new Project(opalCode);
        projectRepository.save(project);

        commit();

        assertThat(observationRepository.count(), is(0L));
        assertThat(projectRepository.count(), is(1L));
        project = projectRepository.findAll().iterator().next();

        List<String> results = depositManagerProjectRepository.findNewProjectCodes();
        assertThat(results, empty());
    }

    @Test
    public void findNewProjectCodesForUnknownProjectWithOneNonDepositedObservation()
    {
        String opalCode = "AS007";
        Integer sbid = RandomUtils.nextInt(0, Integer.MAX_VALUE);

        Project project = new Project(opalCode);
        Observation observation = createDefaultObservation(sbid);
        observation.addImageCube(createDefaultImageCube(project));

        observationRepository.save(observation);

        commit();

        assertThat(observationRepository.count(), is(1L));
        assertThat(projectRepository.count(), is(1L));

        List<String> results = depositManagerProjectRepository.findNewProjectCodes();
        assertThat(results, empty());
    }

    @Test
    public void findNewProjectCodesForUnknownProjectWithOneDepositedObservation()
    {
        String opalCode = "AS007";
        Integer sbid = RandomUtils.nextInt(0, Integer.MAX_VALUE);

        Project project = new Project(opalCode);
        Observation observation = createDefaultObservation(sbid);
        observation.addImageCube(createDefaultImageCube(project));
        observation.setDepositState(new DepositedDepositState(mock(DepositStateFactory.class), observation));

        observationRepository.save(observation);

        commit();

        assertThat(observationRepository.count(), is(1L));
        assertThat(projectRepository.count(), is(1L));

        List<String> results = depositManagerProjectRepository.findNewProjectCodes();
        assertThat(results.size(), is(1));
        assertThat(results, containsInAnyOrder(opalCode));
    }

    @Test
    public void findNewProjectCodesForUnknownProjectWithTwoNonDepositedObservations()
    {
        String opalCode = "AS007";
        Integer sbid1 = RandomUtils.nextInt(0, Integer.MAX_VALUE);
        Integer sbid2 = RandomUtils.nextInt(0, Integer.MAX_VALUE);

        Project project = new Project(opalCode);
        Observation observation1 = createDefaultObservation(sbid1);
        observation1.addImageCube(createDefaultImageCube(project));
        Observation observation2 = createDefaultObservation(sbid2);
        observation2.addImageCube(createDefaultImageCube(project));

        observationRepository.save(observation1);
        observationRepository.save(observation2);

        commit();

        assertThat(observationRepository.count(), is(2L));
        assertThat(projectRepository.count(), is(1L));

        List<String> results = depositManagerProjectRepository.findNewProjectCodes();
        assertThat(results, empty());
    }

    @Test
    public void findNewProjectCodesForUnknownProjectWithOneDepositedObservationAndOneUndepositedObservation()
    {
        String opalCode = "AS007";
        Integer sbid1 = RandomUtils.nextInt(0, Integer.MAX_VALUE);
        Integer sbid2 = RandomUtils.nextInt(0, Integer.MAX_VALUE);

        Project project = new Project(opalCode);
        Observation observation1 = createDefaultObservation(sbid1);
        observation1.addImageCube(createDefaultImageCube(project));
        Observation observation2 = createDefaultObservation(sbid2);
        observation2.addImageCube(createDefaultImageCube(project));
        observation2.setDepositState(new DepositedDepositState(mock(DepositStateFactory.class), observation2));

        observationRepository.save(observation1);
        observationRepository.save(observation2);

        commit();

        assertThat(observationRepository.count(), is(2L));
        assertThat(projectRepository.count(), is(1L));

        List<String> results = depositManagerProjectRepository.findNewProjectCodes();
        assertThat(results.size(), is(1));
        assertThat(results, containsInAnyOrder(opalCode));
    }

    @Test
    public void findNewProjectCodesForKnownProjectWithNoObservations()
    {
        String opalCode = "AS007";
        Project project = new Project(opalCode);
        project.setKnownProject(true);
        projectRepository.save(project);

        commit();

        assertThat(observationRepository.count(), is(0L));
        assertThat(projectRepository.count(), is(1L));
        project = projectRepository.findAll().iterator().next();

        List<String> results = depositManagerProjectRepository.findNewProjectCodes();
        assertThat(results, empty());
    }

    @Test
    public void findNewProjectCodesForKnownProjectWithOneDepositedObservation()
    {
        String opalCode = "AS007";
        Integer sbid = RandomUtils.nextInt(0, Integer.MAX_VALUE);

        Project project = new Project(opalCode);
        project.setKnownProject(true);
        Observation observation = createDefaultObservation(sbid);
        observation.addImageCube(createDefaultImageCube(project));
        observation.setDepositState(new DepositedDepositState(mock(DepositStateFactory.class), observation));

        observationRepository.save(observation);

        commit();

        assertThat(observationRepository.count(), is(1L));
        assertThat(projectRepository.count(), is(1L));

        List<String> results = depositManagerProjectRepository.findNewProjectCodes();
        assertThat(results, is(empty()));
    }

    @Test
    public void findNewProjectCodesForOneKnownAndOneUnknownProjectWithOneSharedDepositedObservation()
    {
        String opalCode1 = "AS007";
        String opalCode2 = "AS008";
        Integer sbid = RandomUtils.nextInt(0, Integer.MAX_VALUE);

        Project project1 = new Project(opalCode1);
        project1.setKnownProject(true);
        Project project2 = new Project(opalCode2);
        Observation observation = createDefaultObservation(sbid);
        observation.addImageCube(createDefaultImageCube(project1));
        observation.addImageCube(createDefaultImageCube(project2));
        observation.setDepositState(new DepositedDepositState(mock(DepositStateFactory.class), observation));

        observationRepository.save(observation);

        commit();

        assertThat(observationRepository.count(), is(1L));
        assertThat(projectRepository.count(), is(2L));

        List<String> results = depositManagerProjectRepository.findNewProjectCodes();
        assertThat(results.size(), is(1));
        assertThat(results, containsInAnyOrder(opalCode2));
    }

    public static Observation createDefaultObservation(Integer sbid)
    {
        Observation observation = new Observation(sbid);
        observation.setTelescope("ASKAP");
        observation.setObsProgram("test");
        observation.setObsStart(DateTime.now());
        observation.setObsStartMjd(0.0);
        observation.setObsEnd(DateTime.now());
        observation.setObsEndMjd(0.0);
        return observation;
    }

    public static ImageCube createDefaultImageCube(Project project)
    {
        ImageCube imageCube = new ImageCube(project);
        imageCube.setFilename(RandomStringUtils.randomAlphabetic(30) + ".fits");
        imageCube.setFormat(RandomStringUtils.randomAlphabetic(10));
        imageCube.setType("cont_restored_T0");
        return imageCube;
    }

    public static MeasurementSet createDefaultMeasurementSet(Project project)
    {
        MeasurementSet measurementSet = new MeasurementSet(project);
        measurementSet.setFilename(RandomStringUtils.randomAlphabetic(30) + ".fits");
        measurementSet.setFormat(RandomStringUtils.randomAlphabetic(10));
        return measurementSet;
    }

    public static Catalogue createDefaultCatalogue(Project project, CatalogueType catalogueType)
    {
        Catalogue catalogue = new Catalogue(catalogueType, project);
        catalogue.setFilename(RandomStringUtils.randomAlphabetic(30) + ".xml");
        catalogue.setFormat(RandomStringUtils.randomAlphabetic(10));
        return catalogue;
    }
}
