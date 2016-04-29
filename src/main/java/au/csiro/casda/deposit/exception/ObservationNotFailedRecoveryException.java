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
 * Exception class used for when a recovery is requested for an observation that has not failed. 
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
public class ObservationNotFailedRecoveryException extends Exception
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a ObservationNotFailedRecoveryException for the given sbid
     * 
     * @param sbid
     *            the sbid of the Observation that is not FAILED
     */
    public ObservationNotFailedRecoveryException(Integer sbid)
    {
        super(String.format("Observation sbid: %d has not failed so it cannot be recovered.", sbid));
    }
}