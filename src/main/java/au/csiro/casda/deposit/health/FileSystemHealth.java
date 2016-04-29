package au.csiro.casda.deposit.health;

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


import java.io.File;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Health check for the required file systems
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 * 
 */
@Component
public class FileSystemHealth implements HealthIndicator
{

    private String depositObservationParentDirectory;
    private String depositToolsWorkingDirectory;
    private String depositToolsInstallationDirectory;
    private String depositTempfileDir;
    private String depositLevel7CollectionsDir;

    /**
     * Create a new FileSystemHealth instance.
     * 
     * @param depositObservationParentDirectory
     *            Folder which should be read-write.
     * @param depositToolsWorkingDirectory
     *            Folder which should be read-write.
     * @param depositToolsInstallationDirectory
     *            Folder which should be readable.
     * @param depositTempfileDir
     *            Folder which should be read-write.
     * @param depositLevel7CollectionsDir
     *            Folder which should be read-write.
     */
    @Autowired
    public FileSystemHealth(@Value("${deposit.observation.parent.directory}") String depositObservationParentDirectory,
            @Value("${deposit.tools.working.directory}") String depositToolsWorkingDirectory,
            @Value("${deposit.tools.installation.directory}") String depositToolsInstallationDirectory,
            @Value("${deposit.tempfile.dir}") String depositTempfileDir,
            @Value("${deposit.level7.collections.dir}") String depositLevel7CollectionsDir)
    {
        this.depositObservationParentDirectory = depositObservationParentDirectory;
        this.depositToolsWorkingDirectory = depositToolsWorkingDirectory;
        this.depositToolsInstallationDirectory = depositToolsInstallationDirectory;
        this.depositTempfileDir = depositTempfileDir;
        this.depositLevel7CollectionsDir = depositLevel7CollectionsDir;
    }

    @Override
    public Health health()
    {
        Health.Builder health = new Health.Builder().up();

        addDirectoryCheck(health, "deposit.observation.parent.directory", depositObservationParentDirectory, true);
        addDirectoryCheck(health, "deposit.tools.working.directory", depositToolsWorkingDirectory, true);
        addDirectoryCheck(health, "deposit.tools.installation.directory", depositToolsInstallationDirectory, false);
        addDirectoryCheck(health, "deposit.tempfile.dir", depositTempfileDir, true);
        addDirectoryCheck(health, "deposit.level7.collections.dir", depositLevel7CollectionsDir, true);
        return health.build();
    }

    private void addDirectoryCheck(Builder health, String paramName, String directory, boolean needWrite)
    {
        File file = Paths.get(directory).toFile();

        String message = null;
        if (!file.exists())
        {
            message = "Directory %s does not exist.";
        }
        else if (!file.isDirectory())
        {
            message = "%s is not a directory.";
        }
        else if (!file.canRead())
        {
            message = "Directory %s cannot be read.";
        }
        else if (needWrite && !file.canWrite())
        {
            message = "Directory %s cannot be modified.";
        }

        if (message != null)
        {
            health.down();
            health.withDetail(paramName, String.format(message, directory));
        }
        else
        {
            health.withDetail(paramName, directory);
        }
    }

}