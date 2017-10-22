package au.csiro.casda.datadeposit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.csiro.casda.entity.observation.ImageCube;
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
 * Abstract class for a DepositState.Type.DEPOSITING type DepositState for ParentDepositableArtefacts (eg Observation,
 * Level 7 Collection). The progress method for this state will attempt to progress the deposit of all the Parent's
 * constituent Depositables (eg: ImageCubes, Catalogues, etc).
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
public abstract class ParentDepositableArtefactDepositingDepositState extends DepositState
{

    private static final Logger logger = LoggerFactory.getLogger(ParentDepositableArtefactDepositingDepositState.class);

    private Type successTransitionState;
    
    private boolean largeFileOnly;

    /**
     * Constructor. (see {@link DepositState})
     * 
     * @param stateFactory
     *            (see {@link DepositState})
     * @param parentDepositableArtefact
     *            (see {@link DepositState})
     * @param currentState the current state of the observation 
     * @param successTransitionState
     *            the state to transition when successfully progressing the parentDepositableArtefact
     * @param largeFileOnly true if only large files (currently image cubes) should be progressed.
     * 
     */
    public ParentDepositableArtefactDepositingDepositState(DepositStateFactory stateFactory, 
    		ParentDepositableArtefact parentDepositableArtefact, Type currentState, 
    		Type successTransitionState, boolean largeFileOnly)
    {
        super(currentState, stateFactory, parentDepositableArtefact);
        this.successTransitionState = successTransitionState;
        this.largeFileOnly = largeFileOnly;
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
        	//processing will occur on all files if largeFileOnly is false OR if not, processing will occur only 
        	//on large files (atm defined as image cubes)
        	if(!largeFileOnly || depositableArtefact instanceof ImageCube)
        	{
                StringBuilder logMessage = new StringBuilder("\t");
                logMessage.append(depositableArtefact.getDepositState().getType());
                logMessage.append(" ");
                logMessage.append(depositableArtefact.getClass().getSimpleName());
                if (depositableArtefact instanceof DepositableArtefact)
                {
                    logMessage.append(" ");
                    logMessage.append(((DepositableArtefact) depositableArtefact).getFilename());
                }

                if (depositableArtefact.isFailedDeposit())
                {
                    anyDepositableFailed = true;
                }
                else if (depositableArtefact.isEncapsulating() || depositableArtefact.isEncapsulated())
                {
                    // Encapsulated objects get progressed by their parent encapsulation, so we leave them alone
                    continue;
                }
                // if only progressing large files (priority_depositing) checks that all files are at least staging,
                // if progressing all files (depositing) checks that all files are archiving or encapsulated
                else if ((!largeFileOnly && !depositableArtefact.isAtleastArchiving())
                        || (largeFileOnly && !depositableArtefact.isAtleastStaging()))
                {
                    allDepositablesFinished = false;
                    depositableArtefact.progressDeposit();

                    logMessage.append("\n\t\t -> " + depositableArtefact.getDepositState().getType());
                }
                else if (!depositableArtefact.isDeposited())
                {
                    // does not affect allDepositablesFinished as artefacts only have to reach the staging or archiving
                    // minimum.
                    depositableArtefact.progressDeposit();

                    logMessage.append("\n\t\t -> " + depositableArtefact.getDepositState().getType());
                }

                logger.debug("{}", logMessage);
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
