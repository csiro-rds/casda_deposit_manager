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
 * Default implementation of DepositState for the DepositState.Type.DEPOSITED end-state
 * <p>
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
public class DepositedDepositState extends DepositState
{
    /**
     * Constructor @see DepositState
     * 
     * @param stateFactory
     *            see @see DepositState
     * @param depositable
     *            see @see DepositState
     */
    public DepositedDepositState(DepositStateFactory stateFactory, Depositable depositable)
    {
        super(DepositState.Type.DEPOSITED, stateFactory, depositable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void progress()
    {
        throw new IllegalEventException("progress", this);
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
