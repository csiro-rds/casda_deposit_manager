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
 * An exception for bad service requests (which nevertheless map to a controller method). Instead of returning a success
 * response (2xx code) and forwarding to an error page you can throw this exception and return a 400 response code.
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
public class BadRequestException extends Exception
{
    /** Serialisation id */
    private static final long serialVersionUID = -1l;

    /**
     * Constructs a BadRequestException with the given message.
     * 
     * @param message
     *            the message
     */
    public BadRequestException(String message)
    {
        super(message);
    }

    /**
     * Constructs a BadRequestException with the given cause.
     * 
     * @param t
     *            the cause
     */
    public BadRequestException(Throwable t)
    {
        super(t);
    }
}