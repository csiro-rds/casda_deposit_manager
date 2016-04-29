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

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.test.context.ContextConfiguration;

import au.csiro.casda.deposit.TestAppConfig;
import au.csiro.casda.entity.ValidationNote;

/**
 * Tests the validation note repository.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
@ContextConfiguration(classes = { TestAppConfig.class })
public class ValidationNoteRepositoryTest extends AbstractPersistenceTest
{

    @Autowired
    private ValidationNoteRepository validationNoteRepository;

    private TestValidationNoteRepository testValidationNoteRepository;

    public ValidationNoteRepositoryTest() throws Exception
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
        testValidationNoteRepository = rfs.getRepository(TestValidationNoteRepository.class);
    }

    @Test
    public void testFindByActiveOrderByDisplayOrderAsc()
    {
        ValidationNote note1 = new ValidationNote();
        note1.setContent("blah blah one");
        note1.setCreated(DateTime.now(DateTimeZone.UTC));
        note1.setPersonId("abc111");
        note1.setPersonName("name one");
        note1.setSbid(111);
        note1.setProjectId(12L);

        ValidationNote note2 = new ValidationNote();
        note2.setContent("blah blah two");
        note2.setCreated(DateTime.now(DateTimeZone.UTC).minusDays(1));
        note2.setPersonId("abc123");
        note2.setPersonName("name two");
        note2.setSbid(111);
        note2.setProjectId(12L);

        ValidationNote note3 = new ValidationNote();
        note3.setContent("blah blah three");
        note3.setCreated(DateTime.now(DateTimeZone.UTC).minusDays(1));
        note3.setPersonId("abc123");
        note3.setPersonName("name two");
        note3.setSbid(115);
        note3.setProjectId(12L);

        testValidationNoteRepository.save(note1);
        testValidationNoteRepository.save(note2);
        testValidationNoteRepository.save(note3);

        commit();

        List<ValidationNote> validationNotes =
                validationNoteRepository.findBySbidAndProjectIdOrderByCreatedAsc(111, 12L);
        assertEquals(2, validationNotes.size());
        assertEquals("blah blah two", validationNotes.get(0).getContent());
        assertEquals("blah blah one", validationNotes.get(1).getContent());

        validationNotes = validationNoteRepository.findBySbidAndProjectIdOrderByCreatedAsc(115, 12L);
        assertEquals(1, validationNotes.size());
        assertEquals("blah blah three", validationNotes.get(0).getContent());
    }

}
