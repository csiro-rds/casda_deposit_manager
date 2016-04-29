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
 * An exception for Deposit Processing errors.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public class DepositProcessingException extends Exception
{
    private static final long serialVersionUID = 717018527441528265L;

    /**
     * Constructs a DepositProcessingException with the given message.
     * 
     * @param message
     *            the message
     */
    public DepositProcessingException(String message)
    {
        super(message);
    }

    /**
     * Constructs a DepositProcessingException with the given cause.
     * 
     * @param t
     *            the cause
     */
    public DepositProcessingException(Throwable t)
    {
        super(t);
    }
}