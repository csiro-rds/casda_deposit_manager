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
 * Default implementation of DepositState for the DepositState.Type.UNDEPOITED state that simply transitions to the
 * PROCESSING state.
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
public class UndepositedDepositState extends DepositState
{
    /**
     * Constructor. (see {@link DepositState})
     * 
     * @param depositStateFactory
     *            (see {@link DepositState})
     * @param depositable
     *            (see {@link DepositState})
     */
    public UndepositedDepositState(DepositStateFactory depositStateFactory, Depositable depositable)
    {
        super(DepositState.Type.UNDEPOSITED, depositStateFactory, depositable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void progress()
    {
    	if((getDepositable() instanceof EncapsulationFile && isEncapsulationFileReadyToProgress()) ||
    		!(getDepositable() instanceof EncapsulationFile))
    	{
    		transitionTo(DepositState.Type.PROCESSING);
    	}
        
    }
    
    private boolean isEncapsulationFileReadyToProgress()
    {
    	for(ChildDepositableArtefact artefact :((EncapsulationFile)getDepositable()).getAllEncapsulatedArtefacts())
    	{
    		if(!artefact.isEncapsulating())
    		{
    			return false;
    		}
    	}
    	return true;
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
