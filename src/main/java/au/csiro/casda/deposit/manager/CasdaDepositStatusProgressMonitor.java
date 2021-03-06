package au.csiro.casda.deposit.manager;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositState.Type;
import au.csiro.casda.datadeposit.DepositableArtefact;
import au.csiro.casda.deposit.DepositManagerEvents;
import au.csiro.casda.deposit.jpa.ObservationRepository;
import au.csiro.casda.entity.CasdaDepositableEntity;
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
 * This class monitors the current status of all observations and depositable entities that are currently depositing
 * (ie. not in a DEPOSITED or FAILED state). It then logs an error if the observation doesn't complete after a
 * configurable timeout period, or an artifact does not progress after a configurable timeout period.
 * <p>
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
@Component
public class CasdaDepositStatusProgressMonitor
{
    private ObservationRepository observationRepository;

    private static final Logger logger = LoggerFactory.getLogger(CasdaDepositStatusProgressMonitor.class);

    private int observationTimeout;
    private int artifactTimeout;

    /**
     * Autowired constructor
     * 
     * @param observationTimeout
     *            Time in millis before the observation timeout is logged
     * @param artifactTimeout
     *            Time in millis before an artifact timeout is logged
     * @param observationRepository
     *            The JPA observation repository
     */
    @Autowired
    public CasdaDepositStatusProgressMonitor(
            @Value("${deposit.observation.completion.timeout.millis}") int observationTimeout,
            @Value("${deposit.artifact.progression.timeout.millis}") int artifactTimeout,
            ObservationRepository observationRepository)
    {
        this.observationTimeout = observationTimeout;
        this.artifactTimeout = artifactTimeout;
        this.observationRepository = observationRepository;
    }

    /**
     * Checks the status of all Depositables at the given time in millis.
     * @param atTimeMillis the time to check
     */
    @Transactional
    public void checkDepositableStatuses(long atTimeMillis)
    {
        EnumSet<DepositState.Type> typeList = EnumSet.allOf((DepositState.Type.class));
        typeList.removeAll
                (Arrays.asList(DepositState.Type.DEPOSITED, DepositState.Type.FAILED));
        List<Observation> observations = 
                observationRepository.findDepositingObservationsForDepositStateTypeOrdered(typeList);

        for (Observation observation : observations)
        {
            // check if the observation has taken too long to complete. if redeposit started time is empty checks against
        	// deposit start time, if not null will check against redeposit start time
            if (atTimeMillis - (observation.getRedepositStarted() == null ? observation.getDepositStarted().getMillis() 
            		: observation.getRedepositStarted().getMillis()) > observationTimeout)
            {
                logger.error(DepositManagerEvents.E076.messageBuilder().add(observation.getSbid()).toString());
            }

            // check if every artifact has taken too long to progress (ignoring those being archived or fully deposited)
            for (DepositableArtefact artifact : observation.getDepositableArtefacts())
            {
                DateTime depositStateChanged = artifact.getDepositStateChanged();
                DepositState.Type depositStateType = null;
                if (artifact instanceof CasdaDepositableEntity)
                {
                    depositStateType = ((CasdaDepositableEntity) artifact).getDepositStateType();
                }

                if (depositStateChanged != null && depositStateType != Type.DEPOSITED
                        && depositStateType != Type.ARCHIVING
                        && atTimeMillis - depositStateChanged.getMillis() > artifactTimeout)
                {
                    logger.error(DepositManagerEvents.E074.messageBuilder().add(artifact.getFilename())
                            .add(observation.getSbid()).toString());
                }
            }

        }
    }
}
