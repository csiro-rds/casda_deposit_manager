package au.csiro.casda.dto;

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

import org.junit.Test;

import au.csiro.AbstractMarshallingTest;
import au.csiro.casda.entity.QualityFlag;

/**
 * Tests JSON serialisation and deserialisation of QualityFlagDTO class
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 *
 */
public class QualityFlagDTOTest extends AbstractMarshallingTest<QualityFlagDTO>
{
    @Override
    protected QualityFlagDTO getTestObject()
    {
        QualityFlagDTO qualityFlag = new QualityFlagDTO();
        qualityFlag.setCode("code");
        qualityFlag.setId(14L);
        qualityFlag.setLabel("label");
        qualityFlag.setDisplayOrder(5);

        return qualityFlag;
    }

    @Test
    public void testConstructor()
    {
        QualityFlag qualityFlag = new QualityFlag();
        qualityFlag.setId(1L);
        qualityFlag.setActive(true);
        qualityFlag.setCode("code1");
        qualityFlag.setLabel("label1");
        qualityFlag.setDisplayOrder(5);

        QualityFlagDTO qualityFlagDTO = new QualityFlagDTO(qualityFlag);
        assertEquals(1L, qualityFlagDTO.getId());
        assertEquals(5, qualityFlagDTO.getDisplayOrder());
        assertEquals("code1", qualityFlagDTO.getCode());
        assertEquals("label1", qualityFlagDTO.getLabel());

    }

}