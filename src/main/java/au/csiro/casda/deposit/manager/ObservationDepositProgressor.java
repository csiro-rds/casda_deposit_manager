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
import au.csiro.casda.deposit.jpa.ObservationRepository;
import au.csiro.casda.entity.observation.Observation;

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
 * Component that can be used to progress an Observation through its deposit states.
 * <p>
 * Copyright 2014, CSIRO Australia
 * All rights reserved.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class ObservationDepositProgressor
{
    @Autowired
    private ObservationRepository observationRepository;

    @Autowired
    private DepositStateFactory depositStateFactory;

    @Autowired
    @Qualifier("CasdaObservationDepositStateChangeListener")
    private DepositStateChangeListener depositStateChangeListener;

    /**
     * Progress an observation (identified by the sbid) through its deposit states.
     * 
     * @param sbid
     *            the scheduling block ID of the observation
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void progressObservation(Integer sbid)
    {
        Observation observation = observationRepository.findBySbid(sbid);
        progressObservation(observation);
        // Observation and all children are saved automatically by the transaction
    }

    private void progressObservation(Observation observation)
    {
        observation.setDepositStateFactory(depositStateFactory);
        observation.setDepositStateChangeListener(depositStateChangeListener);

        if (!observation.isDeposited() && !observation.isFailedDeposit())
        {
            observation.progressDeposit();
        }
    }
}
