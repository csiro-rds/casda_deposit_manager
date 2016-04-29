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

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import au.csiro.casda.Utils;
import au.csiro.casda.datadeposit.ArchivingDepositState;
import au.csiro.casda.deposit.TestAppConfig;
import au.csiro.casda.entity.observation.ImageCube;
import au.csiro.casda.entity.observation.Observation;
import au.csiro.casda.jobmanager.JavaProcessJob;
import au.csiro.casda.jobmanager.ProcessJob;

/**
 * Verify the functions of the CasdaArchivingDepositState class.
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { TestAppConfig.class })
@ActiveProfiles("local")
public class ObservationArchivingDepositStateTest extends BaseArchivingDepositStateTest
{
    protected CasdaArchivingDepositState getState(String statusCommandString, String putCommandString)
    {
        String[] statusCommand = Utils.elStringToList(statusCommandString).toArray(new String[0]);
        String[] putCommand = Utils.elStringToList(putCommandString).toArray(new String[0]);
        Map<String, String> volMap = new HashMap<>();
        volMap.put("image_cube", "vol_A");
        Observation observation;
        observation = new Observation();
        observation.setSbid(1234);
        ImageCube imageCube = new ImageCube();
        imageCube.setFilename("bob.xml");
        observation.addImageCube(imageCube);
        imageCube.setDepositState(new ArchivingDepositState(stateFactory, observation));

        ProcessJob job = new JavaProcessJob("jobId", "type", null, null, statusCommand);
        when(archiveStatusBuilder.createJob(anyString(), anyString())).thenReturn(job);

        job = new JavaProcessJob("jobId", "type", null, null, putCommand);
        when(archivePutBuilder.createJob(anyString(), anyString())).thenReturn(job);

        return new ObservationArchivingDepositState(stateFactory, imageCube, volMap, ngasService, archiveStatusBuilder,
                archivePutBuilder, singleJobMonitorFactory);
    }

    protected String getTestFileId()
    {
        return "observations-1234-image_cubes-bob.xml";
    }

    protected void checkTestAppenderForNgasError(Exception exception)
    {
        log4jTestAppender.verifyLogMessage(Level.WARN,
                "E088] [Deposit Job Workflow - Connection to NGAS lost] ["
                        + "Unable to contact NGAS for artefact bob.xml " + "for observation 1234 in ARCHIVING state.",
                exception);
    }
}
