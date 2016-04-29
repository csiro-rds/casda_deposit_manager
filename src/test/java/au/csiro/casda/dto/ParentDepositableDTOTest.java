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

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import au.csiro.AbstractMarshallingTest;

/**
 * Tests JSON serialisation and deserialisation of ParentDepositableDTO class
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public class ParentDepositableDTOTest extends AbstractMarshallingTest<ParentDepositableDTO>
{

    @Override
    protected ParentDepositableDTO getTestObject()
    {
        ParentDepositableDTO testObject = new ParentDepositableDTO();
        testObject.setDepositState(getSampleDepositStatus());
        testObject.setDepositableTypeDescription(RandomStringUtils.random(20));

        DepositableArtefactDTO artefact = new DepositableArtefactDTO();
        artefact.setDepositState(getSampleDepositStatus());
        artefact.setDepositableTypeDescription(RandomStringUtils.random(20));
        artefact.setFilename(RandomStringUtils.random(50));
        artefact.setFilesizeInBytes((long) Integer.MAX_VALUE + RandomUtils.nextInt(0, Integer.MAX_VALUE));
        artefact.setChecksum(RandomStringUtils.random(20));

        testObject.setDepositableArtefacts(new DepositableArtefactDTO[] { artefact });

        return testObject;
    }

    @Override
    protected void validateJsonDeserialisedObject(ParentDepositableDTO testObj,
            ParentDepositableDTO deserObj)
    {
        assertThat(deserObj.getDepositState(), is(testObj.getDepositState()));
        assertThat(deserObj.getDepositableArtefacts().length, equalTo(1));
        assertThat(deserObj.getDepositableArtefacts()[0].getDepositState(),
                is(testObj.getDepositableArtefacts()[0].getDepositState()));
        assertThat(deserObj.getDepositableArtefacts()[0].getFilename(),
                equalTo(testObj.getDepositableArtefacts()[0].getFilename()));
    }

    @Override
    protected void validateJsonSerialisedObject(ParentDepositableDTO testObj, Map<String, Object> map)
    {
        super.validateJsonSerialisedObject(testObj, map);

        assertThat(map.keySet().size(), equalTo(3));
        assertThat(map.keySet(),
                containsInAnyOrder("depositState", "depositableTypeDescription", "depositableArtefacts"));
        assertThat(map.get("depositState"), instanceOf(String.class));
        assertThat((String) map.get("depositState"), equalTo(testObj.getDepositState().toString()));
        assertThat(map.get("depositableTypeDescription"), instanceOf(String.class));
        assertThat((String) map.get("depositableTypeDescription"), equalTo(testObj.getDepositableTypeDescription()));
        assertThat(map.get("depositableArtefacts"), instanceOf(List.class));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> depositableArtefacts = (List<Map<String, Object>>) map.get("depositableArtefacts");
        assertThat(depositableArtefacts.size(), equalTo(1));
        assertThat(depositableArtefacts.get(0), instanceOf(Map.class));
        Map<String, Object> depositableArtefact = depositableArtefacts.get(0);
        assertThat(depositableArtefact.keySet().size(), equalTo(5));
        assertThat(
                depositableArtefact.keySet(),
                containsInAnyOrder("depositState", "depositableTypeDescription", "filename", "filesizeInBytes",
                        "checksum"));

        assertThat(depositableArtefact.get("depositState"), instanceOf(String.class));
        assertThat((String) depositableArtefact.get("depositState"), equalTo(testObj.getDepositableArtefacts()[0]
                .getDepositState().toString()));
        assertThat(depositableArtefact.get("depositableTypeDescription"), instanceOf(String.class));
        assertThat((String) depositableArtefact.get("depositableTypeDescription"),
                equalTo(testObj.getDepositableArtefacts()[0].getDepositableTypeDescription()));
        assertThat(depositableArtefact.get("filename"), instanceOf(String.class));
        assertThat((String) depositableArtefact.get("depositableTypeDescription"),
                equalTo(testObj.getDepositableArtefacts()[0].getDepositableTypeDescription()));
        assertThat(depositableArtefact.get("filesizeInBytes"), instanceOf(Long.class));
        assertThat((Long) depositableArtefact.get("filesizeInBytes"),
                equalTo(testObj.getDepositableArtefacts()[0].getFilesizeInBytes()));
        assertThat(depositableArtefact.get("checksum"), instanceOf(String.class));
        assertThat((String) depositableArtefact.get("checksum"),
                equalTo(testObj.getDepositableArtefacts()[0].getChecksum()));
    }

    private DepositStateDTO getSampleDepositStatus()
    {
        return DepositStateDTO.values()[RandomUtils.nextInt(0, DepositStateDTO.values().length - 1)];
    }

}
