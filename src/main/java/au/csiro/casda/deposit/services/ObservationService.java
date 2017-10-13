package au.csiro.casda.deposit.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

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


import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import au.csiro.casda.BadRequestException;
import au.csiro.casda.ResourceNotFoundException;
import au.csiro.casda.ServerException;
import au.csiro.casda.Utils;
import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.deposit.DepositManagerEvents;
import au.csiro.casda.deposit.SingleJobMonitorFactory;
import au.csiro.casda.deposit.jpa.ObservationRefreshRepository;
import au.csiro.casda.deposit.jpa.ObservationRepository;
import au.csiro.casda.deposit.jpa.ValidationNoteRepository;
import au.csiro.casda.dto.DataProductDTO;
import au.csiro.casda.dto.ObservationProjectDataProductsDTO;
import au.csiro.casda.dto.QualityFlagDTO;
import au.csiro.casda.dto.ValidationNoteDTO;
import au.csiro.casda.entity.CasdaDataProductEntity;
import au.csiro.casda.entity.QualityFlag;
import au.csiro.casda.entity.ValidationNote;
import au.csiro.casda.entity.observation.Catalogue;
import au.csiro.casda.entity.observation.Cubelet;
import au.csiro.casda.entity.observation.EvaluationFile;
import au.csiro.casda.entity.observation.ImageCube;
import au.csiro.casda.entity.observation.MeasurementSet;
import au.csiro.casda.entity.observation.MomentMap;
import au.csiro.casda.entity.observation.Observation;
import au.csiro.casda.entity.observation.Project;
import au.csiro.casda.entity.observation.QualityLevel;
import au.csiro.casda.entity.observation.Spectrum;
import au.csiro.casda.jobmanager.ProcessJob;
import au.csiro.casda.jobmanager.ProcessJobBuilder.ProcessJobFactory;
import au.csiro.casda.jobmanager.SimpleToolProcessJobBuilder;
import au.csiro.casda.jobmanager.SingleJobMonitor;
import au.csiro.casda.logging.CasdaLogMessageBuilderFactory;
import au.csiro.casda.logging.LogEvent;
import au.csiro.casda.services.dto.Message.MessageCode;
import au.csiro.casda.services.dto.MessageDTO;

/**
 * Service to retrieve and update observation information
 *
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
@Service
public class ObservationService
{

    private static final Logger logger = LoggerFactory.getLogger(ObservationService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObservationRepository observationRepository;

    @Autowired
    private ObservationRefreshRepository observationRefreshRepository;
    
    @Autowired
    private ValidationNoteRepository validationNoteRepository;

    @Autowired
    private QualityService qualityService;

    @Autowired
    private ProcessJobFactory processJobFactory;

    @Autowired
    private SingleJobMonitorFactory singleJobMonitorFactory;

    @Autowired
    @Value("${job.manager.recent.age.hours}")
    private int maxAgeOfRecentCompletedJobs;

    @Autowired
    @Value("${deposit.tools.working.directory}")
    private String depositToolsWorkingDirectory;

    @Autowired
    @Value("${build.catalogue.hips.command.and.args}")
    private String buildCatalogueHipsCommandAndArgs;

    private List<String> invalidObsIds;

    
    /**
     * Create a new instance of the ObservationService
     */
    public ObservationService()
    {
        invalidObsIds = new ArrayList<String>();
    }
    
    /**
     * Retrieve the list of project blocks that are not yet released.
     * 
     * @return The project block (opal code, sbid) of the unreleased observations
     */
    public Set<ObservationProjectDataProductsDTO> getUnreleasedProjectBlocks()
    {
        Set<ObservationProjectDataProductsDTO> unreleasedProjectBlocks = new HashSet<>();
        unreleasedProjectBlocks.addAll(observationRepository.findDepositedProjectBlocksWithUnreleasedImageCubes());
        unreleasedProjectBlocks.addAll(observationRepository.findDepositedProjectBlocksWithUnreleasedSpectra());
        unreleasedProjectBlocks.addAll(observationRepository.findDepositedProjectBlocksWithUnreleasedMomentMaps());
        unreleasedProjectBlocks.addAll(observationRepository.findDepositedProjectBlocksWithUnreleasedCubelets());
        unreleasedProjectBlocks.addAll(observationRepository.findDepositedProjectBlocksWithUnreleasedCatalogues());
        unreleasedProjectBlocks.addAll(observationRepository.findDepositedProjectBlocksWithUnreleasedMeasurementSets());
        return unreleasedProjectBlocks;
    }

    /**
     * Retrieve a list of released project blocks for a given project code
     * 
     * @param projectCode
     *            the project code related to the observations
     * @param date
     *            (optional) if this is absent, returns all observations for a project; if this is present, returns all
     *            observations released after this date
     * @return A set of project blocks (project code, observation's sbid), all will have the same project code
     */
    public Set<ObservationProjectDataProductsDTO> getReleasedProjectBlocks(String projectCode, Long date)
    {
        if (date == null)
        {
            date = 0L;
        }
        DateTime timestamp = new DateTime(date.longValue(), DateTimeZone.UTC);

        Set<ObservationProjectDataProductsDTO> releasedObservations = new HashSet<>();
        releasedObservations.addAll(observationRepository.findProjectBlocksWithReleasedImageCubesAfterDate(projectCode,
                timestamp));
        releasedObservations.addAll(observationRepository.findProjectBlocksWithReleasedSpectraAfterDate(projectCode,
                timestamp));
        releasedObservations.addAll(observationRepository.findProjectBlocksWithReleasedMomentMapsAfterDate(projectCode,
                timestamp));
        releasedObservations.addAll(observationRepository.findProjectBlocksWithReleasedCubeletsAfterDate(projectCode,
                timestamp));
        releasedObservations.addAll(observationRepository.findProjectBlocksWithReleasedMeasurementSetsAfterDate(
                projectCode, timestamp));
        releasedObservations.addAll(observationRepository.findProjectBlocksWithReleasedCataloguesAfterDate(projectCode,
                timestamp));
        return releasedObservations;
    }

    /**
     * Retrieve a list of project blocks for observations refreshed since the given date.
     * 
     * @param date
     *            Returns all observations refreshed after this date
     * @return A set of project blocks (project code, observation's sbid), all will have the same project code
     */
    public Set<ObservationProjectDataProductsDTO> getRefreshedProjectBlocks(Long date)
    {
        if (date == null)
        {
            date = 0L;
        }
        DateTime timestamp = new DateTime(date.longValue(), DateTimeZone.UTC);
        
        List<Long> obsIds = observationRefreshRepository.getIdsOfObservationsRefreshedSince(timestamp);

        Set<ObservationProjectDataProductsDTO> releasedObservations = new HashSet<>();
        if (CollectionUtils.isNotEmpty(obsIds))
        {
            releasedObservations.addAll(observationRepository.findProjectBlocksWithImageCubes(obsIds));
            releasedObservations.addAll(observationRepository.findProjectBlocksWithSpectra(obsIds));
            releasedObservations.addAll(observationRepository.findProjectBlocksWithMomentMaps(obsIds));
          	releasedObservations.addAll(observationRepository.findProjectBlocksWithCubelets(obsIds));
        
        }
        return releasedObservations;
    }

    /**
     * Set the release date of the project block (project and observation combination) to now
     * 
     * @param sbid
     *            scheduling block id of the observation.
     * @param opalCode
     *            the project's code in OPAL
     * @param releasedDate
     *            the release date to set. Note we are using {@link DateTime} which contains timezone information;
     *            timezone should be set to UTC
     * 
     * @return MessageDTO with information about success/failure
     * @throws ResourceNotFoundException
     *             if an Observation with the given scheduling block ID could not be found
     * @throws BadRequestException
     *             if any of the data products for the project block have not been validated
     */
    @Transactional
    public MessageDTO releaseProjectBlock(String opalCode, Integer sbid, DateTime releasedDate)
            throws ResourceNotFoundException, BadRequestException
    {
        Observation observation = findObservation(sbid);

        if (!observation.isDeposited())
        {
            String message = "Observation with sbid " + sbid + " has not finished depositing";
            logger.error(CasdaLogMessageBuilderFactory.getCasdaMessageBuilder(LogEvent.UNKNOWN_EVENT).add(message)
                    .toString());
            throw new ResourceNotFoundException(message);
        }

        boolean hasDataProducts = false;

        for (Catalogue cat : observation.getCatalogues())
        {
            if (opalCode.equals(cat.getProject().getOpalCode()))
            {
                if (cat.getQualityLevel() == QualityLevel.NOT_VALIDATED)
                {
                    throw new BadRequestException(String.format(
                            "Not all data products are validated for project block with sbid %d and project code %s",
                            sbid, opalCode));
                }
                if (cat.getReleasedDate() == null)
                {
                    cat.setReleasedDate(releasedDate);
                    // update the catalogue rows
    
                    String query = "UPDATE casda." + cat.getCatalogueType().name().toLowerCase()
                            + " SET released_date=?, last_modified=? WHERE catalogue_id=? and released_date is NULL";
    
                    // converting released date and last modified to Date because it is a recognised
                    // sql types.
                    int response =
                            jdbcTemplate.update(query, releasedDate.toDate(), DateTime.now(DateTimeZone.UTC).toDate(),
                                    cat.getId());
    
                    logger.info("Updated released date for {} catalogue rows in {} for catalogue id {} to {}", response,
                            cat.getCatalogueType().name().toLowerCase(), cat.getId(), releasedDate);
                }

                hasDataProducts = true;
            }
        }
        
        for (MeasurementSet measurementSet : observation.getMeasurementSets())
        {
            if (opalCode.equals(measurementSet.getProject().getOpalCode()))
            {
                if (measurementSet.getQualityLevel() == QualityLevel.NOT_VALIDATED)
                {
                    throw new BadRequestException(String.format(
                            "Not all data products are validated for project block with sbid %d and project code %s",
                            sbid, opalCode));
                }
                if (measurementSet.getReleasedDate() == null)
                {
                    measurementSet.setReleasedDate(releasedDate);
                }
                hasDataProducts = true;
            }
        }

        for (ImageCube imageCube : observation.getImageCubes())
        {
            if (opalCode.equals(imageCube.getProject().getOpalCode()))
            {
                if (imageCube.getQualityLevel() == QualityLevel.NOT_VALIDATED)
                {
                    throw new BadRequestException(String.format(
                            "Not all data products are validated for project block with sbid %d and project code %s",
                            sbid, opalCode));
                }
                if (imageCube.getReleasedDate() == null)
                {
                    imageCube.setReleasedDate(releasedDate);
                }
                hasDataProducts = true;
            }
        }


        for (Spectrum spectrum : observation.getSpectra())
        {
            if (opalCode.equals(spectrum.getProject().getOpalCode()))
            {
                if (spectrum.getQualityLevel() == QualityLevel.NOT_VALIDATED)
                {
                    throw new BadRequestException(String.format(
                            "Not all data products are validated for project block with sbid %d and project code %s",
                            sbid, opalCode));
                }
                if (spectrum.getReleasedDate() == null)
                {
                    spectrum.setReleasedDate(releasedDate);
                }
                hasDataProducts = true;
            }
        }


        for (MomentMap momentMap : observation.getMomentMaps())
        {
            if (opalCode.equals(momentMap.getProject().getOpalCode()))
            {
                if (momentMap.getQualityLevel() == QualityLevel.NOT_VALIDATED)
                {
                    throw new BadRequestException(String.format(
                            "Not all data products are validated for project block with sbid %d and project code %s",
                            sbid, opalCode));
                }
                if (momentMap.getReleasedDate() == null)
                {
                    momentMap.setReleasedDate(releasedDate);
                }
                hasDataProducts = true;
            }
        }

        for (Cubelet cubelet : observation.getCubelets())
        {
            if (opalCode.equals(cubelet.getProject().getOpalCode()))
            {
                if (cubelet.getQualityLevel() == QualityLevel.NOT_VALIDATED)
                {
                    throw new BadRequestException(String.format(
                            "Not all data products are validated for project block with sbid %d and project code %s",
                            sbid, opalCode));
                }
                if (cubelet.getReleasedDate() == null)
                {
                	cubelet.setReleasedDate(releasedDate);
                }
                hasDataProducts = true;
            }
        }
        
        for (EvaluationFile ef : observation.getEvaluationFiles())
        {
            if (opalCode.equals(ef.getProject().getOpalCode()))
            {
                if (ef.getReleasedDate() == null)
                {
                    ef.setReleasedDate(releasedDate);
                }
                hasDataProducts = true;
            }
        }
        
        if (!hasDataProducts)
        {

            String message =
                    String.format("Could not find a project block with sbid %d and opal code %s", sbid, opalCode);
            logger.error(CasdaLogMessageBuilderFactory.getCasdaMessageBuilder(LogEvent.UNKNOWN_EVENT).add(message)
                    .toString());
            throw new ResourceNotFoundException(message);
        }

        try
        {
            observationRepository.save(observation);
            String message =
                    CasdaLogMessageBuilderFactory.getCasdaMessageBuilder(DepositManagerEvents.E089).add(sbid)
                            .add(opalCode).toString();
            logger.info(message);
            return new MessageDTO(MessageCode.SUCCESS, message);
        }
        catch (Exception e)
        {
            String message =
                    CasdaLogMessageBuilderFactory.getCasdaMessageBuilder(DepositManagerEvents.E086).add(sbid)
                            .add(opalCode).toString();
            logger.error(message);
            throw new ServerException(message, e);
        }
    }

    /**
     * Retrieve a list of observations in the supplied state.
     * 
     * @param depositStateType
     *            of DepositState.Type
     * @return List of Observations of that Type
     */
    public List<Observation> findObservationsByDepositStateType(DepositState.Type depositStateType)
    {
        return findObservationsByDepositStateType(EnumSet.of(depositStateType));
    }

    /**
     * Retrieve a list of observations in the supplied states.
     * 
     * @param depositStateTypes
     *            Set of DepositState.Type
     * @return List of Observations matching the types
     */
    public List<Observation> findObservationsByDepositStateType(EnumSet<DepositState.Type> depositStateTypes)
    {
        return observationRepository.findObservationsByDepositStateType(depositStateTypes);
    }

    /**
     * Retrieve a list of observations recently completed (e.g. in the last 2 days).
     * 
     * @return List of observations recently completed.
     */
    public List<Observation> findRecentlyCompletedObservations()
    {
        DateTime recentCutoff = DateTime.now(DateTimeZone.UTC).minusHours(maxAgeOfRecentCompletedJobs);
        return observationRepository.findObservationsCompletedSince(recentCutoff);
    }

    /**
     * Retrieve a list of observations recently failed (e.g. in the last 3 months).
     * 
     * @param recentCutoff
     *            The earliest failed date to be included.
     * @return List of observations recently failed.
     */
    public List<Observation> findObservationsFailedSince(DateTime recentCutoff)
    {
        return observationRepository.findObservationsFailedSince(recentCutoff);
    }

    /**
     * Returns a list of quality flags that are currently active.
     * 
     * @return list of active quality flags
     */
    public List<QualityFlagDTO> getActiveQualityFlags()
    {
        List<QualityFlag> flags = qualityService.findOrderedActiveQualityFlags();
        return flags.stream().map(flag -> new QualityFlagDTO(flag)).collect(Collectors.toList());
    }

    /**
     * Returns information about the data products for a given observation and project combination.
     * 
     * @param sbid
     *            the observation's scheduling block id
     * @param opalCode
     *            the project code
     * @return the data product details
     * @throws ResourceNotFoundException
     *             if there is no observation and project combination
     */
    @Transactional
    public ObservationProjectDataProductsDTO getObservationProjectDataProducts(Integer sbid, String opalCode)
            throws ResourceNotFoundException
    {
        Observation observation = findObservation(sbid);
        Project project = findProject(observation, opalCode);
        List<String> qualityFlagCodes =
                qualityService.getQualityFlagCodesForObservationProject(observation.getId(), project.getId());
        List<ValidationNote> validationNotes =
                validationNoteRepository
                        .findBySbidAndProjectIdOrderByCreatedAsc(observation.getSbid(), project.getId());
        return new ObservationProjectDataProductsDTO(opalCode, observation, qualityFlagCodes, validationNotes);
    }

    /**
     * Updates quality information for data products and project scheduling block (observation-project combination).
     * This will update the quality level for measurement sets,image cubes, catalogues and individual catalogue rows. It
     * will also update the quality flags at the scheduling block level (observation-project) in the
     * ObservationProjectQualityFlag table
     * 
     * NOTE this does not update validation notes - these are treated as read only.
     * 
     * @param details
     *            the observation-project and data product details for update
     * @throws ResourceNotFoundException
     *             if there is no matching observation-project record
     */
    @Transactional
    public void updateObservationProjectDataProducts(ObservationProjectDataProductsDTO details)
            throws ResourceNotFoundException
    {
        Observation observation = findObservation(details.getSbid());
        Project project = findProject(observation, details.getOpalCode());

        // update the quality level
        if (CollectionUtils.isNotEmpty(details.getDataProducts()))
        {
            updateQualityLevelMatchingDataProduct(observation, details.getDataProducts());
        }

        if (CollectionUtils.isNotEmpty(details.getQualityFlagCodes()))
        {
            // update the quality flags (replace any if existing)
            qualityService.updateQualityFlagsForObservationProject(observation.getId(), project.getId(),
                    details.getQualityFlagCodes());
        }
    }

    /**
     * Finds and updates the quality level for the matching data products in the observation. This will persist the
     * quality level at data product level and at the catalogue row level.
     * 
     * @param observation
     *            the observation, this holds all the data products that need updating
     * @param dataProducts
     *            the data product details, these contains the id, type and quality level to update
     * @throws ResourceNotFoundException
     */
    private void updateQualityLevelMatchingDataProduct(Observation observation, List<DataProductDTO> dataProducts)
            throws ResourceNotFoundException
    {
        for (DataProductDTO dataProduct : dataProducts)
        {
            CasdaDataProductEntity depositable = null;
            switch (dataProduct.getType())
            {
            case CATALOGUE:
                Optional<Catalogue> catalogue =
                        observation.getCatalogues().stream().filter(cat -> cat.getId() == dataProduct.getId())
                                .findFirst();
                if (catalogue.isPresent())
                {
                    depositable = catalogue.get();
                    // update the catalogue rows
                    qualityService.updateQualityLevelForCatalogueRows(dataProduct);
                }
                break;
            case IMAGE_CUBE:
                Optional<ImageCube> imageCube =
                        observation.getImageCubes().stream().filter(ic -> ic.getId() == dataProduct.getId())
                                .findFirst();
                if (imageCube.isPresent())
                {
                    depositable = imageCube.get();
                }
                break;
            case SPECTRUM:
                Optional<Spectrum> spectrum =
                        observation.getSpectra().stream().filter(ic -> ic.getId() == dataProduct.getId()).findFirst();
                if (spectrum.isPresent())
                {
                    depositable = spectrum.get();
                }
                break;
            case MOMENT_MAP:
                Optional<MomentMap> momentMap =
                        observation.getMomentMaps().stream().filter(ic -> ic.getId() == dataProduct.getId()).findFirst();
                if (momentMap.isPresent())
                {
                    depositable = momentMap.get();
                }
                break;
            case CUBELET:
                Optional<Cubelet> cubelet =
                        observation.getCubelets().stream().filter(ic -> ic.getId() == dataProduct.getId()).findFirst();
                if (cubelet.isPresent())
                {
                    depositable = cubelet.get();
                }
                break;
            case MEASUREMENT_SET:
                Optional<MeasurementSet> measurementSet =
                        observation.getMeasurementSets().stream().filter(ms -> ms.getId() == dataProduct.getId())
                                .findFirst();
                if (measurementSet.isPresent())
                {
                    depositable = measurementSet.get();
                }
                break;
            default:
                throw new IllegalArgumentException("Data product type " + dataProduct.getType() + " is not supported");
            }

            if (depositable == null)
            {
                throw new ResourceNotFoundException("Couldn't find " + dataProduct.getType() + " matching id "
                        + dataProduct.getId() + " for sbid " + observation.getSbid());
            }
            if (dataProduct.getQualityLevel() != null)
            {
                depositable.setQualityLevel(QualityLevel.valueOf(dataProduct.getQualityLevel().name()));
            }
        }

        observationRepository.save(observation);
    }

    /**
     * Adds the validation note to the
     * 
     * @param sbid
     *            observation's scheduling block id
     * @param opalCode
     *            the opal code for the project
     * @param validationNoteDto
     *            the validation note details
     * @return the new validation note
     * @throws ResourceNotFoundException
     *             if there is no matching scheduling block
     */
    public ValidationNoteDTO addValidationNote(Integer sbid, String opalCode, ValidationNoteDTO validationNoteDto)
            throws ResourceNotFoundException
    {
        Observation observation = findObservation(sbid);
        Project project = findProject(observation, opalCode);

        ValidationNote validationNote = new ValidationNote();
        validationNote.setContent(validationNoteDto.getContent());
        if (validationNoteDto.getCreatedDate() == null)
        {
            validationNote.setCreated(DateTime.now(DateTimeZone.UTC));
        }
        else
        {
            validationNote.setCreated(new DateTime(validationNoteDto.getCreatedDate(), DateTimeZone.UTC));
        }
        validationNote.setPersonId(validationNoteDto.getUserId());
        validationNote.setPersonName(validationNoteDto.getUserName());
        validationNote.setProject(project);
        validationNote.setSbid(sbid);
        validationNote = validationNoteRepository.save(validationNote);
        return new ValidationNoteDTO(validationNote);
    }

    
    /**
     * Scheduled job which regenerates the catalogue HiPS for each catalogue type. 
     */
    @Scheduled(cron = "${catalogue.regen.period}")
    public void regenerateCatalogueHips()
    {
        logger.info("Started regenerating catalogue HiPS");
        
        String[] catalogueTypes = new String[] { "continuum_component", "continuum_island", "polarisation_component",
                "spectral_line_absorption", "spectral_line_emission" };
        
        for (String catType : catalogueTypes)
        {
            logger.info("Regenerating HiPS for " + catType + " ...");
            String jobId = String.format("Genhips-%s", catType);
            List<String> commandParts = new ArrayList<>();
            commandParts.addAll(Arrays.asList(Utils.elStringToArray(buildCatalogueHipsCommandAndArgs)));
            SimpleToolProcessJobBuilder processBuilder =
                    new SimpleToolProcessJobBuilder(this.processJobFactory, commandParts.toArray(new String[] {}));
            processBuilder.setProcessParameter("catalogueType", catType);

            processBuilder.setWorkingDirectory(depositToolsWorkingDirectory);

            // Run extract job inline
            ProcessJob job = processBuilder.createJob(jobId, "Catalogue_hips");
            SingleJobMonitor monitor = singleJobMonitorFactory.createSingleJobMonitor();
            
            job.run(monitor);
            if (monitor.isJobFailed())
            {
                logger.error("Failed to regenerate HiPS for " + catType + ": " + monitor.getJobOutput());
            }
        }

        logger.info("Finished regenerating catalogue HiPS.");
    }
    
    private Observation findObservation(Integer sbid) throws ResourceNotFoundException
    {
        Observation observation = observationRepository.findBySbid(sbid);
        if (observation == null)
        {
            throw new ResourceNotFoundException(String.format("Could not find Observation %d", sbid));
        }
        return observation;
    }

    private Project findProject(Observation observation, String opalCode) throws ResourceNotFoundException
    {
        Optional<Project> project =
                observation.getProjects().stream().filter(proj -> opalCode.equals(proj.getOpalCode())).findFirst();
        if (!project.isPresent())
        {
            throw new ResourceNotFoundException(String.format(
                    "Observation %d does not have any data products for project %s", observation.getSbid(), opalCode));
        }
        return project.get();
    }

    /**
     * @return A sorted copy of the list of invalid observations
     */
    public synchronized List<String> getInvalidObservationIds()
    {
        List<String> sortedList = new ArrayList<>(invalidObsIds);
        Collections.sort(sortedList);
        return sortedList;
    }

    /**
     * Add an entry to the list of invalid observations.
     * @param sbid The scheduling block id of the invalid observation.xml
     */
    public synchronized void addInvalidObservation(String sbid)
    {
        if (!invalidObsIds.contains(sbid))
        {
            invalidObsIds.add(sbid);
        }
    }

    /**
     * Reset the list of invalid observations to be empty.
     */
    public synchronized void clearInvalidObservationIds()
    {
        invalidObsIds.clear();
    }

}
