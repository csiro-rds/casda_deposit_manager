package au.csiro.casda.deposit.manager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import au.csiro.casda.datadeposit.DepositStateChangeListener;
import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.deposit.jpa.Level7CollectionRepository;
import au.csiro.casda.entity.observation.Level7Collection;
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
 * Component that can be used to progress a Level 7 Collection through its deposit states.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class Level7DepositProgressor
{
    @Autowired
    private Level7CollectionRepository level7CollectionRepository;

    @Autowired
    private JobManager jobManager;

    @Autowired
    private DepositStateFactory depositStateFactory;

    @Autowired
    @Qualifier("CasdaLevel7DepositStateChangeListener")
    private DepositStateChangeListener depositStateChangeListener;

    /**
     * Progress a level 7 collection (identified by the collection id) through its deposit states.
     * 
     * @param dapCollectionId
     *            the level 7 collection id (corresponds with the Data Access Portal data collection id)
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void progressCollection(Long dapCollectionId)
    {
        Level7Collection observation = level7CollectionRepository.findByDapCollectionId(dapCollectionId);
        progressCollection(observation);
        // Level 7 Collection and all children are saved automatically by the transaction
    }

    private void progressCollection(Level7Collection level7Collection)
    {
        level7Collection.setDepositStateFactory(depositStateFactory);
        level7Collection.setDepositStateChangeListener(depositStateChangeListener);

        if (!level7Collection.isDeposited() && !level7Collection.isFailedDeposit())
        {
            level7Collection.progressDeposit();
        }
    }
}
