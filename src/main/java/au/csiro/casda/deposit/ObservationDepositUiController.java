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


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.view.RedirectView;

import au.csiro.casda.ResourceNotFoundException;
import au.csiro.casda.datadeposit.ChildDepositableArtefact;
import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositState.IllegalEventException;
import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.deposit.exception.ArtefactInvalidStateRecoveryException;
import au.csiro.casda.deposit.exception.ArtefactNotFoundException;
import au.csiro.casda.deposit.exception.ObservationNotFailedRecoveryException;
import au.csiro.casda.deposit.exception.ObservationNotFoundException;
import au.csiro.casda.deposit.jpa.ObservationRepository;
import au.csiro.casda.deposit.manager.DepositManagerService;
import au.csiro.casda.deposit.services.ObservationDepositRecoveryService;
import au.csiro.casda.deposit.services.ObservationService;
import au.csiro.casda.entity.observation.ChildDepositableArtefactComparator;
import au.csiro.casda.entity.observation.Observation;

/**
 * UI Controller for the Deposit Manager application.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
@Controller
public class ObservationDepositUiController
{
    private static final class ObservationSbidComparator implements Comparator<Observation>
    {
        /** {@inheritDoc} */
        @Override
        public int compare(Observation o1, Observation o2)
        {
            if (o1 == o2)
            {
                return 0;
            }
            if (o1 == null || o1.getSbid() == null)
            {
                return -1;
            }
            if (o2 == null || o2.getSbid() == null)
            {
                return +1;
            }
            return o2.getSbid().compareTo(o1.getSbid()); // Sort descending
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(ObservationDepositUiController.class);

    /**
     * MVC Model key for failed Observations.
     */
    static final String FAILED_OBSERVATIONS_MODEL_KEY = "failedObservations";

    /**
     * MVC Model key for depositing Observations.
     */
    static final String DEPOSITING_OBSERVATIONS_MODEL_KEY = "activeObservations";

    /**
     * MVC Model key for deposited Observations.
     */
    static final String DEPOSITED_OBSERVATIONS_MODEL_KEY = "completedObservations";

    /**
     * MVC Model key for failed observation depositables.
     */
    static final String FAILED_OBSERVATION_DEPOSITABLES_MODEL_KEY = "obsWithFailures";

    /**
     * MVC Model key for maximum age of recently completed observations
     */
    static final String DEPOSITED_OBSERVATIONS_MAX_AGE_MODEL_KEY = "depositedObservationsMaximumAge";

    /**
     * MVC Model key for single observation.
     */
    static final String OBSERVATION_MODEL_KEY = "observation";

    /**
     * MVC Model key for observation depositable artefacts.
     */
    static final String OBSERVATION_DEPOSITABLE_ARTEFACTS_MODEL_KEY = "depositableArtefacts";

    /**
     * Constant for deposit status page
     */
    static final String OBSERVATION_DEPOSIT_STATUS_PAGE = "observationDepositStatus";

    /**
     * Constant for observation show page
     */
    static final String OBSERVATION_SHOW_PAGE = "observation/show";

    private static final ObservationSbidComparator OBSERVATION_COMPARATOR = new ObservationSbidComparator();

    private static final ChildDepositableArtefactComparator DEPOSITABLE_ARTEFACT_COMPARATOR =
            new ChildDepositableArtefactComparator();

    @Autowired
    private ObservationService observationService;

    @Autowired
    private ObservationDepositRecoveryService recoveryService;

    @Autowired
    private ObservationRepository observationRepository;

    @Autowired
    private DepositStateFactory depositStateFactory;

    @Autowired
    private FlashHelper flashHelper;

    @Autowired
    private DepositManagerService depositManagerService;

    @Autowired
    @Value("${job.manager.recent.age.hours}")
    private int maxAgeOfRecentCompletedJobs;

    /**
     * Return the path for showing a particular observation
     * 
     * @param sbid
     *            the Observation SBID
     * @return the path
     */
    static String getPathForShowObservation(Integer sbid)
    {
        return "/level_5_deposits/" + sbid;
    }

    /**
     * A page showing a list of data deposit jobs that are failed.
     * 
     * @param model
     *            the web app model
     * @return an exception
     */
    @RequestMapping(method = RequestMethod.GET, value = "/level_5_deposits")
    public String observationDepositStatus(Model model)
    {
        logger.info("Deposit manager status page for observations requested.");

        List<Observation> failedObservations =
                observationService.findObservationsByDepositStateType(DepositState.Type.FAILED);
        failedObservations.sort(OBSERVATION_COMPARATOR);
        model.addAttribute(FAILED_OBSERVATIONS_MODEL_KEY, failedObservations);

        List<Observation> activeObservations =
                observationService.findObservationsByDepositStateType(EnumSet.of(DepositState.Type.STAGING,
                        DepositState.Type.DEPOSITING, DepositState.Type.NOTIFYING));
        activeObservations.sort(OBSERVATION_COMPARATOR);
        model.addAttribute(DEPOSITING_OBSERVATIONS_MODEL_KEY, activeObservations);

        Map<Integer, List<ChildDepositableArtefact>> failedObservationDepositablesMap = new HashMap<>();

        for (List<Observation> observations : Arrays.asList(activeObservations, failedObservations))
        {
            for (Observation observation : observations)
            {
                List<ChildDepositableArtefact> failureList = new ArrayList<>();
                for (ChildDepositableArtefact depositable : observation.getDepositableArtefacts())
                {
                    if (depositable.isFailedDeposit())
                    {
                        failureList.add(depositable);
                    }
                }
                if (!failureList.isEmpty())
                {
                    failureList.sort(DEPOSITABLE_ARTEFACT_COMPARATOR);
                    failedObservationDepositablesMap.put(observation.getSbid(), failureList);
                }
            }
        }
        model.addAttribute(FAILED_OBSERVATION_DEPOSITABLES_MODEL_KEY, failedObservationDepositablesMap);

        List<Observation> completedObservations = observationService.findRecentlyCompletedObservations();
        completedObservations.sort(OBSERVATION_COMPARATOR);
        model.addAttribute(DEPOSITED_OBSERVATIONS_MODEL_KEY, completedObservations);

        model.addAttribute(
                DEPOSITED_OBSERVATIONS_MAX_AGE_MODEL_KEY,
                DurationFormatUtils.formatDurationWords(
                        TimeUnit.HOURS.toMillis((long) this.maxAgeOfRecentCompletedJobs), true, true));

        return OBSERVATION_DEPOSIT_STATUS_PAGE;
    }

    /**
     * Spring MVC Controller method to show the Observation with the given sbid.
     * 
     * @param model
     *            a Spring MVC Model
     * @param sbid
     *            the sbid of the observation to show
     * @return the name of the view to show
     * @throws ResourceNotFoundException
     *             if an Observation with the give scheduling block ID could not be found
     */
    @RequestMapping(method = RequestMethod.GET, value = "/level_5_deposits/{sbid}")
    public String showObservation(Model model, @PathVariable() Integer sbid) throws ResourceNotFoundException
    {
        logger.info("Deposit manager observation {} detail page requested.", sbid);

        Observation observation = getObservationForSbid(sbid);
        observation.setDepositStateFactory(depositStateFactory);
        model.addAttribute(OBSERVATION_MODEL_KEY, observation);
        List<ChildDepositableArtefact> depositableArtefacts = new ArrayList<>(observation.getDepositableArtefacts());
        depositableArtefacts.sort(DEPOSITABLE_ARTEFACT_COMPARATOR);
        model.addAttribute(OBSERVATION_DEPOSITABLE_ARTEFACTS_MODEL_KEY, depositableArtefacts);
        return OBSERVATION_SHOW_PAGE;
    }

    /**
     * Spring MVC Controller method to recover the failed Observation with the given sbid.
     * 
     * @param request
     *            the HttpServletRequest
     * @param sbid
     *            the sbid of the observation to recover
     * @return the name of the view to show
     * @throws ResourceNotFoundException
     *             if an Observation with the give scheduling block ID could not be found
     */
    @RequestMapping(method = RequestMethod.POST, value = "/level_5_deposits/{sbid}/recover")
    public RedirectView recoverDeposit(HttpServletRequest request, @PathVariable() Integer sbid)
            throws ResourceNotFoundException
    {
        logger.info("Deposit manager observation {} requested to resume deposit.", sbid);

        // make sure the observation exists because we will redirect to it later
        getObservationForSbid(sbid);

        boolean failed = true;
        try
        {
            recoveryService.recoverObservation(sbid);
            failed = false;
            logger.info(DepositManagerEvents.E097.messageBuilder().add(sbid).toString());
        }
        catch (IllegalEventException e)
        {
            logger.error(DepositManagerEvents.E096.messageBuilder().add(sbid).toString());
        }
        catch (ObservationNotFailedRecoveryException e)
        {
            logger.error(DepositManagerEvents.E094.messageBuilder().add(sbid).toString());
        }
        catch (ObservationNotFoundException e)
        {
            logger.error(DepositManagerEvents.E099.messageBuilder().add(sbid).toString());
        }

        if (failed)
        {
            flashHelper.flash(request, "error", "Request to resume deposit failed.");
        }
        else
        {
            flashHelper.flash(request, "success", "Deposit resumed.");
        }

        return new RedirectView(getPathForShowObservation(sbid), true);
    }

    /**
     * Spring MVC Controller method to recover the failed artefact with the give file id for an observation with the
     * given sbid.
     * 
     * @param request
     *            the HttpServletRequest
     * @param sbid
     *            the sbid of the artefact's parent observation
     * @param fileId
     *            the artefact's file id
     * @return the name of the view to show
     * @throws ResourceNotFoundException
     *             if an Observation with the given scheduling block ID could not be found
     */
    @RequestMapping(method = RequestMethod.POST, value = "/level_5_deposits/{sbid}/artefacts/{fileId}/recover")
    public RedirectView recoverDeposit(HttpServletRequest request, @PathVariable() Integer sbid,
            @PathVariable() String fileId) throws ResourceNotFoundException
    {
        logger.info("Deposit manager observation {} artefact {} requested to resume deposit.", sbid, fileId);

        boolean failed = true;
        try
        {
            recoveryService.recoverArtefact(sbid, fileId);
            failed = false;
            logger.info(DepositManagerEvents.E138.messageBuilder().add(fileId).add(sbid).toString());
        }
        catch (IllegalEventException e)
        {
            logger.error(DepositManagerEvents.E137.messageBuilder().add(fileId).add(sbid).toString());
        }
        catch (ArtefactInvalidStateRecoveryException | ArtefactNotFoundException e)
        {
            logger.error(DepositManagerEvents.E136.messageBuilder().add(fileId).add(sbid).toString(), e);
        }
        catch (ObservationNotFoundException e)
        {
            logger.error(DepositManagerEvents.E099.messageBuilder().add(sbid).toString());
        }

        if (failed)
        {
            flashHelper.flash(request, "error", "Request to resume deposit of artefact " + fileId + " failed.");
        }
        else
        {
            flashHelper.flash(request, "success", "Deposit of artefact " + fileId + " resumed.");
        }

        return new RedirectView(getPathForShowObservation(sbid), true);
    }

    /**
     * Perform a manual poll on the deposit manager service
     * 
     * @param request
     *            the http request object
     * @return A redirect pack to the parent page
     */
    @RequestMapping(method = RequestMethod.POST, value = "/level_5_deposits", params = "manualPoll")
    public RedirectView observationDepositStatusManualPoll(HttpServletRequest request)
    {
        depositManagerService.pollRtc();

        flashHelper.flash(request, "success", "Initiated poll for new observation at: "
                + LocalDateTime.now().toString());

        return new RedirectView("/level_5_deposits", true);
    }

    private Observation getObservationForSbid(Integer sbid) throws ResourceNotFoundException
    {
        Observation observation = observationRepository.findBySbid(sbid);
        if (observation == null)
        {
            throw new ResourceNotFoundException("No observation with SBID '" + sbid + "'");
        }

        return observation;
    }

}
