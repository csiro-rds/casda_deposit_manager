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
import static org.junit.Assert.assertNull;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.test.context.ContextConfiguration;

import au.csiro.casda.deposit.TestAppConfig;
import au.csiro.casda.entity.QualityFlag;

/**
 * Tests the quality flag repository.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
@ContextConfiguration(classes = { TestAppConfig.class })
public class QualityFlagRepositoryTest extends AbstractPersistenceTest
{

    @Autowired
    private QualityFlagRepository qualityFlagRepository;

    private TestQualityFlagRepository testQualityFlagRepository;

    public QualityFlagRepositoryTest() throws Exception
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
    }

    @Test
    public void testFindByActiveOrderByDisplayOrderAsc()
    {
        QualityFlag flag1 = new QualityFlag();
        flag1.setCode("flag1");
        flag1.setActive(true);
        flag1.setDisplayOrder(4);

        QualityFlag flag2 = new QualityFlag();
        flag2.setCode("flag2");
        flag2.setActive(true);
        flag2.setDisplayOrder(1);

        QualityFlag flag3 = new QualityFlag();
        flag3.setCode("flag3");
        flag3.setActive(false);
        flag3.setDisplayOrder(7);

        testQualityFlagRepository.save(flag1);
        testQualityFlagRepository.save(flag2);
        testQualityFlagRepository.save(flag3);

        commit();

        List<QualityFlag> activeFlags = qualityFlagRepository.findByActiveOrderByDisplayOrderAsc(true);
        assertEquals(2, activeFlags.size());
        assertEquals("flag2", activeFlags.get(0).getCode());
        assertEquals("flag1", activeFlags.get(1).getCode());

        List<QualityFlag> inactiveFlags = qualityFlagRepository.findByActiveOrderByDisplayOrderAsc(false);
        assertEquals(1, inactiveFlags.size());
        assertEquals(inactiveFlags.get(0).getCode(), "flag3");
    }

    @Test
    public void testFindByCodeAndActive()
    {
        QualityFlag flag1 = new QualityFlag();
        flag1.setCode("flagA");
        flag1.setDisplayOrder(5);
        flag1.setActive(true);

        QualityFlag flag2 = new QualityFlag();
        flag2.setCode("flagB");
        flag2.setDisplayOrder(8);
        flag2.setActive(true);

        flag1 = testQualityFlagRepository.save(flag1);
        flag2 = testQualityFlagRepository.save(flag2);

        commit();

        assertEquals(2, qualityFlagRepository.count());

        QualityFlag qualityFlag = qualityFlagRepository.findByCodeAndActive("flagA", true);
        assertEquals("flagA", qualityFlag.getCode());

        QualityFlag qualityFlag2 = qualityFlagRepository.findByCodeAndActive("flagB", true);
        assertEquals("flagB", qualityFlag2.getCode());
        
        QualityFlag qualityFlag3 = qualityFlagRepository.findByCodeAndActive("flagA", false);
        assertNull(qualityFlag3);
    }
}
