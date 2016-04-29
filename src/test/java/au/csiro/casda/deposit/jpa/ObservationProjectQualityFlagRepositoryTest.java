package au.csiro.casda.deposit.jpa;

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

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.test.context.ContextConfiguration;

import au.csiro.casda.deposit.TestAppConfig;
import au.csiro.casda.entity.ObservationProjectQualityFlag;
import au.csiro.casda.entity.QualityFlag;

/**
 * Tests the observation-project-quality-flag repository.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
@ContextConfiguration(classes = { TestAppConfig.class })
public class ObservationProjectQualityFlagRepositoryTest extends AbstractPersistenceTest
{

    private TestQualityFlagRepository testQualityFlagRepository;

    @Autowired
    private ObservationProjectQualityFlagRepository observationProjectQualityFlagRepository;

    private TestObservationProjectQualityFlagRepository testObservationProjectQualityFlagRepository;

    public ObservationProjectQualityFlagRepositoryTest() throws Exception
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
        testQualityFlagRepository = rfs.getRepository(TestQualityFlagRepository.class);
        testObservationProjectQualityFlagRepository =
                rfs.getRepository(TestObservationProjectQualityFlagRepository.class);
    }

    @Test
    public void testDeleteQualityFlagCodesForObservationProject()
    {

        ObservationProjectQualityFlag opqf1 = new ObservationProjectQualityFlag();
        opqf1.setObservationId(12L);
        opqf1.setProjectId(15L);
        opqf1.setQualityFlagId(1L);

        ObservationProjectQualityFlag opqf2 = new ObservationProjectQualityFlag();
        opqf2.setObservationId(13L);
        opqf2.setProjectId(15L);
        opqf2.setQualityFlagId(2L);

        ObservationProjectQualityFlag opqf3 = new ObservationProjectQualityFlag();
        opqf3.setObservationId(12L);
        opqf3.setProjectId(15L);
        opqf3.setQualityFlagId(3L);

        opqf1 = testObservationProjectQualityFlagRepository.save(opqf1);
        opqf2 = testObservationProjectQualityFlagRepository.save(opqf2);
        opqf3 = testObservationProjectQualityFlagRepository.save(opqf3);

        commit();

        assertEquals(3, observationProjectQualityFlagRepository.count());

        observationProjectQualityFlagRepository.deleteByProjectIdAndObservationId(15L, 12L);

        assertEquals(1, observationProjectQualityFlagRepository.count());
        ObservationProjectQualityFlag remainingFlag =
                observationProjectQualityFlagRepository.findAll().iterator().next();
        assertEquals(opqf2.getId(), remainingFlag.getId());
    }

    @Test
    public void testGetQualityFlagCodesForObservationProject()
    {
        QualityFlag flag1 = new QualityFlag();
        flag1.setCode("flagA");
        flag1.setDisplayOrder(5);

        QualityFlag flag2 = new QualityFlag();
        flag2.setCode("flagB");
        flag2.setDisplayOrder(8);

        flag1 = testQualityFlagRepository.save(flag1);
        flag2 = testQualityFlagRepository.save(flag2);

        ObservationProjectQualityFlag opqf1 = new ObservationProjectQualityFlag();
        opqf1.setObservationId(12L);
        opqf1.setProjectId(15L);
        opqf1.setQualityFlagId(flag1.getId());

        ObservationProjectQualityFlag opqf2 = new ObservationProjectQualityFlag();
        opqf2.setObservationId(13L);
        opqf2.setProjectId(15L);
        opqf2.setQualityFlagId(flag2.getId());

        testObservationProjectQualityFlagRepository.save(opqf1);
        testObservationProjectQualityFlagRepository.save(opqf2);

        commit();

        assertEquals(2, observationProjectQualityFlagRepository.count());

        List<String> qualityFlagCodes =
                observationProjectQualityFlagRepository.getQualityFlagCodesForObservationProject(12L, 15L);

        assertEquals(1, qualityFlagCodes.size());
        assertEquals("flagA", qualityFlagCodes.get(0));
    }
}
