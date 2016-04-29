package au.csiro.casda.dto;

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


import java.io.Serializable;

import au.csiro.casda.entity.QualityFlag;

/**
 * Data transfer object for quality flag.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public class QualityFlagDTO implements Serializable
{
    private static final long serialVersionUID = 1642144866760241120L;

    private long id;
    private String code;
    private String label;
    private int displayOrder;

    /**
     * Empty constructor for JSON serialisation.
     */
    public QualityFlagDTO()
    {
    }

    /**
     * Constructor from quality flag
     * 
     * @param qualityFlag
     *            the quality flag
     */
    public QualityFlagDTO(QualityFlag qualityFlag)
    {
        this.id = qualityFlag.getId();
        this.code = qualityFlag.getCode();
        this.label = qualityFlag.getLabel();
        this.displayOrder = qualityFlag.getDisplayOrder();
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public int getDisplayOrder()
    {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder)
    {
        this.displayOrder = displayOrder;
    }

}
