package au.csiro.casda.deposit.exception;

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
 * Exception class used for when a recovery is requested for an artefact that does not exist.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public class ArtefactNotFoundException extends Exception
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructs an ArtefactNotFoundException for the given sbid
     * 
     * @param sbid
     *            an Integer
     * @param fileId
     *            the file id
     */
    public ArtefactNotFoundException(Integer sbid, String fileId)
    {
        super(String.format("No artefact with fileId: %s could be found for observation: %d to be recovered.", fileId,
                sbid));
    }
}