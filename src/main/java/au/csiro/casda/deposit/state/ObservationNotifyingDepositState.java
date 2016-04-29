package au.csiro.casda.deposit.state;

import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositStateFactory;
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
 * Implementation of an Observation-specific DepositState for the DepositState.Type.NOTIFYING state that simply
 * transitions to the DEPOSITED state.
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
public class ObservationNotifyingDepositState extends DepositState
{
    /**
     * Constructor. (see {@link DepositState})
     * 
     * @param stateFactory
     *            (see {@link DepositState})
     * @param observation
     *            (see {@link DepositState})
     */
    public ObservationNotifyingDepositState(DepositStateFactory stateFactory, Observation observation)
    {
        super(DepositState.Type.NOTIFYING, stateFactory, observation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void progress()
    {
        transitionTo(DepositState.Type.DEPOSITED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCheckpointState()
    {
        return true;
    }
}
