package au.csiro.casda.datadeposit;

import au.csiro.casda.deposit.jdbc.SimpleJdbcRepository;
import au.csiro.casda.entity.observation.FitsObject;

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
 * Default implementation of DepositState for the DepositState.Type.REGISTERED state that simply transitions to the
 * ARCHIVING state.
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
public class RegisteredDepositState extends DepositState
{
    private SimpleJdbcRepository simpleJdbcRepository;

    /**
     * Constructor. (see {@link DepositState})
     * 
     * @param stateFactory
     *            (see {@link DepositState})
     * @param depositable
     *            (see {@link DepositState})
     * @param simpleJdbcRepository
     *            the repository for running arbitrary SQL statements
     */
    public RegisteredDepositState(DepositStateFactory stateFactory, Depositable depositable,
            SimpleJdbcRepository simpleJdbcRepository)
    {
        super(DepositState.Type.REGISTERED, stateFactory, depositable);
        this.simpleJdbcRepository = simpleJdbcRepository;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void progress()
    {
        if (getDepositable() instanceof FitsObject)
        {
            String imageType = ((FitsObject) getDepositable()).getType();
            if (simpleJdbcRepository.isImageTypeIncludeCoverage(imageType))
            {
                transitionTo(DepositState.Type.MAPPING);
                return;
            }
        }
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
