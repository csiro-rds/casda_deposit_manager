package au.csiro.casda.deposit.health;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import au.csiro.casda.deposit.services.VoToolsService;

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
 * This health indicator calls the VoTools health service to determine if it is running.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
@Component
public class VoToolsHealth implements HealthIndicator
{

    @Autowired
    private VoToolsService voToolsService;

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.boot.actuate.health.HealthIndicator#health()
     */
    @Override
    public Health health()
    {
        try
        {
            Map<String, Object> response = voToolsService.getHealth();
            if ("UP".equals(response.get("status")))
            {
                return new Health.Builder().up().build();
            }
            else
            {
                return new Health.Builder().down().withDetail("health", response).build();
            }
        }
        catch (RuntimeException e)
        {
            return new Health.Builder().down(e).build();
        }

    }

}
