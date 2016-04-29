package au.csiro.casda.entity;

import au.csiro.casda.datadeposit.AbstractDepositableArtefactStateRecoverTest;
import au.csiro.casda.datadeposit.DepositState.Type;
import au.csiro.casda.datadeposit.DepositableArtefact;
import au.csiro.casda.entity.observation.Observation;

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
 * CasdaDepositableArtefactRecoverEntity Test - when failure occurs the state of the entity rolls back to a state that
 * it can continue from
 * <p>
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
public class CasdaDepositableArtefactEntityRecoveryTest extends AbstractDepositableArtefactStateRecoverTest
{
    /*
     * (non-Javadoc)
     * 
     * @see AbstractDepositableArtefactTest#createDepositableArtefact()
     */
    @Override
    protected DepositableArtefact createDepositableArtefact()
    {
        DummyObservationDepositableArtefact result =
                new DummyObservationDepositableArtefact(new Observation(1234), "filename.xml");
        result.setDepositStateFactory(depositStateFactory);
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see AbstractDepositableArtefactTest#createDepositableArtefactInState(DepositState.Type)
     */
    protected DepositableArtefact createDepositableArtefactInState(Type depositState)
    {
        CasdaDepositableArtefactEntity depositableArtefact;
        depositableArtefact = new DummyObservationDepositableArtefact(new Observation(1234), "filename.xml");
        depositableArtefact.setDepositStateFactory(depositStateFactory);
        depositableArtefact.setDepositState(depositStateFactory.createState(depositState, depositableArtefact));
        return depositableArtefact;
    }

}
