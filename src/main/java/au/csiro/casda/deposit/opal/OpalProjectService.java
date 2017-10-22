package au.csiro.casda.deposit.opal;

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


import java.util.List;
import java.util.Map;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import au.csiro.casda.BadRequestException;
import au.csiro.casda.ResourceNotFoundException;
import au.csiro.casda.entity.observation.Project;

/**
 * Service to create a project data sync request and enqueue it so that subscribers (such as DAP) can process the
 * requests when they (the subscribers) are available.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
@Service
public interface OpalProjectService
{
    /**
     * Gets project details for projects which have not yet been sync'ed by DAP, and have at least one fully deposited
     * observation.
     * 
     * @return a List of Maps. Each map contains 2 keys, opalCode for the project's code and earliestObservationDate for
     *         the earliest start date (as millis) for an observation associated with the project
     * @throws DataAccessException
     *             if something went wrong with the database call
     */
    public List<Map<String, Object>> getNewProjects() throws DataAccessException;

    /**
     * Sets the known_project flag to true, and updates project with data from OPAL.
     * 
     * @param opalCode
     *            the OPAL identifier of the project
     * @param shortName
     *            the human-readable name of the project (like EMU)
     * @param principalFirstName
     *            the first name of the principal investigator for the project
     * @param principalLastName
     *            the last name of the principal investigator for the project
     * @return Project the updated project
     * @throws ResourceNotFoundException
     *             if the Project matching the opalCode could not be found
     * @throws BadRequestException 
     * 				for a bad service request
     */
    public Project updateProjectWithOpalData(String opalCode, String shortName, String principalFirstName,
            String principalLastName) throws ResourceNotFoundException, BadRequestException;
}
