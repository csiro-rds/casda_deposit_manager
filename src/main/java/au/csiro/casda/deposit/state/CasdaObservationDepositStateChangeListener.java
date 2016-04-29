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
import au.csiro.casda.entity.observation.Observation;
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
@Component(value = "CasdaObservationDepositStateChangeListener")
public class CasdaObservationDepositStateChangeListener implements DepositStateChangeListener
{
    private static final Logger logger = LoggerFactory.getLogger(CasdaObservationDepositStateChangeListener.class);

    private NgasService ngasService;

    /**
     * Constructor.
     * 
     * @param ngasService
     *            the ngasService used to determine DepositableArtefact file sizes.
     */
    @Autowired
    public CasdaObservationDepositStateChangeListener(NgasService ngasService)
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
            if (depositable instanceof Observation)
            {
                // Log each change in deposit status for the observation (Event E075)
                Observation observation = (Observation) depositable;
                logger.info(DepositManagerEvents.E075.messageBuilder() //
                        .add(observation.getSbid()) //
                        .add(toStateType.name()) //
                        .addFileId(observation.getUniqueIdentifier().replace("/", "-")).toString());

                if (toStateType.equals(Type.DEPOSITED))
                {
                    // On success of observation deposit log an information event (Event E077)
                    long filesizeInBytes = 0;
                    for (DepositableArtefact depositableArtefact : observation.getDepositableArtefacts())
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
                    logger.info(DepositManagerEvents.E077.messageBuilder() //
                            .add(observation.getSbid()) //
                            .addStartTime(observation.getDepositStarted().toDate().toInstant()) //
                            .addEndTime(observation.getDepositStateChanged().toDate().toInstant()) //
                            .addSource(DataLocation.RTC) //
                            .addDestination(DataLocation.ARCHIVE) //
                            .addVolumeBytes(filesizeInBytes) //
                            .addFileId(observation.getUniqueIdentifier().replace("/", "-")) //
                            .toString());
                }
            }
            else
            {
                // Log each change in deposit status for each artefact (Event E050)
                ChildDepositableArtefact artefact = (ChildDepositableArtefact) depositable;
                CasdaDepositManagerMessageBuilder messageBuilder = DepositManagerEvents.E050.messageBuilder() //
                        .add(artefact.getFilename()) //
                        .add(artefact.getParent().getUniqueId()) //
                        .add(toStateType.name()).addFileId(artefact.getFileId());
                if (toStateType.equals(Type.DEPOSITED))
                {
                    // On success of artefefact deposit add additional fields for performance reporting
                    Observation observation = (Observation) artefact.getParent();
                    long depositablArtefactFileSize = getDepositablArtefactFileSize(artefact);
                    messageBuilder//
                            .addStartTime(observation.getDepositStarted().toDate().toInstant()) //
                            .addEndTime(artefact.getDepositStateChanged().toDate().toInstant()) //
                            .addSource(DataLocation.RTC) //
                            .addDestination(DataLocation.ARCHIVE) //
                            .addVolumeBytes(depositablArtefactFileSize) //
                    ;
                }
                logger.info(messageBuilder.toString());

            }
        }
        else
        {
            // On controlled observation/ artefact deposit step failure (job step catches error condition,
            // handles appropriately & returns cleanly), log failure
            if (depositable instanceof Observation)
            {
                logger.error(DepositManagerEvents.E070.messageBuilder() //
                        .add(((Observation) depositable).getSbid()) //
                        .addFileId(((Observation) depositable).getUniqueIdentifier().replace("/", "-")) //
                        .toString());
            }
            else
            {
                ChildDepositableArtefact artefact = (ChildDepositableArtefact) depositable;
                logger.error(DepositManagerEvents.E052.messageBuilder() //
                        .add(artefact.getFilename()) //
                        .add(artefact.getParent().getUniqueId()) //
                        .add(fromStateType.name()) //
                        .addFileId(artefact.getFileId()) //
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
