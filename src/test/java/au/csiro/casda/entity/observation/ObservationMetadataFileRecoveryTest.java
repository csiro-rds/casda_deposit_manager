package au.csiro.casda.entity.observation;

import au.csiro.casda.datadeposit.AbstractDepositableArtefactStateRecoverTest;
import au.csiro.casda.datadeposit.DepositState.Type;
import au.csiro.casda.datadeposit.DepositableArtefact;

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
 * ObservationMetadataFile Recover (checkpoint) Test
 * <p>
 * Copyright 2014, CSIRO Australia
 * All rights reserved.
 */
public class ObservationMetadataFileRecoveryTest extends AbstractDepositableArtefactStateRecoverTest
{
    
    /* (non-Javadoc)
     * @see AbstractDepositableArtefactTest#createDepositableArtefact()
     */
    @Override
    protected DepositableArtefact createDepositableArtefact()
    {
        ObservationMetadataFile result = new ObservationMetadataFile(new Observation(1234));
        result.getParent().setDepositStateFactory(depositStateFactory);
        return result;
    }

    /* (non-Javadoc)
     * @see AbstractDepositableArtefactTest#createDepositableArtefactInState(DepositState.Type)
     */
    @Override
    protected DepositableArtefact createDepositableArtefactInState(Type depositStateType)
    {
        ObservationMetadataFile observationMetadataFile = new ObservationMetadataFile(new Observation(1234));
        observationMetadataFile.getParent().setDepositStateFactory(depositStateFactory);
        observationMetadataFile.setDepositState(
                depositStateFactory.createState(depositStateType, observationMetadataFile));
        return observationMetadataFile;
    }
}
