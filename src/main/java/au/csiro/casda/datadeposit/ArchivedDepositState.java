package au.csiro.casda.datadeposit;

import au.csiro.casda.entity.observation.EncapsulationFile;

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
 * Default implementation of DepositState for the DepositState.Type.ARCHIVED state that simply transitions to the
 * DEPOSITED state.
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
public class ArchivedDepositState extends DepositState
{
    /**
     * Constructor. (see {@link DepositState})
     * 
     * @param stateFactory
     *            (see {@link DepositState})
     * @param depositable
     *            (see {@link DepositState})
     */
    public ArchivedDepositState(DepositStateFactory stateFactory, Depositable depositable)
    {
        super(DepositState.Type.ARCHIVED, stateFactory, depositable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void progress()
    {
        transitionTo(DepositState.Type.DEPOSITED);
        if(getDepositable() instanceof EncapsulationFile)
        {
        	for(ChildDepositableArtefact artifact : ((EncapsulationFile)getDepositable()).getAllEncapsulatedArtefacts())
        	{
        		//all encapsulated artifacts must be ENCAPSULATED before the EncapsulationFile reaches this state
        		//so progressing them now will cause them to transition to the DEPOSITED state
        		artifact.progressDeposit();
        	}
        }
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
