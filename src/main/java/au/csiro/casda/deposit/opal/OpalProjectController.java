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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;

import au.csiro.casda.BadRequestException;
import au.csiro.casda.ResourceNotFoundException;
import au.csiro.casda.ServerException;
import au.csiro.casda.entity.observation.Project;
import au.csiro.casda.services.dto.Message.MessageCode;
import au.csiro.casda.services.dto.MessageDTO;

/**
 * RESTful web service controller for getting new (unknown to DAP) projects and updating a project with data from OPAL.
 * 
 * usage:
 * <ul>
 * <li>to get a list of unknown projects: /casda_vo_tools/opalProjectSync/getNewProjects</li>
 * <li>to set a project as known: /casda_vo_tools/opalProjectSync/flagProjectAsKnown ?opalCode=A008&shortName=EMU</li>
 * </ul>
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
@Api(value = "Projects", description = "Project query and maintenance service")
@RestController
public class OpalProjectController
{

    private OpalProjectService opalProjectService;

    private static Logger logger = LoggerFactory.getLogger(OpalProjectController.class);

    /**
     * Constructs an OpalProjectController with the given OpalProjectService
     * 
     * @param opalProjectService
     *            and OpalProjectService
     */
    @Autowired
    public OpalProjectController(OpalProjectService opalProjectService)
    {
        this.opalProjectService = opalProjectService;
    }

    /**
     * Retrieves a list of project details (including OPAL codes and the earliest observation start date associated with
     * a project) which DAP does not know about yet.
     * 
     * @param state
     *            The state of the projects to be retrieved. Only 'new' is supported.
     * @return a list of Maps of project details, with keys opalCode and startDate, to be added to the response body
     * @throws BadRequestException
     *             if the state value is not 'new'
     */
    @RequestMapping(method = RequestMethod.GET, value = "/projects")
    public List<Map<String, Object>> getNewProjects(@RequestParam() String state) throws BadRequestException
    {
        if (!"new".equals(state))
        {
            throw new BadRequestException("Invalid state value. Supported values are 'new'");
        }
        try
        {
            List<Map<String, Object>> results = opalProjectService.getNewProjects();
            return results;
        }
        catch (DataAccessException dataAccessException)
        {
            logger.error("error trying to retrieve the message", dataAccessException);
            throw new ServerException("error trying to retrieve the message", dataAccessException);
        }
    }

    /**
     * Handles a request to set the known flag on a specific project, and update project data from OPAL.
     * 
     * @param opalCode
     *            the opal id of a project
     * @param shortName
     *            the human-friendly name of the project, such as EMU or POSSUM
     * @param principalFirstName
     *            the first name of the principal investigator for the project
     * @param principalLastName
     *            the last name of the principal investigator for the project
     * @return a String in the response body: "true" if the operation succeeded, "false" otherwise
     * @throws ResourceNotFoundException
     *             if the Project matching the opalCode could not be found
     * @throws BadRequestException
     *             if the opalCode or shortName query parameters are blank
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/projects/{opalCode}")
    public @ResponseBody MessageDTO flagProjectAsKnown(@PathVariable(value = "opalCode") String opalCode,
            @RequestParam(value = "shortName") String shortName,
            @RequestParam(value = "principalFirstName") String principalFirstName, @RequestParam(
                    value = "principalLastName") String principalLastName) throws ResourceNotFoundException,
            BadRequestException
    {
        // perform some validation on the request parameters
        if (StringUtils.isBlank(opalCode) || StringUtils.isBlank(shortName))
        {
            throw new BadRequestException(
                    "Bad parameters passed to service (expect opalCode and shortName query params)");
        }

        // invoke the service to do the work. Any exception should be thrown
        Project updatedProject =
                opalProjectService
                        .updateProjectWithOpalData(opalCode, shortName, principalFirstName, principalLastName);

        // log a success event
        String successMessage = "Successfully updated project " + updatedProject.getOpalCode();

        logger.info(successMessage);

        return new MessageDTO(MessageCode.SUCCESS, successMessage);
    }
}