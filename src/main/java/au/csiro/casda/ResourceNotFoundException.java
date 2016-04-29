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
 * An exception for when a resource is not found.
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
public class ResourceNotFoundException extends Exception
{
    private static final long serialVersionUID = -9151423532958177468L;

    /**
     * Constructs a ResourceNotFoundException with the given message.
     * 
     * @param message
     *            the message
     */
    public ResourceNotFoundException(String message)
    {
        super(message);
    }

    /**
     * Constructs a ResourceNotFoundException with the given cause.
     * 
     * @param t
     *            the cause
     */
    public ResourceNotFoundException(Throwable t)
    {
        super(t);
    }
}