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

import au.csiro.casda.entity.observation.Catalogue;
import au.csiro.casda.entity.observation.CatalogueType;
import au.csiro.casda.entity.observation.ImageCube;
import au.csiro.casda.entity.observation.MeasurementSet;

/**
 * Data product information for transfer to casda rules.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public class DataProductDTO implements Serializable
{
    private static final long serialVersionUID = 5414996942474392723L;

    /**
     * Available quality levels
     *
     */
    public enum QualityLevel
    {
        /** Not validated */
        NOT_VALIDATED,
        /** Good */
        GOOD,
        /** Uncertain */
        UNCERTAIN,
        /** Bad */
        BAD,
        /** Under review */
        UNDER_REVIEW,
        /** Not applicable */
        NOT_APPLICABLE
    }

    /**
     * Available data product types
     */
    public enum DataProductType
    {
        /** Catalogue */
        CATALOGUE,
        /** Measurement set (Visibility) */
        MEASUREMENT_SET,
        /** Image cube */
        IMAGE_CUBE

    }

    private long id;
    private DataProductType type;
    private CatalogueType catalogueType;
    private String identifier;
    private QualityLevel qualityLevel;

    /**
     * Empty constructor, for JSON deserialisation
     */
    public DataProductDTO()
    {
    }

    /**
     * Constructor for catalogue
     * 
     * @param catalogue
     *            the catalogue
     */
    public DataProductDTO(Catalogue catalogue)
    {
        this(catalogue.getId(), DataProductType.CATALOGUE, catalogue.getCatalogueType(), catalogue.getFileId(),
                catalogue.getQualityLevel() == null ? null : QualityLevel.valueOf(catalogue.getQualityLevel().name()));
    }

    /**
     * Constructor for measurement set
     * 
     * @param measurementSet
     *            the measurement set
     */
    public DataProductDTO(MeasurementSet measurementSet)
    {
        this(measurementSet.getId(), DataProductType.MEASUREMENT_SET, null, measurementSet.getFileId(), measurementSet
                .getQualityLevel() == null ? null : QualityLevel.valueOf(measurementSet.getQualityLevel().name()));
    }

    /**
     * Constructor for image cube
     * 
     * @param imageCube
     *            the image cube
     */
    public DataProductDTO(ImageCube imageCube)
    {
        this(imageCube.getId(), DataProductType.IMAGE_CUBE, null, imageCube.getFileId(),
                imageCube.getQualityLevel() == null ? null : QualityLevel.valueOf(imageCube.getQualityLevel().name()));
    }

    /**
     * Minimal constructor
     * 
     * @param id
     *            the data product id
     * @param type
     *            the data product type
     * @param catalogueType
     *            the data product subtype
     * @param identifier
     *            the data product file identifier
     * @param qualityLevel
     *            the quality level
     */
    public DataProductDTO(long id, DataProductType type, CatalogueType catalogueType, String identifier,
            QualityLevel qualityLevel)
    {
        this.id = id;
        this.type = type;
        this.catalogueType = catalogueType;
        this.identifier = identifier;
        this.qualityLevel = qualityLevel;
    }

    public long getId()
    {
        return id;
    }

    public DataProductType getType()
    {
        return type;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public QualityLevel getQualityLevel()
    {
        return qualityLevel;
    }

    public void setQualityLevel(QualityLevel qualityLevel)
    {
        this.qualityLevel = qualityLevel;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public void setType(DataProductType type)
    {
        this.type = type;
    }

    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public CatalogueType getCatalogueType()
    {
        return catalogueType;
    }

    public void setCatalogueType(CatalogueType catalogueType)
    {
        this.catalogueType = catalogueType;
    }
}
