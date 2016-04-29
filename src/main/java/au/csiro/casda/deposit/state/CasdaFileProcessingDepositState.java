package au.csiro.casda.deposit.state;

import java.io.File;

import org.apache.commons.io.FileUtils;

import au.csiro.casda.datadeposit.ChildDepositableArtefact;
import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.datadeposit.Depositable;
import au.csiro.casda.datadeposit.ProcessingDepositState;

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
 * State which performs basic processing for all data product types that are not handled elsewhere. Currently this just
 * populates the file size.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
public class CasdaFileProcessingDepositState extends ProcessingDepositState
{

    private String parentDirectory;

    /**
     * Constructor
     * 
     * @param stateFactory
     *            see {@link DepositState}
     * @param depositable
     *            the data product that the state pertains to
     * @param parentDirectory
     *            the parent directory of the deposit observation or level 7 collection folders
     */
    public CasdaFileProcessingDepositState(DepositStateFactory stateFactory, Depositable depositable,
            String parentDirectory)
    {
        super(stateFactory, depositable);
        this.parentDirectory = parentDirectory;
    }

    /*
     * (non-Javadoc)
     * 
     * @see au.csiro.casda.datadeposit.ProcessingDepositState#progress()
     */
    @Override
    public void progress()
    {
        ChildDepositableArtefact artefact = (ChildDepositableArtefact) getDepositable();
        String filename = artefact.getFilename();

        File depositableFolder = new File(parentDirectory, artefact.getParent().getUniqueId());
        File artefactFile = new File(depositableFolder, filename);

        artefact.setFilesize((long) Math.ceil((double) artefactFile.length() / FileUtils.ONE_KB));
        super.progress();
    }

}
