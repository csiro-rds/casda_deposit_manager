package au.csiro.casda.dto;

import java.io.Serializable;

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
 * Data Transfer Object encapsulating information about a DepositableArtefact in CASDA.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public class DepositableArtefactDTO extends DepositableDTO implements Serializable
{
    private static final long serialVersionUID = -1272493692217486834L;

    private String filename;

    private Long filesizeInBytes;
    
    private String checksum;
    
    /**
     * Empty constructor, for JSON deserialisation
     */
    public DepositableArtefactDTO()
    {
    }

    public String getFilename()
    {
        return filename;
    }
    
    public void setFilename(String filename)
    {
        this.filename = filename;
    }

    public Long getFilesizeInBytes()
    {
        return filesizeInBytes;
    }
    
    public void setFilesizeInBytes(Long filesizeInBytes)
    {
        this.filesizeInBytes = filesizeInBytes;
    }
    
    public String getChecksum()
    {
        return checksum;
    }
    
    public void setChecksum(String checksum)
    {
        this.checksum = checksum;
    }
}
