package au.csiro.casda.deposit.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import au.csiro.casda.jobmanager.JobManager;

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
 * HealthIndicator for Slurm.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
@Component
public class SlurmHealth implements HealthIndicator
{
    private static final Logger logger = LoggerFactory.getLogger(SlurmHealth.class);
    
    @Autowired
    private JobManager jobManager;

    @Override
    public Health health()
    {
        Health.Builder healthBuilder = new Health.Builder();
        try
        {
            // try running a slurm command, and if it works, slurm is available.
            Integer runningJobsCount = jobManager.getRunningJobsCount("stage_artefact");
            logger.debug("number of running stage_artefact jobs: {}", runningJobsCount);
            healthBuilder.up();
        }
        catch (Throwable t)
        {
            logger.warn("Slurm is not responding", t);
            healthBuilder.down();
            healthBuilder.withDetail("Error", t.getMessage());
        }

        return healthBuilder.build();
    }

}
