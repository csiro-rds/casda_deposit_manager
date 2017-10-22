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

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import au.csiro.AbstractMarshallingTest;
import au.csiro.casda.entity.ValidationNote;
import au.csiro.casda.entity.observation.Project;

/**
 * Tests JSON serialisation and deserialisation of ValidationNoteDTO class
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 *
 */
public class ValidationNoteDTOTest extends AbstractMarshallingTest<ValidationNoteDTO>
{
    @Override
    protected ValidationNoteDTO getTestObject()
    {
        ValidationNoteDTO validationNote = new ValidationNoteDTO();
        validationNote.setContent("the content");
        validationNote.setCreatedDate(DateTime.now(DateTimeZone.UTC).getMillis());
        validationNote.setId(156L);
        validationNote.setUserId("user123");
        validationNote.setUserName("Lee Jones");

        return validationNote;
    }

    @Test
    public void testConstructor()
    {
        DateTime now = DateTime.now(DateTimeZone.UTC);
    	Project project = new Project();
    	project.setId(12L);
        ValidationNote validationNote = new ValidationNote();
        validationNote.setContent("the content");
        validationNote.setCreated(now);
        validationNote.setId(228L);
        validationNote.setPersonId("person123");
        validationNote.setPersonName("Person Name");
        validationNote.setProject(project);
        validationNote.setSbid(152);

        ValidationNoteDTO validationNoteDto = new ValidationNoteDTO(validationNote);
        assertEquals("the content", validationNoteDto.getContent());
        assertEquals(now.getMillis(), validationNoteDto.getCreatedDate().longValue());
        assertEquals(228L, validationNoteDto.getId());
        assertEquals("person123", validationNoteDto.getUserId());
        assertEquals("Person Name", validationNoteDto.getUserName());

    }

}