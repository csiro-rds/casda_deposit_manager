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
public class ParentDepositableArtefactArchivingDepositState extends DepositState
{
    private Type successTransitionState;

    /**
     * Constructor. (see {@link DepositState})
     * 
     * @param stateFactory
     *            (see {@link DepositState})
     * @param parentDepositableArtefact
     *            (see {@link DepositState})
     * @param successTransitionState
     *            the state to transition when successfully progressing the parentDepositableArtefact
     */
    public ParentDepositableArtefactArchivingDepositState(DepositStateFactory stateFactory,
            ParentDepositableArtefact parentDepositableArtefact, Type successTransitionState)
    {
        super(DepositState.Type.ARCHIVING, stateFactory, parentDepositableArtefact);
        this.successTransitionState = successTransitionState;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void progress()
    {
        boolean allDepositablesFinished = true;
        boolean anyDepositableFailed = false;
        for (Depositable depositableArtefact : getDepositable().getDepositableArtefacts())
        {
            if (depositableArtefact.isFailedDeposit())
            {
                anyDepositableFailed = true;
            }
            else if (depositableArtefact.isEncapsulating() || depositableArtefact.isEncapsulated())
            {
                // Encapsulated objects get progressed by their parent encapsulation, so we leave them alone
                continue;
            }
            else if (!depositableArtefact.isDeposited())
            {
                allDepositablesFinished = false;
                depositableArtefact.progressDeposit();
            }
        }
        
        if (allDepositablesFinished)
        {
            if (anyDepositableFailed)
            {
                transitionTo(DepositState.Type.FAILED);
            }
            else
            {
                transitionTo(successTransitionState);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ParentDepositableArtefact getDepositable()
    {
        return (ParentDepositableArtefact) super.getDepositable();
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
