package au.csiro.casda.deposit;

import javax.persistence.Transient;

/*
 * #%L
 * CSIRO ASKAP Science Data Archive
 * %%
 * Copyright (C) 2016 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

import org.joda.time.DateTime;

import au.csiro.casda.entity.observation.Project;

/**
 * Container object for reporting a summary of a group of depositable artefacts. The artefacts must all share a type,
 * status and project. They are then reported on a single summary line in the CASDA Observation Deposit Status page.
 * <p>
 * Copyright 2016, CSIRO Australia. All rights reserved.
 */
public class DepositableStatusSummary
{
    private String description;
    private String depositStateDescription;
    private int numArtefacts = 0;
    private DateTime depositStateChanged;
    private String checkpointStateType;
    private String projectCode;
    private Project project;
    private DateTime releasedDate;
    private boolean hasReleaseDate;

    /**
     * Create a new DepositableStatusSummary instance.
     *  
     * @param description The human readable description of the type of artefact type.
     * @param depositStateDescription The human readable description of the deposit state type.
     * @param project The project the artefact belongs to.
     * @param checkpointStateType The state the artefact will roll back to if it fails its current state.
     * @param hasReleaseDate Does this object type have a released date
     */
    public DepositableStatusSummary(String description, String depositStateDescription, Project project,
            String checkpointStateType, boolean hasReleaseDate)
    {
        this.description = description;
        this.depositStateDescription = depositStateDescription;
        this.project = project;
        this.checkpointStateType = checkpointStateType;
        this.hasReleaseDate = hasReleaseDate;
        projectCode = project == null ? "N/A" : project.getOpalCode();
    }

    /**
     * Generate a map key from the unique details for a summary.
     * @param description The human readable description of the type of artefact type.
     * @param depositStateDescription The human readable description of the deposit state type.
     * @param projectCode The OPAL code of the project the artefact belongs to.
     * @return The key
     */
    public static String getSummaryKey(String description, String depositStateDescription, String projectCode)
    {
        return description + "|" + depositStateDescription + "|" + projectCode;
    }

    /**
     * @return The map key for the summary.
     */
    public String getKey()
    {
        return DepositableStatusSummary.getSummaryKey(description, depositStateDescription, projectCode);
    }

    public String getDescription()
    {
        return description;
    }

    public String getDepositStateDescription()
    {
        return depositStateDescription;
    }

    public int getNumArtefacts()
    {
        return numArtefacts;
    }

    public void setNumArtefacts(int num)
    {
        numArtefacts = num;
    }

    public DateTime getDepositStateChanged()
    {
        return depositStateChanged;
    }

    public void setDepositStateChanged(DateTime depositStateChanged)
    {
        this.depositStateChanged = depositStateChanged;
    }

    public String getCheckpointStateType()
    {
        return checkpointStateType;
    }

    public String getProjectCode()
    {
        return projectCode;
    }

    public Project getProject()
    {
        return project;
    }

    public DateTime getReleasedDate()
    {
        return releasedDate;
    }

    public void setReleasedDate(DateTime releasedDate)
    {
        this.releasedDate = releasedDate;
    }
    
    /**
     * @return A string representation of the release date, showing N/A only if the data product does not have release
     *         dates.
     */
    @Transient
    public String getReleasedDateString()
    {
        if (!hasReleaseDate)
        {
            return "N/A";
        }
        if (releasedDate != null)
        {
            return String.valueOf(releasedDate);
        }
        return "";
    }
}