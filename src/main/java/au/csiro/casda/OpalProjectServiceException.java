package au.csiro.casda;

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
 * An exception for service requests which are valid but fail with an exception in the server. Not the client's fault -
 * for example the database might be down. You can throw this exception and the controller will return an HTTP 500
 * response code.
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
public class OpalProjectServiceException extends RuntimeException
{
    /** Serialisation id */
    private static final long serialVersionUID = -1l;

    /**
     * Constructs a OpalProjectServiceException with the given message.
     * 
     * @param message
     *            the message
     */
    public OpalProjectServiceException(String message)
    {
        super(message);
    }

    /**
     * Constructs a OpalProjectServiceException with the given cause.
     * 
     * @param t
     *            the cause
     */
    public OpalProjectServiceException(Throwable t)
    {
        super(t);
    }
}