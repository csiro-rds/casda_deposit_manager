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
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;

import au.csiro.casda.entity.ValidationNote;
import au.csiro.casda.entity.observation.Catalogue;
import au.csiro.casda.entity.observation.Cubelet;
import au.csiro.casda.entity.observation.ImageCube;
import au.csiro.casda.entity.observation.MeasurementSet;
import au.csiro.casda.entity.observation.MomentMap;
import au.csiro.casda.entity.observation.Observation;
import au.csiro.casda.entity.observation.Project;
import au.csiro.casda.entity.observation.Spectrum;

/**
 * Details about data products for an observation and project combination, for transfer to casda rules.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public class ObservationProjectDataProductsDTO implements Serializable
{
    private static final long serialVersionUID = -2354798068637054170L;

    private Integer sbid;
    private String opalCode;
    private Long obsStartDateMillis;
    private String principalFirstName;
    private String principalLastName;
    private List<String> qualityFlagCodes = new ArrayList<>();
    private List<DataProductDTO> dataProducts = new ArrayList<>();
    private List<ValidationNoteDTO> validationNotes = new ArrayList<>();

    /**
     * Empty constructor, for JSON deserialisation.
     */
    public ObservationProjectDataProductsDTO()
    {
    }

    /**
     * Minimal constructor
     * 
     * @param sbid
     *            observation scheduling block id
     * @param opalCode
     *            project code
     * @param obsStartDate
     *            observation start date
     * @param principalFirstName
     *            the principal investigator's first name
     * @param principalLastName
     *            the principal investigator's last name
     */
    public ObservationProjectDataProductsDTO(Integer sbid, String opalCode, DateTime obsStartDate,
            String principalFirstName, String principalLastName)
    {
        this.opalCode = opalCode;
        this.sbid = sbid;
        this.obsStartDateMillis = obsStartDate.getMillis();
        this.principalFirstName = principalFirstName;
        this.principalLastName = principalLastName;
    }

    /**
     * Constructor
     * 
     * @param opalCode
     *            the project code we are interested in
     * @param observation
     *            the observation to extract information from
     * @param qualityFlagCodes
     *            list of quality flag codes associated with this scheduling block (observation-project combination)
     * @param validationNotes
     *            the list of validation notes associated with this scheduling block (observation-project combination)
     */
    public ObservationProjectDataProductsDTO(String opalCode, Observation observation, List<String> qualityFlagCodes,
            List<ValidationNote> validationNotes)
    {
        this.opalCode = opalCode;
        this.sbid = observation.getSbid();
        this.obsStartDateMillis = observation.getObsStart().getMillis();

        Project project = null;

        for (Catalogue catalogue : observation.getCatalogues())
        {
            if (opalCode.equals(catalogue.getProject().getOpalCode()))
            {
                if (project == null)
                {
                    project = catalogue.getProject();
                }
                dataProducts.add(new DataProductDTO(catalogue));
            }
        }

        for (MeasurementSet measurementSet : observation.getMeasurementSets())
        {
            if (opalCode.equals(measurementSet.getProject().getOpalCode()))
            {
                if (project == null)
                {
                    project = measurementSet.getProject();
                }
                dataProducts.add(new DataProductDTO(measurementSet));
            }
        }

        for (ImageCube imageCube : observation.getImageCubes())
        {
            if (opalCode.equals(imageCube.getProject().getOpalCode()))
            {
                if (project == null)
                {
                    project = imageCube.getProject();
                }
                dataProducts.add(new DataProductDTO(imageCube));
            }
        }

        for (Spectrum spectrum : observation.getSpectra())
        {
            if (opalCode.equals(spectrum.getProject().getOpalCode()))
            {
                if (project == null)
                {
                    project = spectrum.getProject();
                }
                dataProducts.add(new DataProductDTO(spectrum));
            }
        }

        for (MomentMap momentMap : observation.getMomentMaps())
        {
            if (opalCode.equals(momentMap.getProject().getOpalCode()))
            {
                if (project == null)
                {
                    project = momentMap.getProject();
                }
                dataProducts.add(new DataProductDTO(momentMap));
            }
        }
        
        for (Cubelet cubelet : observation.getCubelets())
        {
            if (opalCode.equals(cubelet.getProject().getOpalCode()))
            {
                if (project == null)
                {
                    project = cubelet.getProject();
                }
                dataProducts.add(new DataProductDTO(cubelet));
            }
        }

        if (project != null)
        {
            this.principalFirstName = project.getPrincipalFirstName();
            this.principalLastName = project.getPrincipalLastName();
        }

        if (CollectionUtils.isNotEmpty(qualityFlagCodes))
        {
            this.qualityFlagCodes.addAll(qualityFlagCodes);
        }

        if (CollectionUtils.isNotEmpty(validationNotes))
        {
            for (ValidationNote note : validationNotes)
            {
                this.validationNotes.add(new ValidationNoteDTO(note));
            }
        }
    }

    public String getOpalCode()
    {
        return opalCode;
    }

    public Integer getSbid()
    {
        return sbid;
    }

    public Long getObsStartDateMillis()
    {
        return obsStartDateMillis;
    }

    public String getPrincipalFirstName()
    {
        return principalFirstName;
    }

    public String getPrincipalLastName()
    {
        return principalLastName;
    }

    public List<DataProductDTO> getDataProducts()
    {
        return dataProducts;
    }

    public void setSbid(Integer sbid)
    {
        this.sbid = sbid;
    }

    public void setOpalCode(String opalCode)
    {
        this.opalCode = opalCode;
    }

    public void setObsStartDateMillis(Long obsStartDateMillis)
    {
        this.obsStartDateMillis = obsStartDateMillis;
    }

    public void setPrincipalFirstName(String principalFirstName)
    {
        this.principalFirstName = principalFirstName;
    }

    public void setPrincipalLastName(String principalLastName)
    {
        this.principalLastName = principalLastName;
    }

    /**
     * Adds data product to the list of data products
     * 
     * @param dataProduct
     *            data product to add
     */
    public void addDataProduct(DataProductDTO dataProduct)
    {
        this.dataProducts.add(dataProduct);
    }

    public List<String> getQualityFlagCodes()
    {
        return qualityFlagCodes;
    }

    public List<ValidationNoteDTO> getValidationNotes()
    {
        return validationNotes;
    }

    /**
     * Adds quality flag code to the list of codes
     * 
     * @param qualityFlagCode
     *            code to add
     */
    public void addQualityFlagCode(String qualityFlagCode)
    {
        this.qualityFlagCodes.add(qualityFlagCode);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((dataProducts == null) ? 0 : dataProducts.hashCode());
        result = prime * result + ((obsStartDateMillis == null) ? 0 : obsStartDateMillis.hashCode());
        result = prime * result + ((opalCode == null) ? 0 : opalCode.hashCode());
        result = prime * result + ((principalFirstName == null) ? 0 : principalFirstName.hashCode());
        result = prime * result + ((principalLastName == null) ? 0 : principalLastName.hashCode());
        result = prime * result + ((qualityFlagCodes == null) ? 0 : qualityFlagCodes.hashCode());
        result = prime * result + ((sbid == null) ? 0 : sbid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        ObservationProjectDataProductsDTO other = (ObservationProjectDataProductsDTO) obj;
        if (dataProducts == null)
        {
            if (other.dataProducts != null)
            {
                return false;
            }
        }
        else if (!dataProducts.equals(other.dataProducts))
        {
            return false;
        }
        if (obsStartDateMillis == null)
        {
            if (other.obsStartDateMillis != null)
            {
                return false;
            }
        }
        else if (!obsStartDateMillis.equals(other.obsStartDateMillis))
        {
            return false;
        }
        if (opalCode == null)
        {
            if (other.opalCode != null)
            {
                return false;
            }
        }
        else if (!opalCode.equals(other.opalCode))
        {
            return false;
        }
        if (principalFirstName == null)
        {
            if (other.principalFirstName != null)
            {
                return false;
            }
        }
        else if (!principalFirstName.equals(other.principalFirstName))
        {
            return false;
        }
        if (principalLastName == null)
        {
            if (other.principalLastName != null)
            {
                return false;
            }
        }
        else if (!principalLastName.equals(other.principalLastName))
        {
            return false;
        }
        if (qualityFlagCodes == null)
        {
            if (other.qualityFlagCodes != null)
            {
                return false;
            }
        }
        else if (!qualityFlagCodes.equals(other.qualityFlagCodes))
        {
            return false;
        }
        if (sbid == null)
        {
            if (other.sbid != null)
            {
                return false;
            }
        }
        else if (!sbid.equals(other.sbid))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "ObservationProjectDataProductsDTO [sbid=" + sbid + ", opalCode=" + opalCode + ", obsStartDateMillis="
                + obsStartDateMillis + ", principalFirstName=" + principalFirstName + ", principalLastName="
                + principalLastName + ", qualityFlagCodes=" + qualityFlagCodes + ", dataProducts=" + dataProducts + "]";
    }

}
