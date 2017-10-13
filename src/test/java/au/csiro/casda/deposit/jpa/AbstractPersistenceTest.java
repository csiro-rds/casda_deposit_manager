package au.csiro.casda.deposit.jpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.junit.After;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;

import au.csiro.casda.deposit.TestAppConfig;

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
 * Base class for persistence tests. This class provides typical setup required to run persistence tests against
 * CasdaEntity classes, <em>without using SpringJUnit4ClassRunner</em>. Each test is wrapped in a transaction, which is
 * accessible via the protected EntityManager field. The transaction is rolled-back at the end of the test. Any
 * Repositories you might want to use need to be created using the following pattern: <code>
 *      private MyRepository myRepository;
 *      ...
 *      ...
 *      ...
 *      protected void initializeRepositories(RepositoryFactorySupport rfs)
 *      {
 *          myRepository = rfs.getRepository(MyRepository.class)
 *      }
 * </code> Repositories initialised in this way will use the instance's EntityManager (much like 'wrapping' a method in
 * a transaction via a javax.persitence.Transactional annotation). Please be aware that the database is wiped at the
 * start of every test, using a TestMetadataService (if you objects are still in the database, someone might have
 * forgotten to add a repository to the list in the Impl class).
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
@ContextConfiguration(classes = { TestAppConfig.class })
public abstract class AbstractPersistenceTest
{
    @PersistenceUnit
    protected EntityManagerFactory emf;

    @Autowired
    private ConfigurableApplicationContext context;

    protected RepositoryFactorySupport rfs;

    protected EntityManager entityManager;

    private boolean isDatabaseDirty;
    
    @Autowired
    private TestProjectRepository myProjectRepository;

    @Autowired
    private TestObservationRepository myObservationRepository;

    @Autowired
    private TestImageCubeRepository myImageCubeRepository;

    @Autowired
    private TestCatalogueRepository myCatalogueRepository;

    @Autowired
    private TestMeasurementSetRepository myMeasurementSetRepository;
    
    @Autowired
    private TestQualityFlagRepository myQualityFlagRepository;
    
    @Autowired
    private TestValidationNoteRepository myValidationNoteRepository;
    
    @Autowired
    private TestObservationProjectQualityFlagRepository myObservationProjectQualityFlagRepository;

    public AbstractPersistenceTest() throws Exception
    {
        // Standard Spring test config in the absence of a SpringJUnitTestRunner
        TestContextManager testContextManager = new TestContextManager(getClass());
        testContextManager.prepareTestInstance(this);
    }

    @Before
    public void setUp()
    {
        this.isDatabaseDirty = false;

        entityManager = emf.createEntityManager();
        rfs = new JpaRepositoryFactory(entityManager);
        initializeRepositories(rfs);
        entityManager.getTransaction().begin();
    }

    /**
     * Template method subclasses can override to initialise a Repository using the instance's EntityManager.
     * 
     * @param rfs
     *            a RepositoryFactorySupport instance
     */
    protected void initializeRepositories(RepositoryFactorySupport rfs)
    {
        // NOOP
    }

    @After
    public void tearDown()
    {
        try
        {
            entityManager.getTransaction().rollback();
            if (this.isDatabaseDirty)
            {
                myCatalogueRepository.deleteAll();
                myImageCubeRepository.deleteAll();
                myMeasurementSetRepository.deleteAll();
                myObservationRepository.deleteAll();
                myValidationNoteRepository.deleteAll();
                myProjectRepository.deleteAll();
                myQualityFlagRepository.deleteAll();
                myObservationProjectQualityFlagRepository.deleteAll();
            }
        }
        finally
        {
            entityManager.close();
        }
    }

    /**
     * Commits the current transaction and starts a new one. Please note that this will effectively dirty the database
     * and will force the test case to try and wipe the entire database after the test has run.
     */
    protected void commit()
    {
        this.isDatabaseDirty = true;
        entityManager.getTransaction().commit();
        entityManager.clear();
        entityManager.getTransaction().begin();
    }

}
