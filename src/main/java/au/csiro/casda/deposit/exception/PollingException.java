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
 * Exception class used to notify clients of any issues encountered with the data deposit polling.
 * <p>
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
public class PollingException extends Exception
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a PollingException.
     */
    public PollingException()
    {
    }

    /**
     * Constructs a PollingException with the given message.
     * 
     * @param message
     *            a String
     */
    public PollingException(String message)
    {
        super(message);
    }

    /**
     * Constructs a PollingException with the given cause.
     * 
     * @param cause
     *            another Throwable
     */
    public PollingException(Throwable cause)
    {
        super(cause);
    }

}