package au.csiro.casda.deposit;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import au.csiro.casda.BadRequestException;
import au.csiro.casda.ResourceNotFoundException;
import au.csiro.casda.deposit.manager.Level7DepositService;
import au.csiro.casda.deposit.state.CasdaCatalogueProcessingDepositState;
import au.csiro.casda.dto.ParentDepositableDTO;
import au.csiro.casda.entity.observation.CatalogueType;
import au.csiro.casda.jobmanager.CasdaToolProcessJobBuilder;
import au.csiro.casda.jobmanager.ProcessJob;
import au.csiro.casda.jobmanager.SingleJobMonitor;
import au.csiro.casda.logging.CasdaLogMessageBuilderFactory;
import au.csiro.casda.logging.LogEvent;
import au.csiro.casda.services.dto.Message.MessageCode;
import au.csiro.casda.services.dto.MessageDTO;

import com.wordnik.swagger.annotations.Api;

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
 * RESTful web service controller. End point validates and publishes Level 7 Deposits.
 * 
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
@Api(basePath = "/projects", value = "Level 7 Deposit Control",
        description = "Controls deposit of level 7 collections as provided by CSIRO's Data Access portal")
@RestController
public class Level7DepositProcessingController
{

    private static Logger logger = LoggerFactory.getLogger(Level7DepositProcessingController.class);

    private String validationFileTempDirectory;

    private Level7DepositService level7DepositService;

    private CasdaToolProcessJobBuilderFactory builderFactory;

    private SingleJobMonitorFactory singleJobMonitorFactory;

    /**
     * Constructor
     * 
     * @param validationFileTempDirectory
     *            Temp directory
     * @param level7DepositService
     *            the level 7 deposit service
     * @param builderFactory
     *            the CasdaToolJobProcessBuilder factory
     * @param singleJobMonitorFactory
     *            the SingleJobMonitorFactory
     */
    @Autowired
    public Level7DepositProcessingController(@Value("${deposit.tempfile.dir}") String validationFileTempDirectory,
            Level7DepositService level7DepositService, CasdaToolProcessJobBuilderFactory builderFactory,
            SingleJobMonitorFactory singleJobMonitorFactory)
    {
        this.validationFileTempDirectory = validationFileTempDirectory;
        this.level7DepositService = level7DepositService;
        this.builderFactory = builderFactory;
        this.singleJobMonitorFactory = singleJobMonitorFactory;
    }

    /**
     * REST end point for Level 7 catalogue validation.
     * 
     * @param opalCode
     *            The Opal Code
     * @param collectionId
     *            The catalogue's collection id
     * @param file
     *            The catalogue file
     * @return A List of validation errors, empty if file is valid
     * @throws Exception
     *             Any IO or validation runtime exception
     */
    @RequestMapping(value = "/projects/{opalCode}/level_7_collections/{collectionId}/catalogues/validate",
            method = RequestMethod.POST)
    public @ResponseBody String[] validateLevel7Catalogue(@PathVariable("opalCode") String opalCode,
            @PathVariable("collectionId") String collectionId, @RequestParam("file") MultipartFile file)
            throws Exception
    {
        logger.info(
                "Hit the controller for the '/projects/{}/level_7_collections/{}/catalogues/validate' with file '{}'",
                opalCode, collectionId, file.getOriginalFilename());

        if (file.isEmpty())
        {
            String[] errors = { "File is empty" };
            logger.info("Validation complete with following messages: {}", (Object) errors);
            return errors;
        }

        File catalogue = new File(validationFileTempDirectory, "tmpCat-" + collectionId + ".xml");
        try (InputStream in = file.getInputStream();
                BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(catalogue));)
        {

            IOUtils.copyLarge(in, out);
            out.close(); // We need to close the out stream so that the full file is present for validation

            String[] validationMessages = validateCatalogue(catalogue, collectionId);

            logger.info("Validation complete with following messages: {}", (Object) validationMessages);
            return validationMessages;
        }
        catch (Exception e)
        {
            logger.error(
                    CasdaLogMessageBuilderFactory.getCasdaMessageBuilder(LogEvent.UNKNOWN_EVENT)
                            .add(String.format("An unexpected exception occured trying to validate catalogue file."))
                            .toString(), e);
            throw e;
        }
        finally
        {
            catalogue.delete();
        }
    }

    /**
     * REST end point for saving a Level 7 catalogue file, ready for depositing.
     * 
     * @param opalCode
     *            The Opal Code
     * @param collectionId
     *            The catalogue's collection id
     * @param file
     *            The catalogue file
     * @return A messageDTO with information of success
     * @throws BadRequestException
     *             if the level 7 collection deposit has already been initiated, or the supplied file is empty
     */
    @RequestMapping(value = "/projects/{opalCode}/level_7_collections/{collectionId}/catalogues",
            method = RequestMethod.POST)
    public @ResponseBody MessageDTO saveCatalogueForDeposit(@PathVariable("opalCode") String opalCode,
            @PathVariable("collectionId") String collectionId, @RequestParam("file") MultipartFile file)
            throws BadRequestException
    {
        logger.info("Hit the controller for the '/projects/{}/level_7_collections/{}/catalogues' with file {}",
                opalCode, collectionId, file.getOriginalFilename());

        ParentDepositableDTO level7CollectionInfo = null;
        try
        {
            level7CollectionInfo =
                    this.level7DepositService.getLevel7CollectionSummary(opalCode,
                            parseAndValidateCollectionId(collectionId));
        }
        catch (Level7DepositService.CollectionProjectCodeMismatchException ex)
        {
            /*
             * This exception can only be thrown if the Level7Collection record exists, which means that the deposit has
             * already been initiated.
             */
            throw new BadRequestException(String.format(
                    "Level 7 collection with id '%s' deposit has already been initiated", collectionId));
        }
        if (level7CollectionInfo != null && !level7CollectionInfo.isBuildingDeposit())
        {
            throw new BadRequestException(String.format(
                    "Level 7 collection with id '%s' deposit has already been initiated", collectionId));
        }

        if (file.isEmpty())
        {
            throw new BadRequestException("Incoming file is empty " + file.getOriginalFilename());
        }

        try (InputStream in = file.getInputStream())
        {
            // create the collection directory if it doesn't exist
            String catalogueFilePath =
                    level7DepositService.saveFileForLevel7CollectionDeposit(parseAndValidateCollectionId(collectionId),
                            file.getOriginalFilename(), in);
            return new MessageDTO(MessageCode.SUCCESS, "Successfully created file " + catalogueFilePath);
        }
        catch (IOException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    /**
     * REST end point for indicating that a level 7 collection is ready for processing.
     * 
     * @param opalCode
     *            The Opal Code
     * @param collectionId
     *            The catalogue's collection id
     * @return A messageDTO with information of success
     * @throws ResourceNotFoundException
     *             if a level 7 collection with the specified dap collection id could not be found
     * @throws BadRequestException
     *             if the level 7 collection has no files, or if the level 7 collection deposit has already been
     *             initiated
     */
    @RequestMapping(value = "/projects/{opalCode}/level_7_collections/{collectionId}/ready",
            method = RequestMethod.POST)
    public @ResponseBody MessageDTO initiateLevel7CollectionDeposit(@PathVariable("opalCode") String opalCode,
            @PathVariable("collectionId") String collectionId) throws ResourceNotFoundException, BadRequestException
    {
        logger.info("Hit the controller for '/projects/{}/level_7_collections/{}/ready", opalCode, collectionId);

        try
        {
            level7DepositService.initiateLevel7CollectionDeposit(opalCode, parseAndValidateCollectionId(collectionId));
        }
        catch (Level7DepositService.CollectionIllegalStateException e)
        {
            throw new BadRequestException(e.getMessage());
        }

        logger.info(CasdaLogMessageBuilderFactory.getCasdaMessageBuilder(DepositManagerEvents.E036).add(collectionId)
                .addCustomMessage("Project code: " + opalCode).toString());

        return new MessageDTO(MessageCode.SUCCESS, "Successfully created level 7 collection " + collectionId);
    }

    /**
     * REST end point for recovering a failed Level 7 collection deposit.
     * 
     * @param opalCode
     *            The Opal Code
     * @param collectionId
     *            The catalogue's collection id
     * @return A messageDTO with information of success
     * @throws BadRequestException
     * @throws ResourceNotFoundException
     *             if a level 7 collection with the specified dap collection id could not be found
     * @throws BadRequestException
     *             if the level 7 collection with the specified dap collection id has a different opal project code to
     *             the one specified, or if the level 7 collection has not failed
     */
    @RequestMapping(value = "/projects/{opalCode}/level_7_collections/{collectionId}/recover",
            method = RequestMethod.POST)
    public @ResponseBody MessageDTO recoverFailedLevel7CollectionDeposit(@PathVariable("opalCode") String opalCode,
            @PathVariable("collectionId") String collectionId) throws ResourceNotFoundException, BadRequestException
    {
        logger.info("Hit the controller for '/projects/{}/level_7_collections/{}/recover", opalCode, collectionId);

        long id = parseAndValidateCollectionId(collectionId);
        try
        {
            level7DepositService.recoverFailedLevel7CollectionDeposit(opalCode, id);
        }
        catch (Level7DepositService.UnknownCollectionException ex)
        {
            logger.error(CasdaLogMessageBuilderFactory.getCasdaMessageBuilder(DepositManagerEvents.E127)
                    .add(collectionId).addCustomMessage("Project code: " + opalCode).toString());
            throw new ResourceNotFoundException(ex.getMessage());
        }
        catch (Level7DepositService.CollectionProjectCodeMismatchException ex)
        {
            logger.error(CasdaLogMessageBuilderFactory.getCasdaMessageBuilder(DepositManagerEvents.E124).toString(), ex);
            throw new BadRequestException(ex.getMessage());
        }
        catch (Level7DepositService.CollectionIllegalStateException ex)
        {
            logger.error(CasdaLogMessageBuilderFactory.getCasdaMessageBuilder(DepositManagerEvents.E123)
                    .add(collectionId).addCustomMessage("Project code: " + opalCode).toString());
            throw new BadRequestException(ex.getMessage());
        }
        catch (RuntimeException ex)
        {
            logger.error(
                    CasdaLogMessageBuilderFactory.getCasdaMessageBuilder(DepositManagerEvents.E125).add(collectionId)
                            .addCustomMessage("Project code: " + opalCode).toString(), ex);
            throw ex;
        }
        logger.info(CasdaLogMessageBuilderFactory.getCasdaMessageBuilder(DepositManagerEvents.E126).add(collectionId)
                .addCustomMessage("Project code: " + opalCode).toString());
        return new MessageDTO(MessageCode.SUCCESS, "Successfully recovered level 7 collection " + collectionId);

    }

    /**
     * Returns a ParentDepositableDTO describing the Level 7 deposit identified by the given opalCode and collectionId.
     * 
     * @param opalCode
     *            the project's opal code
     * @param collectionId
     *            the data collection id
     * @return a ParentDepositableArtefactDTO
     * @throws ResourceNotFoundException
     *             if a level 7 collection with the specified dap collection id could not be found
     * @throws BadRequestException
     *             if the level 7 collection with the specified dap collection id has a different opal project code to
     *             the one specified
     */
    @Transactional
    @RequestMapping(value = "/projects/{opalCode}/level_7_collections/{collectionId}", method = RequestMethod.GET,
            produces = { "application/json" })
    public @ResponseBody ParentDepositableDTO getLevel7CollectionSummary(@PathVariable("opalCode") String opalCode,
            @PathVariable("collectionId") String collectionId) throws ResourceNotFoundException, BadRequestException
    {
        logger.info("Hit the controller for the '/project/{}/level_7_collection/{}' (GET)", opalCode, collectionId);

        try
        {
            ParentDepositableDTO level7CollectionSummary =
                    level7DepositService.getLevel7CollectionSummary(opalCode,
                            parseAndValidateCollectionId(collectionId));
            if (level7CollectionSummary == null)
            {
                throw new ResourceNotFoundException(String.format("No level 7 collection matching collection id '%s'",
                        collectionId));
            }
            return level7CollectionSummary;
        }
        catch (Level7DepositService.CollectionProjectCodeMismatchException e)
        {
            throw new BadRequestException(e.getMessage());
        }
    }

    /**
     * Checks that the collection id is a long greater than 0
     * 
     * @param collectionId
     *            the collection id
     * @return the collection id as a long
     * @throws BadRequestException
     */
    private long parseAndValidateCollectionId(String collectionId) throws BadRequestException
    {
        long collectionIdLong;
        try
        {
            collectionIdLong = Long.parseLong(collectionId);
        }
        catch (NumberFormatException e)
        {
            throw new BadRequestException("Invalid collection id");
        }

        if (collectionIdLong <= 0)
        {
            throw new BadRequestException("Invalid collection id");
        }

        return collectionIdLong;
    }

    /**
     * Validate a temp catalogue file using the importer command line tool with the validate-only flag
     * 
     * @param catalogueFile
     *            Temp catalogue file
     * @param collectionId
     *            The collection id
     * @return An array of validation errors, empty if valid
     */
    private String[] validateCatalogue(File catalogueFile, String collectionId)
    {
        String[] errors = null;

        CasdaToolProcessJobBuilder processBuilder = builderFactory.createBuilder();

        ProcessJob validationJob =
                processBuilder.setCommand(CasdaCatalogueProcessingDepositState.CATALOGUE_IMPORTER_TOOL_NAME)
                        .addCommandArgument("-catalogue-type", CatalogueType.LEVEL7.getName())
                        .addCommandArgument("-parent-id", collectionId)
                        .addCommandArgument("-catalogue-filename", catalogueFile.getName())
                        .addCommandArgument("-infile", catalogueFile.getPath()).addCommandSwitch("-validate-only")
                        .createJob(null, CasdaCatalogueProcessingDepositState.CATALOGUE_IMPORTER_TOOL_NAME);

        SingleJobMonitor monitor = singleJobMonitorFactory.createSingleJobMonitor();
        validationJob.run(monitor);
        if (monitor.isJobFailed())
        {
            throw new RuntimeException(monitor.getJobOutput());
        }
        else
        {
            if (StringUtils.isBlank(monitor.getJobOutput()))
            {
                errors = new String[0];
            }
            else
            {
                String[] output = monitor.getJobOutput().split("\n");
                List<String> jobOutputErrors = new ArrayList<>();
                for (String line : output)
                {
                    line = line.trim();
                    if (line.matches("^Error in .*:.*$"))
                    {
                        jobOutputErrors.add(line);
                    }
                }
                errors = jobOutputErrors.toArray(new String[jobOutputErrors.size()]);
            }
        }

        return errors;
    }
}
