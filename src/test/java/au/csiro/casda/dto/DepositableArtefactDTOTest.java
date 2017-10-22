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


import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Map;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import au.csiro.AbstractMarshallingTest;

/**
 * Tests JSON serialisation and deserialisation of DepositableArtefactDTO class
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public class DepositableArtefactDTOTest extends AbstractMarshallingTest<DepositableArtefactDTO>
{

    @Override
    protected DepositableArtefactDTO getTestObject()
    {
        String filename = RandomStringUtils.random(50);
        DepositStateDTO status = DepositStateDTO.values()[RandomUtils.nextInt(0, DepositStateDTO.values().length - 1)];

        DepositableArtefactDTO testObject = new DepositableArtefactDTO();
        testObject.setDepositState(status);
        testObject.setDepositableTypeDescription(RandomStringUtils.random(20));
        testObject.setFilename(filename);
        testObject.setFilesizeInBytes((long) Integer.MAX_VALUE + RandomUtils.nextInt(0, Integer.MAX_VALUE));
        testObject.setChecksum(RandomStringUtils.random(20));
        return testObject;
    }

    @Override
    protected void validateJsonDeserialisedObject(DepositableArtefactDTO testObj, DepositableArtefactDTO deserObj)
    {
        assertThat(deserObj.getFilename(), equalTo(testObj.getFilename()));
        assertThat(deserObj.getDepositState(), is(testObj.getDepositState()));
    }

    @Override
    protected void validateJsonSerialisedObject(DepositableArtefactDTO testObj, Map<String, Object> map)
    {
        super.validateJsonSerialisedObject(testObj, map);

        assertThat(
                map.keySet(),
                containsInAnyOrder("depositState", "depositableTypeDescription", "filename", "filesizeInBytes",
                        "checksum", "thumbnailName"));
        assertThat(map.get("depositState"), instanceOf(String.class));
        assertThat((String) map.get("depositState"), equalTo(testObj.getDepositState().toString()));
        assertThat(map.get("depositableTypeDescription"), instanceOf(String.class));
        assertThat((String) map.get("depositableTypeDescription"), equalTo(testObj.getDepositableTypeDescription()));
        assertThat(map.get("filename"), instanceOf(String.class));
        assertThat((String) map.get("depositableTypeDescription"), equalTo(testObj.getDepositableTypeDescription()));
        assertThat(map.get("filesizeInBytes"), instanceOf(Long.class));
        assertThat((Long) map.get("filesizeInBytes"), equalTo(testObj.getFilesizeInBytes()));
        assertThat(map.get("checksum"), instanceOf(String.class));
        assertThat((String) map.get("checksum"), equalTo(testObj.getChecksum()));
    }
}
