package au.csiro.casda.deposit;

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
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import au.csiro.casda.BadRequestException;
import au.csiro.casda.ResourceNotFoundException;
import au.csiro.casda.ServerException;
import au.csiro.casda.deposit.services.ObservationService;
import au.csiro.casda.dto.ObservationProjectDataProductsDTO;
import au.csiro.casda.dto.QualityFlagDTO;
import au.csiro.casda.dto.ValidationNoteDTO;
import au.csiro.casda.services.dto.MessageDTO;

import com.wordnik.swagger.annotations.Api;

/**
 * RESTful web service controller. Endpoint retrieves and updates observation data.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 *
 */
@Api(basePath = "/observations", value = "Observations", description = "ASKAP Observations (aka scheduling blocks)")
@RestController
public class ObservationController
{

    private static final Logger logger = LoggerFactory.getLogger(ObservationController.class);

    private ObservationService observationService;

    /**
     * Constructor
     * 
     * @param observationService
     *            the observation service
     */
    @Autowired
    public ObservationController(ObservationService observationService)
    {
        this.observationService = observationService;
    }

    /**
     * Retrieve a list of project blocks for a given project code that have released data products or that are fully
     * deposited but not yet released.
     * 
     * @param state
     *            The state key, must be either released or unreleased
     * @param projectCode
     *            (only required for released state) the project code related to the observations
     * @param date
     *            (optional) if this is absent, returns all observations for a project; if this is present, returns all
     *            observations released after this date
     * @return A set of project blocks
     * @throws BadRequestException
     *             if the state is not 'released' or 'unreleased', or if the sate is 'released' and the projectCode is
     *             blank
     */
    @RequestMapping(method = RequestMethod.GET, value = "/observations/all/projects", produces = "application/json")
    public @ResponseBody Set<ObservationProjectDataProductsDTO> getProjectBlocks(@RequestParam() String state,
            @RequestParam(required = false) String projectCode, @RequestParam(required = false) Long date)
            throws BadRequestException
    {
        logger.info("Hit the controller for the '/observations/all/projects?state={}&projectCode={}&date={}'", state,
                projectCode, date);

        /*
         * This method returns a simplified DTO because a Project object a) is too noisy, and b) JSONification would
         * involve additional work in casda-commons (such as breaking a cyclic dependency, deciding on what fields to
         * expose, etc.) that at this stage does not seem warranted.
         */

        if ("unreleased".equals(state))
        {
            try
            {
                return observationService.getUnreleasedProjectBlocks();
            }
            catch (DataAccessException dataAccessException)
            {
                logger.error("error trying to retrieve the unreleased observations", dataAccessException);
                throw new ServerException("error trying to retrieve the unreleased observations", dataAccessException);
            }
        }
        else if ("released".equals(state))
        {
            if (StringUtils.isBlank(projectCode))
            {
                throw new BadRequestException("Missing projectCode param.");
            }
            try
            {
                return observationService.getReleasedProjectBlocks(projectCode, date);
            }
            catch (DataAccessException dataAccessException)
            {
                logger.error("error trying to retrieve the released observations", dataAccessException);
                throw new ServerException("error trying to retrieve the released observations", dataAccessException);
            }
        }
        throw new BadRequestException("Invalid state. State must be 'released' or 'unreleased'.");
    }

    /**
     * Release the project block (observation project combination) - ie set the related data products' release date to
     * now.
     * 
     * @param opalCode
     *            the project code
     * @param sbid
     *            observation id
     * @param date
     *            the release date
     * 
     * @return MessageDTO on success
     * @throws ResourceNotFoundException
     *             if an Project Block with the specified scheduling block ID and opal code could not be found
     * @throws BadRequestException
     *             if the date is not valid
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/observations/{sbid}/projects/{opalCode}/release",
            produces = "application/json")
    public @ResponseBody MessageDTO releaseProjectBlock(@PathVariable() Integer sbid, @PathVariable() String opalCode,
            @RequestParam() Long date) throws ResourceNotFoundException, BadRequestException
    {
        logger.info("Hit the controller for the '/observations/{}/projects/{}/release' url mapping", opalCode, sbid);

        if (date == null || date < 0)
        {
            throw new BadRequestException("Invalid date parameter. Date must be greater or equal to zero");
        }

        DateTime releaseDate = new DateTime(date, DateTimeZone.UTC);

        return observationService.releaseProjectBlock(opalCode, sbid, releaseDate);
    }

    /**
     * Returns a list of quality flags used to indicate conditions present that can contribute to data quality issues
     * 
     * @return the list of quality flags
     */
    @RequestMapping(method = RequestMethod.GET, value = "/observations/quality_flags", produces = "application/json")
    public @ResponseBody List<QualityFlagDTO> getQualityFlags()
    {
        logger.info("Hit the controller for /observation/quality_flags");
        return observationService.getActiveQualityFlags();
    }

    /**
     * Returns the observation, project and data product details for a given observation and project.
     *
     * @param sbid
     *            the scheduling block id for the observation
     * @param opalCode
     *            the project code
     * @return the observation, project and data product details
     * @throws ResourceNotFoundException
     *             if an observation with the given sbid could not be found
     */
    @RequestMapping(method = RequestMethod.GET, value = "/observations/{sbid}/projects/{opalCode}/dataproducts",
            produces = "application/json")
    public @ResponseBody ObservationProjectDataProductsDTO getDataProductDetails(@PathVariable() Integer sbid,
            @PathVariable() String opalCode) throws ResourceNotFoundException
    {
        logger.info("Hit the controller for '/observations/{}/projects/{}/dataproducts'", sbid, opalCode);
        return observationService.getObservationProjectDataProducts(sbid, opalCode);
    }

    /**
     * Updates the quality information for a given scheduling block (observation-project combination) and its data
     * product details.
     *
     * @param sbid
     *            the scheduling block id for the observation
     * @param opalCode
     *            the project code
     * @param details
     *            the observation, project and data product details to be updated
     * 
     * @throws ResourceNotFoundException
     *             if an observation with the given sbid could not be found
     * @throws BadRequestException
     *             if the sbid in the details does not match the supplied sbid
     */
    @RequestMapping(method = RequestMethod.PUT, value = "/observations/{sbid}/projects/{opalCode}/dataproducts",
            consumes = "application/json")
    public @ResponseBody void updateDataProductDetails(@PathVariable() Integer sbid, @PathVariable() String opalCode,
            @RequestBody ObservationProjectDataProductsDTO details) throws ResourceNotFoundException,
            BadRequestException
    {
        logger.info("Hit the controller for '/observations/{}/projects/{}/dataproducts'", sbid, opalCode);

        if (!sbid.equals(details.getSbid()) || !opalCode.equals(details.getOpalCode()))
        {
            throw new BadRequestException("Mismatch between sbid and project code in url and request body");
        }

        observationService.updateObservationProjectDataProducts(details);
    }
    
    /**
     * Adds a validation note to a given scheduling block (observation-project combination).
     *
     * @param sbid
     *            the scheduling block id for the observation
     * @param opalCode
     *            the project code
     * @param validationNoteDto
     *            the validation note to add
     * @return the new validation note
     * @throws ResourceNotFoundException
     *             if an observation with the given sbid could not be found
     */
    @RequestMapping(method = RequestMethod.POST, value = "/observations/{sbid}/projects/{opalCode}/validationnotes",
            consumes = "application/json", produces = "application/json")
    public @ResponseBody ValidationNoteDTO addValidationNote(@PathVariable() Integer sbid, @PathVariable() String opalCode,
            @RequestBody ValidationNoteDTO validationNoteDto) throws ResourceNotFoundException
    {
        logger.info("Hit the controller for '/observations/{}/projects/{}/validationnotes'", sbid, opalCode);

        return observationService.addValidationNote(sbid, opalCode, validationNoteDto);
    }
}
