package au.csiro.casda.deposit.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

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
 * This health indicator runs a PostGIS specific query against the database to ensure that PostGIS is correctly
 * configured and running in our target database.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
@Component
public class PostGISHealth implements HealthIndicator
{

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
            // Run a query that needs postGIS, it will fail if postGIS is not active.
            jdbcTemplate.queryForObject("select ST_GeomFromText('POINT(0 0)', 0)", Object.class);
            return new Health.Builder().up().build();
        }
        catch (Exception e)
        {
            return new Health.Builder().down(e).build();
        }

    }

}
