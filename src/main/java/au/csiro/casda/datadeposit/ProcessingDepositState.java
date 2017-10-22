package au.csiro.casda.datadeposit;

import au.csiro.casda.entity.observation.EvaluationFile;
import au.csiro.casda.entity.observation.ImageDerivedProduct;
import au.csiro.casda.entity.observation.Thumbnail;

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
 * Default implementation of DepositState for the DepositState.Type.PROCESSING state that simply transitions to the
 * PROCESSED state.
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
public class ProcessingDepositState extends DepositState
{
    /**
     * Constructor. (see {@link DepositState})
     * 
     * @param stateFactory
     *            (see {@link DepositState})
     * @param depositable
     *            (see {@link DepositState})
     */
    public ProcessingDepositState(DepositStateFactory stateFactory, Depositable depositable)
    {
        super(DepositState.Type.PROCESSING, stateFactory, depositable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void progress()
    {
    	//will progress all spectra & moment maps o encapsulating state, but only thumbnails belonging to moment maps
    	//or spectra will follow this path, all other files (including image cube thumbnails) will transition to staging
    	if(	getDepositable() instanceof ImageDerivedProduct ||
    		getDepositable() instanceof EvaluationFile ||
    		(getDepositable() instanceof Thumbnail && ((Thumbnail)getDepositable()).getEncapsulationFile() != null))
    	{
    		 transitionTo(DepositState.Type.ENCAPSULATING);
    	}
    	else
    	{
    		 transitionTo(DepositState.Type.PROCESSED);
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
