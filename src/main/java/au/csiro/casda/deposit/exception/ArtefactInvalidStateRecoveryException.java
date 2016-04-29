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
 * Exception class used for when a recovery is requested for an observation's artefact cannot be recovered.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public class ArtefactInvalidStateRecoveryException extends Exception
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a ArtefactNotFailedRecoveryException for the given sbid and file id
     * 
     * @param sbid
     *            the sbid of the Observation that owns the artefact
     * @param fileId
     *            the file id of the artefact
     * @param reason
     *            the reason the artefact can't be recovered
     */
    public ArtefactInvalidStateRecoveryException(Integer sbid, String fileId, String reason)
    {
        super(String.format("Artefact fileId: %s for observation sbid: %d has not cannot be recovered: %s", fileId,
                sbid, reason));
    }
}