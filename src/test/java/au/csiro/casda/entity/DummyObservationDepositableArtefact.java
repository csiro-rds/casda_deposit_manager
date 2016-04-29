package au.csiro.casda.entity;

import au.csiro.casda.entity.observation.Observation;
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
 * Dummy implementation for test cases.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public class DummyObservationDepositableArtefact extends CasdaDepositableArtefactEntity
{

    private ParentDepositableArtefact parent;

    /**
     * Creates a DummyObservationDepositableArtefact with the given Observation and filename.
     * 
     * @param parent
     *            an Observation
     * @param filename
     *            the filename
     */
    public DummyObservationDepositableArtefact(ParentDepositableArtefact parent, String filename)
    {
        super(filename);
        this.parent = parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setParent(ParentDepositableArtefact parent)
    {
        this.parent = (Observation) parent;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public ParentDepositableArtefact getParent()
    {
        return this.parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCollectionName()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDepositableArtefactTypeDescription()
    {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDepositableArtefactTypeName()
    {
        return null;
    }

}
