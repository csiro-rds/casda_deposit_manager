package au.csiro.casda.deposit.state;

import au.csiro.casda.datadeposit.ChildDepositableArtefact;
import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.datadeposit.ProcessedDepositState;
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
 * Extension of ProcessedDepositState specific to wrapping up the processing of an EncapsulationFile
 * <p>
 * Copyright 2017, CSIRO Australia. All rights reserved.
 */
public class CasdaEncapsulationFileProcessedDepositState extends ProcessedDepositState
{

    /**
     * Constructor
     * 
     * @param stateFactory
     *            see {@link DepositStateFactory}
     * @param encapsulationFile
     *            the encapsulationFile that the state pertains to
     */
    public CasdaEncapsulationFileProcessedDepositState(DepositStateFactory stateFactory,
            EncapsulationFile encapsulationFile)
    {
        super(stateFactory, encapsulationFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void progress()
    {
        //for EncapulationFiles all files encapsulated within will now be set to 'ENCAPSULATED' state
    	for(ChildDepositableArtefact ef : ((EncapsulationFile)getDepositable()).getAllEncapsulatedArtefacts())
    	{
    		ef.progressDeposit();
    	}

        super.progress();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public EncapsulationFile getDepositable()
    {
        return (EncapsulationFile) super.getDepositable();
    }
}
