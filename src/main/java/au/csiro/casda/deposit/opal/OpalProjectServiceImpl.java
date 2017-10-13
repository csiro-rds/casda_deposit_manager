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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import au.csiro.casda.BadRequestException;
import au.csiro.casda.OpalProjectServiceException;
import au.csiro.casda.ResourceNotFoundException;
import au.csiro.casda.deposit.jpa.ObservationRepository;
import au.csiro.casda.deposit.jpa.ProjectRepository;
import au.csiro.casda.entity.observation.Project;

/**
 * Service to get projects that have been encountered by data deposit and need to be sync'ed with OPAL and to allow a
 * client to update a project with data from OPAL (has been sync'ed).
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
@Service
public class OpalProjectServiceImpl implements OpalProjectService
{
    private static Logger logger = LoggerFactory.getLogger(OpalProjectService.class);

    @Autowired
    private ProjectRepository projectRepository;
    
    @Autowired
    private ObservationRepository observationRepository;

    /** Constructor. Sets the timeout */
    public OpalProjectServiceImpl()
    {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, Object>> getNewProjects() throws DataAccessException
    {
        logger.info("Retrieving project codes from the CASDA database");
        List<String> results = projectRepository.findNewProjectCodes();
        
        List<Map<String, Object>> projects = new ArrayList<>();
        if (results != null)
        {
            for (String projectCode : results)
            {
                Map<String, Object> projectDetails = new HashMap<>();
                projectDetails.put("opalCode", projectCode);
                DateTime earliestDate = observationRepository.findEarliestObservationStartDateForProject(projectCode);
                if (earliestDate == null)
                {
                    logger.debug("Ignoring project {} with no earliest observation start date.", projectCode);
                }
                else
                {
                    projectDetails.put("earliestObservationDate", earliestDate.getMillis());
                    // only add it to the list if there is an earliest observation date
                    projects.add(projectDetails);
                }
            }
        }
                
        logger.info("results found: {}", results);
        return projects;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Project updateProjectWithOpalData(String opalCode, String shortName, String principalFirstName,
            String principalLastName) throws ResourceNotFoundException, BadRequestException
    {
        if (StringUtils.isBlank(opalCode) || StringUtils.isBlank(shortName))
        {
            throw new BadRequestException(
                    "Bad parameters passed to service (expect opalCode and shortName query params)");
        }

        logger.info("Marking project {} as known", opalCode);

        Project project = projectRepository.findByOpalCode(opalCode);
        if (project == null)
        {
            throw new ResourceNotFoundException("Failed to update project details. "
                    + "Check that project with OPAL Code " + opalCode + " exists");
        }
        project.setKnownProject(Boolean.TRUE);
        project.setShortName(shortName);
        project.setPrincipalFirstName(principalFirstName);
        project.setPrincipalLastName(principalLastName);

        try
        {
            return projectRepository.save(project);
        }
        catch (Exception ex)
        {
            throw new OpalProjectServiceException(ex);
        }
    }
}
