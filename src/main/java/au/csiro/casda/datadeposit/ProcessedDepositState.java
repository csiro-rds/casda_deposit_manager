package au.csiro.casda.datadeposit;

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
 * Default implementation of DepositState for the DepositState.Type.PROCESSED state that simply transitions to the
 * STAGING state, or for files which need to be encapsulated the ENCAPSULATING state.
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
public class ProcessedDepositState extends DepositState
{

    /**
     * Constructor. (see {@link DepositState})
     * 
     * @param stateFactory
     *            (see {@link DepositState})
     * @param depositable
     *            (see {@link DepositState})
     */
    public ProcessedDepositState(DepositStateFactory stateFactory, Depositable depositable)
    {
        super(DepositState.Type.PROCESSED, stateFactory, depositable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void progress()
    {
    	transitionTo(DepositState.Type.STAGING); 
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
