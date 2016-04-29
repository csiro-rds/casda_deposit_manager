package au.csiro.casda.deposit.state;

import java.util.Map;

import au.csiro.casda.datadeposit.ChildDepositableArtefact;
import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.deposit.DepositManagerEvents;
import au.csiro.casda.deposit.SingleJobMonitorFactory;
import au.csiro.casda.deposit.services.NgasService;
import au.csiro.casda.entity.observation.Level7Collection;
import au.csiro.casda.jobmanager.ProcessJobBuilder;

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
 * Extension of ArchivingDepositState that ensures that a DepositableArtefact's backing file has been archived and that
 * dual copies exist
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
public class Level7ArchivingDepositState extends CasdaArchivingDepositState
{
    /**
     * Constructor
     * 
     * @param stateFactory
     *            see {@link DepositState}
     * @param depositableArtefact
     *            the DepositableArtefact that the state pertains to
     * @param ngasArtefactVolumeMap
     *            a map from DepositableArtefact 'types' to NGAS volume names (used to control which volume an artefact
     *            is staged to).
     * @param ngasService
     *            a service to access ngas file manager.
     * @param archiveStatusBuilder
     *            a ProcessJobBuilder that can be used to check the archive status for an artefact.
     * @param archivePutBuilder
     *            a ProcessJobBuilder that can be used to request that an artefact be dual stated.
     * @param singleJobMonitorFactory
     *            the SingleJobMonitor factory
     */
    protected Level7ArchivingDepositState(DepositStateFactory stateFactory,
            ChildDepositableArtefact depositableArtefact, Map<String, String> ngasArtefactVolumeMap,
            NgasService ngasService, ProcessJobBuilder archiveStatusBuilder, ProcessJobBuilder archivePutBuilder,
            SingleJobMonitorFactory singleJobMonitorFactory)
    {
        super(stateFactory, depositableArtefact, ngasArtefactVolumeMap, ngasService, archiveStatusBuilder,
                archivePutBuilder, singleJobMonitorFactory);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getLogMessageForArchivingProgressWarning()
    {
        Level7Collection level7Collection = (Level7Collection) this.getDepositable().getParent();
        return DepositManagerEvents.E122.messageBuilder().add(this.getDepositable().getFilename())
                .add(level7Collection.getUniqueId()).add(this.getDepositable().getDepositState().getType().name())
                .addCustomMessage("Project code: " + level7Collection.getProject().getOpalCode()).toString();
    }

}
