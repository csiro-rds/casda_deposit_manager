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
 * ServerError to throw in REST controllers when some server error occurs.
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
public class ServerException extends RuntimeException
{
    private static final long serialVersionUID = -3036989267674020271L;

    /**
     * Constructs a ServerException.
     */
    public ServerException()
    {
        super();
    }

    /**
     * Constructs a ServerException with the given message.
     * 
     * @param message
     *            the message
     */
    public ServerException(String message)
    {
        super(message);
    }

    /**
     * Constructs a ServerException with the given cause.
     * 
     * @param cause
     *            the cause
     */
    public ServerException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Constructs a ServerException with the given message and cause.
     * 
     * @param message
     *            the message
     * @param cause
     *            the cause
     */
    public ServerException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
