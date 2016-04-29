package au.csiro.casda.entity;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import au.csiro.casda.datadeposit.ChildDepositableArtefact;
import au.csiro.casda.entity.observation.Level7Collection;
import au.csiro.casda.entity.observation.Observation;
import au.csiro.casda.entity.observation.ChildDepositableArtefactComparator;
import au.csiro.casda.entity.observation.ObservationMetadataFile;

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
 * Test case for ObservationDepositableArtefactComparator
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public class ChildDepositableArtefactComparatorTest
{
    private static final class DummyCasdaDepositableArtefactA extends DummyObservationDepositableArtefact
    {
        private DummyCasdaDepositableArtefactA(Observation observation, String filename)
        {
            super(observation, filename);
        }
    }

    private static final class DummyCasdaDepositableArtefactB extends DummyObservationDepositableArtefact
    {
        private DummyCasdaDepositableArtefactB(Observation observation, String filename)
        {
            super(observation, filename);
        }
    }

    private static final class DummyCasdaDepositableArtefactC extends DummyObservationDepositableArtefact
    {
        private DummyCasdaDepositableArtefactC(Observation observation, String filename)
        {
            super(observation, filename);
        }
    }

    private ChildDepositableArtefactComparator comparator = new ChildDepositableArtefactComparator();

    @Test
    public void bothIdenticalShouldReturnZero()
    {
        ChildDepositableArtefact depositable = new DummyObservationDepositableArtefact(null, null);
        assertThat(comparator.compare(depositable, depositable), is(equalTo(0)));
    }

    @Test
    public void bothNullShouldReturnZero()
    {
        assertThat(comparator.compare(null, null), is(equalTo(0)));
    }

    @Test
    public void firstNullShouldReturnMinusOne()
    {
        ChildDepositableArtefact depositable = new DummyObservationDepositableArtefact(null, null);
        assertThat(comparator.compare(null, depositable), is(lessThan(0)));
    }

    @Test
    public void secondNullShouldReturnPlusOne()
    {
        ChildDepositableArtefact depositable = new DummyObservationDepositableArtefact(null, null);
        assertThat(comparator.compare(depositable, null), is(greaterThan(0)));
    }

    @Test
    public void depositablesFromDifferentObservationsShouldOrderByObservation()
    {
        String filename = "file1.xml";

        Observation observation1 = new Observation(12345);
        ChildDepositableArtefact depositable1 = new DummyObservationDepositableArtefact(observation1, filename);

        Observation observation2 = new Observation(23456);
        ChildDepositableArtefact depositable2 = new DummyObservationDepositableArtefact(observation2, filename);

        Observation observation3 = new Observation(34567);
        ChildDepositableArtefact depositable3 = new DummyObservationDepositableArtefact(observation3, filename);

        assertThat(comparator.compare(depositable1, depositable2), is(lessThan(0)));
        assertThat(comparator.compare(depositable1, depositable3), is(lessThan(0)));

        assertThat(comparator.compare(depositable2, depositable1), is(greaterThan(0)));
        assertThat(comparator.compare(depositable2, depositable3), is(lessThan(0)));

        assertThat(comparator.compare(depositable3, depositable1), is(greaterThan(0)));
        assertThat(comparator.compare(depositable3, depositable2), is(greaterThan(0)));
    }

    @Test
    public void depositablesFromSameObservationShouldOrderObservationMetadataFileFirst()
    {
        Observation observation = new Observation(12345);

        ChildDepositableArtefact depositable1 = new ObservationMetadataFile(observation);
        ChildDepositableArtefact depositable2 =
                new DummyObservationDepositableArtefact(observation, depositable1.getFilename());
        ChildDepositableArtefact depositable3 =
                new DummyObservationDepositableArtefact(observation, depositable1.getFilename());

        assertThat(comparator.compare(depositable1, depositable2), is(lessThan(0)));
        assertThat(comparator.compare(depositable1, depositable3), is(lessThan(0)));

        assertThat(comparator.compare(depositable2, depositable1), is(greaterThan(0)));

        assertThat(comparator.compare(depositable3, depositable1), is(greaterThan(0)));
    }

    @Test
    public void depositablesFromSameObservationShouldOrderNonObservationMetadataFilesByClassName()
    {
        String filename = "file1.xml";
        Observation observation = new Observation(12345);

        ChildDepositableArtefact depositable1 = new DummyCasdaDepositableArtefactA(observation, filename);
        ChildDepositableArtefact depositable2 = new DummyCasdaDepositableArtefactB(observation, filename);
        ChildDepositableArtefact depositable3 = new DummyCasdaDepositableArtefactC(observation, filename);

        assertThat(comparator.compare(depositable1, depositable2), is(lessThan(0)));
        assertThat(comparator.compare(depositable1, depositable3), is(lessThan(0)));

        assertThat(comparator.compare(depositable2, depositable1), is(greaterThan(0)));
        assertThat(comparator.compare(depositable2, depositable3), is(lessThan(0)));

        assertThat(comparator.compare(depositable3, depositable1), is(greaterThan(0)));
        assertThat(comparator.compare(depositable3, depositable2), is(greaterThan(0)));
    }

    @Test
    public void depositablesFromSameObservationWithSameClassShouldOrderByFilename()
    {
        Observation observation = new Observation(12345);

        ChildDepositableArtefact depositable1 = new DummyObservationDepositableArtefact(observation, "filename1");
        ChildDepositableArtefact depositable2 = new DummyObservationDepositableArtefact(observation, "filename2");
        ChildDepositableArtefact depositable3 = new DummyObservationDepositableArtefact(observation, "filename3");

        assertThat(comparator.compare(depositable1, depositable2), is(lessThan(0)));
        assertThat(comparator.compare(depositable1, depositable3), is(lessThan(0)));

        assertThat(comparator.compare(depositable2, depositable1), is(greaterThan(0)));
        assertThat(comparator.compare(depositable2, depositable3), is(lessThan(0)));

        assertThat(comparator.compare(depositable3, depositable1), is(greaterThan(0)));
        assertThat(comparator.compare(depositable3, depositable2), is(greaterThan(0)));
    }
    
    @Test
    public void depositablesWithDifferentParentTypeShouldOrderByParentClassname()
    {
        Observation observation = new Observation(12345);
        Level7Collection level7Collection = new Level7Collection(123465);

        ChildDepositableArtefact depositable1 = new DummyObservationDepositableArtefact(observation, "filename1");
        ChildDepositableArtefact depositable2 = new DummyObservationDepositableArtefact(level7Collection, "filename2");
        
        assertThat(comparator.compare(depositable1, depositable2), is(greaterThan(0)));
        assertThat(comparator.compare(depositable2, depositable1), is(lessThan(0)));
    }
    
    
    @Test
    public void depositablesFirstParentNullShouldReturnMinusOne()
    {
        Level7Collection level7Collection = new Level7Collection(123465);

        ChildDepositableArtefact depositable1 = new DummyObservationDepositableArtefact(null, "filename1");
        ChildDepositableArtefact depositable2 = new DummyObservationDepositableArtefact(level7Collection, "filename2");
        
        assertThat(comparator.compare(depositable1, depositable2), is(lessThan(0)));
    }
    
    @Test
    public void depositablesSecondParentNullShouldReturnMinusOne()
    {
        Level7Collection level7Collection = new Level7Collection(123465);

        ChildDepositableArtefact depositable1 = new DummyObservationDepositableArtefact(null, "filename1");
        ChildDepositableArtefact depositable2 = new DummyObservationDepositableArtefact(level7Collection, "filename2");

        assertThat(comparator.compare(depositable2, depositable1), is(greaterThan(0)));
    }
    
    @Test
    public void depositablesBothWithoutParentShouldOrderByOwnAttributes()
    {
        ChildDepositableArtefact depositable1 = new DummyObservationDepositableArtefact(null, "filename1");
        ChildDepositableArtefact depositable2 = new DummyObservationDepositableArtefact(null, "filename2");
        
        assertThat(comparator.compare(depositable1, depositable2), is(lessThan(0)));
        assertThat(comparator.compare(depositable2, depositable1), is(greaterThan(0)));
    }
    
    
}
