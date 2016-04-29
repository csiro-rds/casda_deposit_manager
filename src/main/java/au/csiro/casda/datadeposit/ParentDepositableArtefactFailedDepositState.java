package au.csiro.casda.datadeposit;

import au.csiro.casda.entity.observation.ParentDepositableArtefact;

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
 * Implementation of DepositState.Type.FAILED type DepositState for ParentDepositableArtefacts (eg Observation, Level 7
 * Collection). This is reached when any of the artifacts belonging to an observation fail (eg. can't copy).
 * <p>
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
public class ParentDepositableArtefactFailedDepositState extends FailedDepositState
{
    /**
     * Constructor @see DepositState
     * 
     * @param stateFactory
     *            see @see DepositState
     * @param parentDepositableArtefact
     *            see @see DepositState
     */
    public ParentDepositableArtefactFailedDepositState(DepositStateFactory stateFactory,
            ParentDepositableArtefact parentDepositableArtefact)
    {
        super(stateFactory, parentDepositableArtefact);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void recover()
    {
        for (Depositable depositableArtefact : getDepositable().getDepositableArtefacts())
        {
            if (depositableArtefact.isFailedDeposit())
            {
                depositableArtefact.recoverDeposit();
            }
        }
        super.recover();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParentDepositableArtefact getDepositable()
    {
        return (ParentDepositableArtefact) super.getDepositable();
    }

}
