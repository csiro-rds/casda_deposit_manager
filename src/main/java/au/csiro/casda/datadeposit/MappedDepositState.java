package au.csiro.casda.datadeposit;

/*
 * #%L
 * CSIRO ASKAP Science Data Archive
 * %%
 * Copyright (C) 2017 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

/**
 * Default implementation of DepositState for the DepositState.Type.MAPPED state that simply transitions to the
 * ARCHIVING state.
 * <p>
 * Copyright 2017, CSIRO Australia. All rights reserved.
 */
public class MappedDepositState extends DepositState
{
    /**
     * Constructor. (see {@link DepositState})
     * 
     * @param stateFactory
     *            (see {@link DepositState})
     * @param depositable
     *            (see {@link DepositState})
     */
    public MappedDepositState(DepositStateFactory stateFactory, Depositable depositable)
    {
        super(DepositState.Type.MAPPED, stateFactory, depositable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void progress()
    {
        transitionTo(DepositState.Type.ARCHIVING);
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
