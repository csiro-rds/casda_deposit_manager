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


import org.apache.commons.lang3.StringUtils;

/**
 * Exception class used to notify clients of any issues encountered with the data deposit import.
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
public class ImportException extends Exception
{
    private static final long serialVersionUID = 1L;

    private String sbid;

    /**
     * Constructs an ImportException for the given observation and message.
     * 
     * @param sbid
     *            the sbid of the Observation that could not be imported
     * @param message
     *            a String
     */
    public ImportException(String sbid, String message)
    {
        super(message);
        this.sbid = sbid;
    }

    /**
     * Constructs an ImportException for the given observation tool.
     * 
     * @param tool
     *            the name of the tool used to import the observation
     * @param sbid
     *            the sbid of the Observation that could not be imported
     * @param path
     *            the path to the tool
     * @param cause
     *            the cause of the failure
     */
    public ImportException(String tool, String sbid, String path, String cause)
    {
        this(sbid, String.format("Import failed - tool: %s, sbid: %s, path: %s, cause: %s", tool, sbid, path,
                StringUtils.isBlank(cause) ? "unknown" : cause));
    }

    /**
     * Constructs an ImportException for the given observation and cause.
     * 
     * @param sbid
     *            the sbid of the Observation that could not be imported
     * @param cause
     *            another Throwable
     */
    public ImportException(String sbid, Throwable cause)
    {
        super(cause);
        this.sbid = sbid;
    }

    public String getSbid()
    {
        return sbid;
    }

}
