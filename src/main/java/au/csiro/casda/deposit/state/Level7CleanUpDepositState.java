package au.csiro.casda.deposit.state;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.deposit.services.VoToolsService;
import au.csiro.casda.entity.observation.Level7Collection;
import au.csiro.casda.logging.CasdaLogMessageBuilderFactory;
import au.csiro.casda.logging.LogEvent;

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
 * Implementation of a Level 7-specific DepositState for the DepositState.Type.CLEANUP state that deletes the level 7
 * collection folder and then transitions to the DEPOSITED state.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public class Level7CleanUpDepositState extends DepositState
{
    private static final Logger logger = LoggerFactory.getLogger(Level7CleanUpDepositState.class);

    private String level7CollectionParentDirectory;

    private VoToolsService voToolsService;

    /**
     * Constructor. (see {@link DepositState})
     * 
     * @param stateFactory
     *            (see {@link DepositState})
     * @param level7Collection
     *            (see {@link DepositState})
     * @param level7CollectionParentDirectory
     *            the parent directory for all level 7 collection directories, which store the catalogue files for
     *            processing and need to be removed when progressing through this clean up state.
     * @param voToolsService
     *            the VO Tools service
     */
    public Level7CleanUpDepositState(DepositStateFactory stateFactory, Level7Collection level7Collection,
            String level7CollectionParentDirectory, VoToolsService voToolsService)
    {
        super(DepositState.Type.CLEANUP, stateFactory, level7Collection);
        this.level7CollectionParentDirectory = level7CollectionParentDirectory;
        this.voToolsService = voToolsService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void progress()
    {
        try
        {
            voToolsService.resetVoTapMetadata();
            voToolsService.resetVoScsMetadata();
        }
        catch (RuntimeException e)
        {
            logger.error(
                    CasdaLogMessageBuilderFactory.getCasdaMessageBuilder(LogEvent.UNKNOWN_EVENT)
                            .addCustomMessage("Couldn't reset VO Metadata").toString(), e);
            transitionTo(DepositState.Type.FAILED);
            return;
        }

        File collectionFolder =
                new File(level7CollectionParentDirectory, String.valueOf(getDepositable().getDapCollectionId()));

        try
        {
            FileUtils.deleteDirectory(collectionFolder);
            transitionTo(DepositState.Type.DEPOSITED);
        }
        catch (IOException e)
        {
            logger.error(
                    CasdaLogMessageBuilderFactory
                            .getCasdaMessageBuilder(LogEvent.UNKNOWN_EVENT)
                            .addCustomMessage(
                                    "Problem deleting directory for collection " + collectionFolder.getAbsolutePath())
                            .toString(), e);
            transitionTo(DepositState.Type.FAILED);
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

    @Override
    public Level7Collection getDepositable()
    {
        return (Level7Collection) super.getDepositable();
    }
}
