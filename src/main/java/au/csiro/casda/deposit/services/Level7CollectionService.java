package au.csiro.casda.deposit.services;

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


import java.util.EnumSet;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.deposit.jpa.Level7CollectionRepository;
import au.csiro.casda.entity.observation.Level7Collection;

/**
 * Service to retrieve and update level 7 collections information
 *
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
@Service
public class Level7CollectionService
{

    @Autowired
    private Level7CollectionRepository level7CollectionRepository;

    @Autowired
    @Value("${job.manager.recent.age.hours}")
    private int maxAgeOfRecentCompletedJobs;

    
    /**
     * Retrieve a list of level 7 collections in the supplied state.
     * 
     * @param depositStateType
     *            of DepositState.Type
     * @return List of level 7 collections of that Type
     */
    public List<Level7Collection> findLevel7CollectionsByDepositStateType(DepositState.Type depositStateType)
    {
        return findLevel7CollectionsByDepositStateType(EnumSet.of(depositStateType));
    }

    /**
     * Retrieve a list of level 7 collections in the supplied states.
     * 
     * @param depositStateTypes
     *            Set of DepositState.Type
     * @return List of level 7 collections matching the types
     */
    public List<Level7Collection> findLevel7CollectionsByDepositStateType(EnumSet<DepositState.Type> depositStateTypes)
    {
        return level7CollectionRepository.findLevel7CollectionsByDepositStateType(depositStateTypes);
    }

    /**
     * Retrieve a list of level 7 collections recently completed (e.g. in the last 2 days).
     * 
     * @return List of level 7 collections recently completed.
     */
    public List<Level7Collection> findRecentlyCompletedLevel7Collections()
    {
        DateTime recentCutoff = DateTime.now(DateTimeZone.UTC).minusHours(maxAgeOfRecentCompletedJobs);
        return level7CollectionRepository.findLevel7CollectionsCompletedSince(recentCutoff);
    }

}
