package au.csiro.casda.datadeposit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.csiro.casda.entity.observation.EncapsulationFile;
import au.csiro.casda.entity.observation.Level7Collection;
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
 * Implementation of DepositState for the DepositState.Type.VALIDATING pre-initial state. This will process and validate
 * all files in the level 7 collection.
 * <p>
 * Copyright 2017, CSIRO Australia All rights reserved.
 */
public class Level7CollectionValidatingDepositState extends DepositState
{
    private static final Logger logger = LoggerFactory.getLogger(Level7CollectionValidatingDepositState.class);
    
    /**
     * Constructor @see DepositState
     * 
     * @param stateFactory
     *            see @see DepositState
     * @param depositable
     *            see @see DepositState
     */
    public Level7CollectionValidatingDepositState(DepositStateFactory stateFactory, Depositable depositable)
    {
        super(DepositState.Type.VALIDATING, stateFactory, depositable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void progress()
    {
        Level7Collection collection = getDepositable();
        
        logger.info("Validating " + collection);

        // 3. Validate all files
        boolean anyDepositableInvalid = false;
        boolean anyDepositableUnvalidated = false;

        for (DepositableArtefact depositableArtefact : getDepositable().getDepositableArtefacts())
        {
            if (depositableArtefact instanceof EncapsulationFile || depositableArtefact instanceof Thumbnail)
            {
                // We don't want to progress the encapsulations or thumbnails until we start depositing.
                continue;
            }
            if(depositableArtefact.isProcessing())
            {
                anyDepositableUnvalidated = true;
                // Ask the artefact to be validated
                depositableArtefact.progressDeposit();
            }
            else if (depositableArtefact.isFailedDeposit())
            {
                logger.warn(String.format("Artefact '%s' failed", depositableArtefact.getUniqueIdentifier()));
                anyDepositableInvalid = true;
            }
            else if (!depositableArtefact.isProcessed() && !depositableArtefact.isEncapsulating())
            {
                logger.warn(String.format("Found artefact '%s' in unexpected state '%s'",
                        depositableArtefact.getUniqueIdentifier(), depositableArtefact.getDepositState()));
                // In an unexpected state - wait for it to be corrected 
                anyDepositableUnvalidated = true;
            }
        }
        logger.info(String.format("After processing %d artefacts state is %s %s",
                getDepositable().getDepositableArtefacts().size(),
                anyDepositableUnvalidated ? "Unvalidated" : "Validated",
                anyDepositableInvalid ? "Invalid" : "Valid"));
        
        // Check validation outcomes
        if (!anyDepositableUnvalidated)
        {
            if (anyDepositableInvalid)
            {
                transitionTo(DepositState.Type.INVALID);
            }
            else
            {
                transitionTo(DepositState.Type.VALID);
            }
        }
        // else still running
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCheckpointState()
    {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Level7Collection getDepositable()
    {
        return (Level7Collection) super.getDepositable();
    }
}
