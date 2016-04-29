package au.csiro.casda.deposit;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import au.csiro.casda.jobmanager.CasdaToolProcessJobBuilder;
import au.csiro.casda.jobmanager.ProcessJobBuilder.ProcessJobFactory;

/**
 * 
 * Injected factory that creates CasdaToolProcessJobBuilders
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
@Component
public class CasdaToolProcessJobBuilderFactory
{
    private ProcessJobFactory processJobFactory;

    private String depositToolsInstallationDirectory;

    private String depositToolsScriptExtension;

    private String depositToolsWorkingDirectory;

    /**
     * @param processJobFactory
     *            a ProcessJobFactory used to instantiate a specific concrete subclass of ProcessJob
     * @param depositToolsWorkingDirectory
     *            used to configure the working directory of the JobProcessBuilder returned by
     *            createCasdaToolProcessJobBuilder
     * @param depositToolsInstallationDirectory
     *            used to configure the commandPath of the JobProcessBuilder returned by
     *            createCasdaToolProcessJobBuilder
     * @param depositToolsScriptExtension
     *            used to configure the command extension of the JobProcessBuilder returned by
     *            createCasdaToolProcessJobBuilder
     */
    @Autowired
    public CasdaToolProcessJobBuilderFactory(ProcessJobFactory processJobFactory,
            @Value("${deposit.tools.working.directory}") String depositToolsWorkingDirectory,
            @Value("${deposit.tools.installation.directory}") String depositToolsInstallationDirectory,
            @Value("${deposit.tools.script.extension}") String depositToolsScriptExtension)
    {
        this.processJobFactory = processJobFactory;
        this.depositToolsWorkingDirectory = depositToolsWorkingDirectory;
        this.depositToolsScriptExtension = depositToolsScriptExtension;
        this.depositToolsInstallationDirectory = depositToolsInstallationDirectory;
    }

    /**
     * Create a new CasdaToolProcessJobBuilder
     * 
     * @return a new CasdaToolProcessJobBuilder
     */
    public CasdaToolProcessJobBuilder createBuilder()
    {
        return new CasdaToolProcessJobBuilder(this.processJobFactory, this.depositToolsWorkingDirectory,
                this.depositToolsInstallationDirectory, this.depositToolsScriptExtension);
    }
}