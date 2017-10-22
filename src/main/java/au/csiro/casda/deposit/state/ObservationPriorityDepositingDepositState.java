package au.csiro.casda.deposit.state;

import au.csiro.casda.datadeposit.DepositState;

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


import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.datadeposit.ParentDepositableArtefactDepositingDepositState;
import au.csiro.casda.entity.observation.ParentDepositableArtefact;

/**
 * Specific class for a DepositState.Type.DEPOSITING type DepositState for Observations.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public class ObservationPriorityDepositingDepositState extends ParentDepositableArtefactDepositingDepositState
{
    /**
     * Constructor, sets the successful transition to DEPOSITING.
     * 
     * @param stateFactory
     *            the DepositStateFactory
     * @param parentDepositableArtefact
     *            the artefact
     */
    public ObservationPriorityDepositingDepositState(DepositStateFactory stateFactory,
            ParentDepositableArtefact parentDepositableArtefact)
    {
        super(stateFactory, parentDepositableArtefact, DepositState.Type.PRIORITY_DEPOSITING, Type.DEPOSITING, true);
    }
}
