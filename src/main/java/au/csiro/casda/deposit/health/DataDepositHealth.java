package au.csiro.casda.deposit.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import au.csiro.casda.deposit.manager.DepositManagerService;

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
 * HealthIndicator for DataDeposit service.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
@Component
public class DataDepositHealth implements HealthIndicator
{
    @Autowired
    private DepositManagerService depositManagerService;

    @Override
    public Health health()
    {
        Health.Builder healthBuilder = new Health.Builder();
        if (depositManagerService.tooLongSinceLastSuccessfulRtcPoll())
        {
            // if it's been too long since the last successful poll, say this is down
            healthBuilder.down();
        }
        else
        {
            healthBuilder.up();
        }
        return healthBuilder
                .withDetail("last successful RTC poll", depositManagerService.getLastSuccessfulRtcPoll().toString())
                .withDetail("deposit observation parent directory",
                        depositManagerService.getDepositObservationParentDirectory()).build();
    }

}
