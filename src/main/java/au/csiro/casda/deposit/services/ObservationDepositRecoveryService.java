package au.csiro.casda.deposit.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import au.csiro.casda.datadeposit.DepositStateChangeListener;
import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.datadeposit.DepositableArtefact;
import au.csiro.casda.deposit.exception.ArtefactInvalidStateRecoveryException;
import au.csiro.casda.deposit.exception.ArtefactNotFoundException;
import au.csiro.casda.deposit.exception.ObservationNotFailedRecoveryException;
import au.csiro.casda.deposit.exception.ObservationNotFoundException;
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
 * Service to perform recover on selected failed jobs.
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ObservationDepositRecoveryService
{
    private ObservationRepository observationRepository;

    private DepositStateFactory depositStateFactory;

    private DepositStateChangeListener depositStateChangeListener;

    /**
     * Constructor
     * 
     * @param observationRepository
     *            an ObservationRepository used to retrieve and update Observations
     * @param depositStateFactory
     *            a DepositStateFactory used to create the correct DepositState instance for a retrieved Observation
     * @param depositStateChangeListener
     *            a DepositStateChangeListener assigned to a retrieved Observation
     */
    @Autowired
    public ObservationDepositRecoveryService(
            ObservationRepository observationRepository,
            DepositStateFactory depositStateFactory,
            @Qualifier("CasdaObservationDepositStateChangeListener") DepositStateChangeListener depositStateChangeListener)
    {
        this.observationRepository = observationRepository;
        this.depositStateFactory = depositStateFactory;
        this.depositStateChangeListener = depositStateChangeListener;
    }

    /**
     * Recovers a failed observation (identified by the sbid).
     * 
     * @param sbid
     *            the scheduling block ID of the observation
     * @throws ObservationNotFailedRecoveryException
     *             when an observation that isn't failed is attempted to be recovered
     * @throws ObservationNotFoundException
     *             if the observation (sbid) requested doesnt exist
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void recoverObservation(Integer sbid) throws ObservationNotFailedRecoveryException,
            ObservationNotFoundException
    {
        Observation observation = observationRepository.findBySbid(sbid);
        if (observation == null)
        {
            throw new ObservationNotFoundException(sbid);
        }
        recoverObservation(observation);
    }

    private void recoverObservation(Observation observation) throws ObservationNotFailedRecoveryException
    {
        observation.setDepositStateFactory(depositStateFactory);
        observation.setDepositStateChangeListener(depositStateChangeListener);

        if (observation.isFailedDeposit())
        {
            observation.recoverDeposit();
        }
        else
        {
            throw new ObservationNotFailedRecoveryException(observation.getSbid());
        }
    }

    /**
     * Recovers a failed artefact (identified by the file id) for an observation (identified by the sbid).
     * 
     * @param sbid
     *            the scheduling block ID of the observation
     * @param fileId
     *            the file id of the artefact to recover
     * @throws ArtefactInvalidStateRecoveryException
     *             when attempting to recover an artefact that has not failed, or its observation is failed
     * @throws ArtefactNotFoundException
     *             if the requested artefact with the given file id doesn't exist for the observation (sbid)
     * @throws ObservationNotFoundException
     *             when no observation with the given sbid exists
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void recoverArtefact(Integer sbid, String fileId) throws ObservationNotFoundException,
            ArtefactNotFoundException, ArtefactInvalidStateRecoveryException
    {
        Observation observation = observationRepository.findBySbid(sbid);
        if (observation == null)
        {
            throw new ObservationNotFoundException(sbid);
        }

        if (observation.isFailedDeposit())
        {
            throw new ArtefactInvalidStateRecoveryException(sbid, fileId,
                    "Observation is in a failed state, so unable to recover artefact");
        }

        // set the deposit state factory here, so when you get the depositable artefacts, it will
        // set the deposit state factory on them
        observation.setDepositStateFactory(depositStateFactory);
        
        DepositableArtefact depositableArtefact = null;
        
        for (DepositableArtefact artefact : observation.getDepositableArtefacts())
        {
            if (fileId.equals(artefact.getFileId()))
            {
                depositableArtefact = artefact;
            }
        }

        if (depositableArtefact == null)
        {
            throw new ArtefactNotFoundException(sbid, fileId);
        }

        recoverArtefact(observation, depositableArtefact);
    }

    private void recoverArtefact(Observation observation, DepositableArtefact depositableArtefact)
            throws ArtefactInvalidStateRecoveryException
    {
        depositableArtefact.setDepositStateChangeListener(depositStateChangeListener);

        if (depositableArtefact.isFailedDeposit())
        {
            depositableArtefact.recoverDeposit();
        }
        else
        {
            throw new ArtefactInvalidStateRecoveryException(observation.getSbid(), depositableArtefact.getFileId(),
                    "Artefact is not in a failed state");
        }
    }
}
