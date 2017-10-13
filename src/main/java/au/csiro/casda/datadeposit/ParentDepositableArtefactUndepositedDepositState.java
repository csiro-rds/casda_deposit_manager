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
 * Implementation of a DepositState.Type.UNDEPOSITED type DepositState for ParentDepositbaleArtefacts (eg Observation,
 * Level 7 Collection) that simply transitions to the DEPOSITING state.
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
public class ParentDepositableArtefactUndepositedDepositState extends DepositState
{
	private Type nextStateType;
    /**
     * Constructor. (see {@link DepositState})
     * 
     * @param stateFactory
     *            (see {@link DepositState})
     * @param parentDepositableArtefact
     *            (see {@link DepositState})
     * @param nextStateType
     * 			  the state to transition to after this one. level 7 collections should skip priority depositing
     */
    public ParentDepositableArtefactUndepositedDepositState(DepositStateFactory stateFactory,
            ParentDepositableArtefact parentDepositableArtefact, Type nextStateType)
    {
        super(DepositState.Type.UNDEPOSITED, stateFactory, parentDepositableArtefact);
        this.nextStateType = nextStateType;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void progress()
    {
        transitionTo(nextStateType);
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
