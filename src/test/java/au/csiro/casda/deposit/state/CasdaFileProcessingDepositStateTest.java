package au.csiro.casda.deposit.state;

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


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositState.Type;
import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.entity.CasdaDepositableArtefactEntity;
import au.csiro.casda.entity.observation.MeasurementSet;
import au.csiro.casda.entity.observation.Observation;

/**
 * Verify the functions of the CasdaFileProcessingDepositState class.
 * 
 * Copyright 2015, CSIRO Australia
 * All rights reserved.
 */
public class CasdaFileProcessingDepositStateTest
{
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Mock
    private DepositStateFactory stateFactory;

    @Mock
    private DepositState next;

    
    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
        when(next.getType()).thenReturn(Type.PROCESSED);
    }
    
    @Test
    public void testMeasurementSetProcessing() throws IOException
    {
        Observation observation = new Observation();
        MeasurementSet measurementSet = new MeasurementSet();
        observation.addMeasurementSet(measurementSet);
        
        verifyProcessingStage(measurementSet, "Measurementset.tar", 65535, 64);
    }
    
    @Test
    public void testMeasurementSetProcessingForSize2047() throws IOException
    {
        Observation observation = new Observation();
        MeasurementSet measurementSet = new MeasurementSet();
        observation.addMeasurementSet(measurementSet);
        
        verifyProcessingStage(measurementSet, "Measurementset.tar", 2047, 2);
    }
    
    @Test
    public void testMeasurementSetProcessingForSize2048() throws IOException
    {
        Observation observation = new Observation();
        MeasurementSet measurementSet = new MeasurementSet();
        observation.addMeasurementSet(measurementSet);
        
        verifyProcessingStage(measurementSet, "Measurementset.tar", 2048, 2);
    }
    
    @Test
    public void testMeasurementSetProcessingForSize2049() throws IOException
    {
        Observation observation = new Observation();
        MeasurementSet measurementSet = new MeasurementSet();
        observation.addMeasurementSet(measurementSet);
        
        verifyProcessingStage(measurementSet, "Measurementset.tar", 2049, 3);
    }

    private void verifyProcessingStage(CasdaDepositableArtefactEntity target, String name, long expectedSize,
            long expectedSizeKb) throws IOException
    {
        File parentFolder = tempFolder. newFolder("42");
        File dataFile = new File(parentFolder, name);
        String contents = RandomStringUtils.randomAlphanumeric((int) expectedSize);
        FileOutputStream outputStream = new FileOutputStream(dataFile);
        IOUtils.write(contents, outputStream);
        outputStream.close();
        
        assertThat(dataFile.length(), is(expectedSize));
        
        ((Observation)target.getParent()).setSbid(42);
        target.setFilename(name);

        when(stateFactory.createState(eq(Type.PROCESSED), eq(target))).thenReturn(next);
        
        CasdaFileProcessingDepositState state = new CasdaFileProcessingDepositState(stateFactory, target,
                parentFolder.getParent());
        state.progress();
        assertThat(target.getFilesize(), is(expectedSizeKb));
    }
}
