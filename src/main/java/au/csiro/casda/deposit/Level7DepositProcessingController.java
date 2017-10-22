package au.csiro.casda.deposit;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.wordnik.swagger.annotations.Api;

import au.csiro.casda.BadRequestException;
import au.csiro.casda.ResourceNotFoundException;
import au.csiro.casda.deposit.manager.Level7DepositService;
import au.csiro.casda.deposit.manager.Level7DepositService.CollectionIllegalStateException;
import au.csiro.casda.deposit.manager.Level7DepositService.UnknownCollectionException;
import au.csiro.casda.deposit.state.CasdaCatalogueProcessingDepositState;
import au.csiro.casda.dto.DepositableArtefactDTO;
import au.csiro.casda.dto.ParentDepositableDTO;
import au.csiro.casda.entity.observation.CatalogueType;
import au.csiro.casda.entity.observation.Level7Collection;
import au.csiro.casda.jobmanager.CasdaToolProcessJobBuilder;
import au.csiro.casda.jobmanager.ProcessJob;
import au.csiro.casda.jobmanager.SingleJobMonitor;
import au.csiro.casda.logging.CasdaLogMessageBuilderFactory;
import au.csiro.casda.logging.LogEvent;
import au.csiro.casda.services.dto.Message.MessageCode;
import au.csiro.casda.services.dto.MessageDTO;

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

    private String pathWhitelistRegex;

    /**
     * Constructor
     * 
     * @param validationFileTempDirectory
     *            Temp directory
     * @param pathWhitelistRegex
     *            the pattern for allowed level 7 source paths
     * @param level7DepositService
     *            the level 7 deposit service
     * @param builderFactory
     *            the CasdaToolJobProcessBuilder factory
     * @param singleJobMonitorFactory
     *            the SingleJobMonitorFactory
     */
    @Autowired
    public Level7DepositProcessingController(@Value("${deposit.tempfile.dir}") String validationFileTempDirectory,
            @Value("${level7.path.whitelist.regex}") String pathWhitelistRegex,
            Level7DepositService level7DepositService, CasdaToolProcessJobBuilderFactory builderFactory,
            SingleJobMonitorFactory singleJobMonitorFactory)
    {
        this.validationFileTempDirectory = validationFileTempDirectory;
        this.level7DepositService = level7DepositService;
        this.builderFactory = builderFactory;
        this.singleJobMonitorFactory = singleJobMonitorFactory;
        this.pathWhitelistRegex = StringUtils.trimToEmpty(pathWhitelistRegex);
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
     * @param dcCommonId
     *            The base collection id shared by all versions of this data collection.
     * @return A List of validation errors, empty if file is valid
     * @throws Exception
     *             Any IO or validation runtime exception
     */
    @RequestMapping(value = "/projects/{opalCode}/level_7_collections/{collectionId}/catalogues/validate",
            method = RequestMethod.POST)
    public @ResponseBody String[] validateLevel7Catalogue(@PathVariable("opalCode") String opalCode,
            @PathVariable("collectionId") String collectionId, @RequestParam String dcCommonId,
            @RequestParam("file") MultipartFile file) throws Exception
    {
        logger.info(
                "Hit the controller for the '/projects/{}/level_7_collections/{}/catalogues/validate' with file '{}'"
                        + " for dcCommonId {}",
                opalCode, collectionId, file.getOriginalFilename(), dcCommonId);

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

            String[] validationMessages = validateCatalogue(catalogue, collectionId, dcCommonId);

            logger.info("Validation complete with following messages: {}", (Object) validationMessages);
            return validationMessages;
        }
        catch (Exception e)
        {
            logger.error(CasdaLogMessageBuilderFactory.getCasdaMessageBuilder(LogEvent.UNKNOWN_EVENT)
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
            level7CollectionInfo = this.level7DepositService.getLevel7CollectionSummary(opalCode,
                    parseAndValidateCollectionId(collectionId));
        }
        catch (Level7DepositService.CollectionProjectCodeMismatchException ex)
        {
            /*
             * This exception can only be thrown if the Level7Collection record exists, which means that the deposit has
             * already been initiated.
             */
            throw new BadRequestException(
                    String.format("Level 7 collection with id '%s' deposit has already been initiated", collectionId));
        }
        if (level7CollectionInfo != null && !level7CollectionInfo.isBuildingDeposit())
        {
            throw new BadRequestException(
                    String.format("Level 7 collection with id '%s' deposit has already been initiated", collectionId));
        }

        if (file.isEmpty())
        {
            throw new BadRequestException("Incoming file is empty " + file.getOriginalFilename());
        }

        try (InputStream in = file.getInputStream())
        {
            // create the collection directory if it doesn't exist
            String catalogueFilePath = level7DepositService.saveFileForLevel7CollectionDeposit(
                    parseAndValidateCollectionId(collectionId), file.getOriginalFilename(), in);
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
     * @param dcCommonId
     *            The base collection id shared by all versions of this data collection.
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
            @PathVariable("collectionId") String collectionId, @RequestParam String dcCommonId)
            throws ResourceNotFoundException, BadRequestException
    {
        logger.info("Hit the controller for '/projects/{}/level_7_collections/{}/ready for dcCommonId {}", opalCode,
                collectionId, dcCommonId);

        try
        {
            level7DepositService.initiateLevel7CollectionDeposit(opalCode, parseAndValidateCollectionId(collectionId),
                    (int) parseAndValidateCollectionId(dcCommonId));
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
     * 			   for a bad service request
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
            logger.error(CasdaLogMessageBuilderFactory.getCasdaMessageBuilder(DepositManagerEvents.E124).toString(),
                    ex);
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
            logger.error(CasdaLogMessageBuilderFactory.getCasdaMessageBuilder(DepositManagerEvents.E125)
                    .add(collectionId).addCustomMessage("Project code: " + opalCode).toString(), ex);
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
        logger.info("Hit the controller for the '/projects/{}/level_7_collections/{}' (GET)", opalCode, collectionId);

        try
        {
            ParentDepositableDTO level7CollectionSummary = level7DepositService.getLevel7CollectionSummary(opalCode,
                    parseAndValidateCollectionId(collectionId));
            if (level7CollectionSummary == null)
            {
                throw new ResourceNotFoundException(
                        String.format("No level 7 collection matching collection id '%s'", collectionId));
            }
            return level7CollectionSummary;
        }
        catch (Level7DepositService.CollectionProjectCodeMismatchException e)
        {
            throw new BadRequestException(e.getMessage());
        }
    }

    /**
     * Creates or updates a ParentDepositableDTO describing the Level 7 deposit identified by the given opalCode and 
     * collectionId.
     * 
     * @param opalCode
     *            the project's opal code
     * @param collectionId
     *            the data collection id
     * @param dcCommonId
     *            The base collection id shared by all versions of this data collection.
     * @param filePath
     *            The server file system path for each file type
     * @param fileType
     *            The file type for each path e.g. SPECTRUM, MOMENT_MAP, IMAGE_CUBE
     * @return a ParentDepositableArtefactDTO
     * @throws ResourceNotFoundException
     *             if a level 7 collection with the specified dap collection id could not be found
     * @throws BadRequestException
     *             if the level 7 collection with the specified dap collection id has a different opal project code to
     *             the one specified
     */
    @Transactional
    @RequestMapping(value = "/projects/{opalCode}/level_7_collections/{collectionId}", method = RequestMethod.POST,
            produces = { "application/json" })
    public @ResponseBody ParentDepositableDTO saveLevel7Collection(@PathVariable("opalCode") String opalCode,
            @PathVariable("collectionId") String collectionId, @RequestParam String dcCommonId,
            @RequestParam String filePath[], @RequestParam String fileType[])
            throws ResourceNotFoundException, BadRequestException
    {
        logger.info("Hit the controller for the '/projects/{}/level_7_collections/{}' (POST)", opalCode, collectionId);

        // First validate the paths and types
        if (!ArrayUtils.isSameLength(filePath, fileType))
        {
            throw new BadRequestException("The same number of filePath and fileType parameters must be supplied.");
        }

        List<String> errorList = new ArrayList<>(); 
        Path[] dataDirPath = new Path[fileType.length];
        File[] dataDir = new File[fileType.length];
        for (int i = 0; i < fileType.length; i++)
        {
            if (!filePath[i].matches(pathWhitelistRegex))
            {
                errorList.add(String.format("%s:InvalidPath", fileType[i]));
            }
            else
            {
                dataDirPath[i] = Paths.get(filePath[i]);
                dataDir[i] = dataDirPath[i].toFile();
                if (!(dataDir[i].exists() && dataDir[i].isDirectory() && dataDir[i].canRead()))
                {
                    errorList.add(String.format("%s:InaccessiblePath", fileType[i]));
                }
            }
        }

        if (!errorList.isEmpty())
        {
            throw new ResourceNotFoundException(StringUtils.join(errorList, ". "));
        }

        // Now create or update the collection record with the supplied paths
        try
        {
            Level7Collection level7Collection =
                    level7DepositService.getLevel7Collection(opalCode, parseAndValidateCollectionId(collectionId));
            if (level7Collection == null)
            {
                // New record - we need to create it
                level7Collection = level7DepositService.createEmptyLevel7Collection(opalCode,
                        parseAndValidateCollectionId(collectionId), (int) parseAndValidateCollectionId(dcCommonId));
            }

            String imageCubePath = null;
            String spectrumPath = null;
            String momentMapPath = null;
            String cubeletPath = null;
            for (int i = 0; i < fileType.length; i++)
            {
                switch (fileType[i].toUpperCase())
                {

                case "IMAGE_CUBE":
                    imageCubePath = dataDirPath[i].toString();
                    break;

                case "SPECTRUM":
                    spectrumPath = dataDirPath[i].toString();
                    break;

                case "MOMENT_MAP":
                    momentMapPath = dataDirPath[i].toString();
                    break;

                case "CUBELET":
                	cubeletPath = dataDirPath[i].toString();
                    break;

                default:
                    throw new BadRequestException(String.format("Invalid fileType received '%s'.", fileType[i]));
                }
            }
            
            level7DepositService.updateFilePaths(
            		level7Collection, imageCubePath, spectrumPath, momentMapPath, cubeletPath);
            ParentDepositableDTO level7CollectionSummary = new ParentDepositableDTO(level7Collection, null);
            return level7CollectionSummary;
        }
        catch (CollectionIllegalStateException | Level7DepositService.CollectionProjectCodeMismatchException e)
        {
            throw new BadRequestException(e.getMessage());
        }
    }

    /**
     * Returns a ParentDepositableDTO describing the files at the specified file path. The path is checked against a 
     * whitelist, then any non hidden files in the folder or its subdirectories are included in the depositableArtefacts
     * array.
     * 
     * @param opalCode
     *            the project's opal code
     * @param collectionId
     *            the data collection id
     * @return a ParentDepositableArtefactDTO
     * @throws ResourceNotFoundException
     *             if the filePath referred to a path that is either not allowed, not present or not readable by this
     *             process.
     * @throws BadRequestException
     *             if the level 7 collection with the specified dap collection id has a different opal project code to
     *             the one specified
     */
    @Transactional
    @RequestMapping(value = "/projects/{opalCode}/level_7_collections/{collectionId}/file_list",
            method = RequestMethod.GET, produces = { "application/json" })
    public @ResponseBody Map<String, DepositableArtefactDTO[]> getLevel7CollectionFileList(
            @PathVariable("opalCode") String opalCode, @PathVariable("collectionId") String collectionId)
            throws ResourceNotFoundException, BadRequestException
    {
        logger.info("Hit the controller for the '/projects/{}/level_7_collections/{}/file_list' (GET)", opalCode,
                collectionId);

        try
        {
            Level7Collection level7Collection =
                    level7DepositService.getLevel7Collection(opalCode, parseAndValidateCollectionId(collectionId));
            if (level7Collection == null)
            {
                throw new ResourceNotFoundException(
                        String.format("No level 7 collection matching collection id '%s'", collectionId));
            }

            Map<String, DepositableArtefactDTO[]> typeFileMap= new HashMap<>();
            populateFileMap(typeFileMap, "IMAGE_CUBE", level7Collection.getImageCubePath());
            populateFileMap(typeFileMap, "SPECTRUM", level7Collection.getSpectrumPath());
            populateFileMap(typeFileMap, "MOMENT_MAP", level7Collection.getMomentMapPath());
            populateFileMap(typeFileMap, "CUBELET", level7Collection.getCubeletPath());

            return typeFileMap;
        }
        catch (Level7DepositService.CollectionProjectCodeMismatchException e)
        {
            throw new BadRequestException(e.getMessage());
        }

    }

    private void populateFileMap(Map<String, DepositableArtefactDTO[]> typeFileMap, String mapKey, String filePath)
    {
        List<DepositableArtefactDTO> fileList = getFileList(filePath);
        if (fileList == null || fileList.isEmpty())
        {
            return;
        }

        typeFileMap.put(mapKey, fileList.toArray(new DepositableArtefactDTO[fileList.size()]));

    }

    private List<DepositableArtefactDTO> getFileList(String filePath)
    {
        if (StringUtils.isEmpty(filePath))
        {
            return null;
        }
                
        Path dataDirPath = Paths.get(filePath);
        File dataDir = dataDirPath.toFile();

        IOFileFilter ignoreHiddenFilter = FileFilterUtils.notFileFilter(FileFilterUtils.prefixFileFilter("."));
        Collection<File> files = FileUtils.listFiles(dataDir, ignoreHiddenFilter, ignoreHiddenFilter);
        List<String> paths = new ArrayList<>();
        for (File file : files)
        {
            Path path = file.toPath();
            paths.add(dataDirPath.relativize(path).toString());
        }
        Collections.sort(paths);
        
        List<DepositableArtefactDTO> depositables = new ArrayList<>();
        for (File file : files)
        {
            String nameNoExtension;
            if (file.getName().endsWith(".fits"))
            {
                nameNoExtension = file.getName().substring(0,file.getName().length()-(".fits".length()));
            }
            else if (file.getName().endsWith(".fits.gz"))
            {
                nameNoExtension = file.getName().substring(0,file.getName().length()-(".fits.gz".length()));
            }
            else
            {
                continue;
            }

            DepositableArtefactDTO artefact = new DepositableArtefactDTO();
            Path path = file.toPath();
            artefact.setFilename(dataDirPath.relativize(path).toString());
            artefact.setFilesizeInBytes(file.length());
            
            String [] extensions = new String [] {"png", "jpg", "jpeg"};
            for (String imgSuffix : extensions)
            {
                String thumbnailName = nameNoExtension + "." + imgSuffix;
                if (paths.contains(thumbnailName))
                {
                    artefact.setThumbnailName(thumbnailName);
                }
            }
            depositables.add(artefact);
        }
        return depositables;
    }

    /**
     * Release the level 7 collection - ie set the related data products' release date to the provided value.
     * 
     * @param opalCode
     *            the project's opal code
     * @param collectionId
     *            the data collection id
     * @param date
     *            the release date in UTC timezone
     * @return MessageDTO on success
     * @throws ResourceNotFoundException
     *             if an Project Block with the specified scheduling block ID and opal code could not be found
     * @throws BadRequestException
     *             if the date is not valid
     * @throws UnknownCollectionException
     *             If the collection cannot be found.
     */
    @Transactional
    @RequestMapping(value = "/projects/{opalCode}/level_7_collections/{collectionId}/release",
            method = RequestMethod.POST, produces = { "application/json" })
    public @ResponseBody MessageDTO releaseLevel7Collection(@PathVariable("opalCode") String opalCode,
            @PathVariable("collectionId") String collectionId, @RequestParam() Long date)
            throws ResourceNotFoundException, BadRequestException, UnknownCollectionException
    {
        logger.info("Hit the controller for the '/projects/{}/level_7_collections/{}/release' (POST)", opalCode,
                collectionId);

        if (date == null || date < 0)
        {
            throw new BadRequestException("Invalid date parameter. Date must be greater or equal to zero");
        }

        try
        {
            DateTime releaseDate = new DateTime(date, DateTimeZone.UTC);

            return level7DepositService.releaseLevel7Collection(opalCode, parseAndValidateCollectionId(collectionId),
                    releaseDate);
        }
        catch (Level7DepositService.CollectionProjectCodeMismatchException e)
        {
            throw new BadRequestException(e.getMessage());
        }

    }

    /**
     * Starts the validation process for a level 7 image/spectra collection. The validation process will run 
     * asynchronously and progress may be checked using the getLevel7CollectionSummary endpoint. If the process cannot
     * be started an exception will be thrown.  
     * 
     * @param opalCode
     *            the project's opal code
     * @param dapCollectionId
     *            the data collection id
     * @param sbids
     * 			  The sbids to attach to this level 7 collection
     * @return a success result with a message.
     * @throws ResourceNotFoundException
     *             if the filePath referred to a path that is either not allowed, not present or not readable by this
     *             process.
     * @throws BadRequestException
     *             if the level 7 collection with the specified dap collection id has a different opal project code to
     *             the one specified or is not a state which allows validation
     */
    @Transactional
    @RequestMapping(value = "/projects/{opalCode}/level_7_collections/{collectionId}/validate",
            method = RequestMethod.POST, produces = { "application/json" })
    public @ResponseBody MessageDTO validateLevel7Collection(
            @PathVariable("opalCode") String opalCode, @PathVariable("collectionId") String dapCollectionId, 
            @RequestBody(required = false) Integer[] sbids) throws ResourceNotFoundException, BadRequestException
    {
        logger.info("Hit the controller for the '/projects/{}/level_7_collections/{}/validate' (POST)", opalCode,
                dapCollectionId);

        try
        {
            Level7Collection level7Collection = level7DepositService.initiateLevel7CollectionValidate(opalCode,
                    parseAndValidateCollectionId(dapCollectionId), sbids);
            if (level7Collection == null)
            {
                throw new ResourceNotFoundException(
                        String.format("No level 7 collection matching collection id '%s'", dapCollectionId));
            }

            return new MessageDTO(MessageCode.SUCCESS,
                    "Successfully started validation of level 7 collection " + dapCollectionId);
        }
        catch (Level7DepositService.CollectionProjectCodeMismatchException | CollectionIllegalStateException e)
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
     * @param dcCommonId
     *            The base collection id shared by all versions of this data collection.
     * @return An array of validation errors, empty if valid
     */
    private String[] validateCatalogue(File catalogueFile, String collectionId, String dcCommonId)
    {
        String[] errors = null;

        CasdaToolProcessJobBuilder processBuilder = builderFactory.createBuilder();

        ProcessJob validationJob =
                processBuilder.setCommand(CasdaCatalogueProcessingDepositState.CATALOGUE_IMPORTER_TOOL_NAME)
                        .addCommandArgument("-catalogue-type", CatalogueType.DERIVED_CATALOGUE.getName())
                        .addCommandArgument("-parent-id", collectionId)
                        .addCommandArgument("-catalogue-filename", catalogueFile.getName())
                        .addCommandArgument("-infile", catalogueFile.getPath())
                        .addCommandArgument("-dc-common-id", dcCommonId).addCommandSwitch("-validate-only")
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
