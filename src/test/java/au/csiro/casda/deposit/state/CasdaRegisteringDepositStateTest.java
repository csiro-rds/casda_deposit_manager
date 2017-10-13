package au.csiro.casda.deposit.state;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.junit.Test;
import org.mockito.Mockito;

import au.csiro.casda.Utils;
import au.csiro.casda.entity.observation.Catalogue;
import au.csiro.casda.entity.observation.CatalogueType;
import au.csiro.casda.entity.observation.Level7Collection;
import au.csiro.casda.entity.observation.Observation;
import au.csiro.casda.jobmanager.CasdaToolProcessJobBuilder;
import au.csiro.casda.jobmanager.JavaProcessJobFactory;
import au.csiro.casda.jobmanager.JobManager;
import au.csiro.casda.jobmanager.SlurmJobManager;

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
 * Test the implementation of the casda registering deposit state
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 * 
 */
public class CasdaRegisteringDepositStateTest
{
    private Properties props;

    // CASDA-4505 - this test fails if it can't parse / substitute the values correctly
    // will throw an STException
    @Test
    public void testObservation() throws FileNotFoundException, IOException
    {
        props = new Properties();
        props.load(new FileInputStream("src/main/resources/application.properties"));
        String ngas = props.getProperty("artefact.register.command.args");

        CasdaToolProcessJobBuilder casdaToolProcessJobBuilder = new CasdaToolProcessJobBuilder(
                new JavaProcessJobFactory(), "/CASDA/application/casda_deposit_tools/data_deposit",
                "/CASDA/application/casda_deposit_tools/data_deposit", "");
        casdaToolProcessJobBuilder.setCommand("REGISTER_TOOL");
        casdaToolProcessJobBuilder.addCommandArguments(Utils.elStringToArray(ngas));

        Catalogue artefact = new Catalogue(CatalogueType.CONTINUUM_COMPONENT);
        artefact.setFilename("filename");
        artefact.setParent(new Observation(123));
        Map<String, String> map = new HashMap<>();
        map.put("catalogue", "somewhere");
        JobManager jobManager = Mockito.mock(SlurmJobManager.class);
        CasdaRegisteringDepositState state =
                new CasdaRegisteringDepositState(null, artefact, map, casdaToolProcessJobBuilder, jobManager,
                        "/askap/askap_03_dev_fs/casda_deposit", "/askap/askap_03_dev_fs/level7_deposit");

        String[] commandAndArgs = casdaToolProcessJobBuilder.getCommandAndArgs();
        assertEquals("/CASDA/application/casda_deposit_tools/data_deposit/bin/REGISTER_TOOL", commandAndArgs[0]);
        assertEquals("-parent-id", commandAndArgs[1]);
        assertEquals("123", commandAndArgs[2]);
        assertEquals("-infile", commandAndArgs[3]);
        assertEquals("/askap/askap_03_dev_fs/casda_deposit/123/filename", commandAndArgs[4]);
        assertEquals("-staging_volume", commandAndArgs[5]);
        assertEquals("somewhere", commandAndArgs[6]);
        assertEquals("-file_id", commandAndArgs[7]);
        assertEquals("observations-123-catalogues-filename", commandAndArgs[8]);

        state.progress();
    }

    // CASDA-4505 - this test fails if it can't parse / substitute the values correctly
    // will throw an STException
    @Test
    public void testLevel7Collection() throws FileNotFoundException, IOException
    {
        props = new Properties();
        props.load(new FileInputStream("src/main/resources/application.properties"));
        String ngas = props.getProperty("artefact.register.command.args");

        CasdaToolProcessJobBuilder casdaToolProcessJobBuilder = new CasdaToolProcessJobBuilder(
                new JavaProcessJobFactory(), "/CASDA/application/casda_deposit_tools/data_deposit",
                "/CASDA/application/casda_deposit_tools/data_deposit", "");
        casdaToolProcessJobBuilder.setCommand("REGISTER_TOOL");
        casdaToolProcessJobBuilder.addCommandArguments(Utils.elStringToArray(ngas));

        Catalogue artefact = new Catalogue(CatalogueType.DERIVED_CATALOGUE);
        artefact.setFilename("filename");
        artefact.setParent(new Level7Collection(123));
        Map<String, String> map = new HashMap<>();
        map.put("catalogue", "somewhere");
        JobManager jobManager = Mockito.mock(SlurmJobManager.class);
        CasdaRegisteringDepositState state =
                new CasdaRegisteringDepositState(null, artefact, map, casdaToolProcessJobBuilder, jobManager,
                        "/askap/askap_03_dev_fs/casda_deposit", "/askap/askap_03_dev_fs/level7_deposit");

        String[] commandAndArgs = casdaToolProcessJobBuilder.getCommandAndArgs();
        assertEquals("/CASDA/application/casda_deposit_tools/data_deposit/bin/REGISTER_TOOL", commandAndArgs[0]);
        assertEquals("-parent-id", commandAndArgs[1]);
        assertEquals("123", commandAndArgs[2]);
        assertEquals("-infile", commandAndArgs[3]);
        assertEquals("/askap/askap_03_dev_fs/level7_deposit/123/filename", commandAndArgs[4]);
        assertEquals("-staging_volume", commandAndArgs[5]);
        assertEquals("somewhere", commandAndArgs[6]);
        assertEquals("-file_id", commandAndArgs[7]);
        assertEquals("level7-123-catalogues-filename", commandAndArgs[8]);

        state.progress();
    }

}
