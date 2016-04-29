package au.csiro.casda.deposit.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import au.csiro.casda.datadeposit.ChildDepositableArtefact;
import au.csiro.casda.datadeposit.DepositState.Type;
import au.csiro.casda.datadeposit.DepositStateChangeListener;
import au.csiro.casda.datadeposit.Depositable;
import au.csiro.casda.datadeposit.DepositableArtefact;
import au.csiro.casda.deposit.CasdaDepositManagerMessageBuilder;
import au.csiro.casda.deposit.DepositManagerEvents;
import au.csiro.casda.deposit.services.NgasService;
import au.csiro.casda.deposit.services.NgasService.ServiceCallException;
import au.csiro.casda.entity.observation.Level7Collection;
import au.csiro.casda.logging.DataLocation;

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
 * Data deposit implementation of the DepositStateChangeListener. The primary purpose of this class is to log specific
 * messages when observations and depositable artifacts transition between states.
 * <p>
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@Component(value = "CasdaLevel7DepositStateChangeListener")
public class CasdaLevel7DepositStateChangeListener implements DepositStateChangeListener
{
    private static final Logger logger = LoggerFactory.getLogger(CasdaLevel7DepositStateChangeListener.class);

    private NgasService ngasService;

    /**
     * Constructor.
     * 
     * @param ngasService
     *            the ngasService used to determine DepositableArtefact file sizes.
     */
    @Autowired
    public CasdaLevel7DepositStateChangeListener(NgasService ngasService)
    {
        this.ngasService = ngasService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stateChanged(Depositable depositable, Type fromStateType, Type toStateType)
    {
        if (!toStateType.equals(Type.FAILED))
        {
            if (depositable instanceof Level7Collection)
            {
                // Log each change in deposit status for the level 7 (Event E119)
                Level7Collection level7Collection = (Level7Collection) depositable;
                logger.info(DepositManagerEvents.E119.messageBuilder() //
                        .add(level7Collection.getDapCollectionId()) //
                        .add(toStateType.name()) //
                        .addFileId(level7Collection.getUniqueIdentifier().replace("/", "-")) //
                        .addCustomMessage("Project code: " + level7Collection.getProject().getOpalCode()) //
                        .toString());

                if (toStateType.equals(Type.DEPOSITED))
                {
                    // On success of level 7 collection deposit log an information event (Event E115)
                    long filesizeInBytes = 0;
                    for (DepositableArtefact depositableArtefact : level7Collection.getDepositableArtefacts())
                    {
                        long depositablArtefactFileSize = getDepositablArtefactFileSize(depositableArtefact);
                        if (depositablArtefactFileSize == -1)
                        {
                            filesizeInBytes = -1;
                            break;
                        }
                        else
                        {
                            filesizeInBytes += depositablArtefactFileSize;
                        }
                    }
                    logger.info(DepositManagerEvents.E115.messageBuilder()//
                            .add(level7Collection.getDapCollectionId()) //
                            .addCustomMessage("Project code: " + level7Collection.getProject().getOpalCode()) //
                            .addStartTime(level7Collection.getDepositStarted().toDate().toInstant()) //
                            .addEndTime(level7Collection.getDepositStateChanged().toDate().toInstant()) //
                            .addSource(DataLocation.RTC) //
                            .addDestination(DataLocation.ARCHIVE) //
                            .addVolumeBytes(filesizeInBytes) //
                            .addFileId(level7Collection.getUniqueIdentifier().replace("/", "-")) //
                            .toString());
                }
            }
            else
            {
                // Log each change in deposit status for each artefact (Event E120)
                ChildDepositableArtefact artefact = (ChildDepositableArtefact) depositable;
                Level7Collection level7Collection = (Level7Collection) artefact.getParent();
                CasdaDepositManagerMessageBuilder messageBuilder = DepositManagerEvents.E120.messageBuilder() //
                        .add(artefact.getFilename()) //
                        .add(level7Collection.getUniqueId()) //
                        .add(toStateType.name()) //
                        .addCustomMessage("Project code: " + level7Collection.getProject().getOpalCode()) //
                        .addFileId(artefact.getFileId());
                if (toStateType.equals(Type.DEPOSITED))
                {
                    // On success of artefefact deposit add additional fields for performance reporting
                    long depositablArtefactFileSize = getDepositablArtefactFileSize(artefact);
                    messageBuilder//
                            .addStartTime(level7Collection.getDepositStarted().toDate().toInstant()) //
                            .addEndTime(artefact.getDepositStateChanged().toDate().toInstant()) //
                            .addSource(DataLocation.RTC) //
                            .addDestination(DataLocation.ARCHIVE) //
                            .addVolumeBytes(depositablArtefactFileSize);

                }
                logger.info(messageBuilder.toString());
            }
        }
        else
        {
            // On controlled collection/artefact deposit step failure (job step catches error condition,
            // handles appropriately & returns cleanly), log failure:
            if (depositable instanceof Level7Collection)
            {
                Level7Collection level7Collection = (Level7Collection) depositable;
                logger.error(DepositManagerEvents.E114.messageBuilder() //
                        .add(level7Collection.getUniqueId())
                        .addFileId(level7Collection.getUniqueIdentifier().replace("/", "-")) //
                        .addCustomMessage("Project code: " + level7Collection.getProject().getOpalCode()) //
                        .toString());
            }
            else
            {
                ChildDepositableArtefact artefact = (ChildDepositableArtefact) depositable;
                Level7Collection level7Collection = (Level7Collection) artefact.getParent();
                logger.error(DepositManagerEvents.E121.messageBuilder() //
                        .add(artefact.getFilename()) //
                        .add(level7Collection.getUniqueId()) //
                        .add(fromStateType.name()) //
                        .addFileId(artefact.getFileId()) //
                        .addCustomMessage("Project code: " + level7Collection.getProject().getOpalCode()) //
                        .toString());
            }
        }
    }

    private long getDepositablArtefactFileSize(DepositableArtefact depositableArtefact)
    {
        // TODO: Add fileSize to DepositableArtefact and set it as part of the observation.xml parsing.
        try
        {
            return ngasService.getStatus(depositableArtefact.getFileId()).getUncompressedFileSizeBytes();
        }
        catch (ServiceCallException e)
        {
            return -1;
        }
    }
}
