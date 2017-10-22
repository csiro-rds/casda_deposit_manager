package au.csiro.casda.deposit;

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


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import au.csiro.casda.ResourceNotFoundException;
import au.csiro.casda.datadeposit.ChildDepositableArtefact;
import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositState.IllegalEventException;
import au.csiro.casda.deposit.exception.ArtefactInvalidStateRecoveryException;
import au.csiro.casda.deposit.exception.ArtefactNotFoundException;
import au.csiro.casda.deposit.exception.ObservationNotFailedRecoveryException;
import au.csiro.casda.deposit.exception.ObservationNotFoundException;
import au.csiro.casda.deposit.jpa.ObservationRepository;
import au.csiro.casda.deposit.manager.DepositManagerService;
import au.csiro.casda.deposit.manager.ObservationRefreshHandler;
import au.csiro.casda.deposit.services.ObservationDepositRecoveryService;
import au.csiro.casda.deposit.services.ObservationService;
import au.csiro.casda.entity.observation.Observation;
import au.csiro.casda.entity.refresh.ObservationRefresh;
import au.csiro.casda.entity.refresh.RefreshJob;
import au.csiro.casda.entity.refresh.RefreshStateType;

/**
 * UI Controller for the Deposit Manager application.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
@Controller
public class ObservationDepositUiController extends ParentDepositableController 
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

    private static final class ObservationRefreshComparator implements Comparator<ObservationRefresh>
    {
        /** {@inheritDoc} */
        @Override
        public int compare(ObservationRefresh o1, ObservationRefresh o2)
        {
            if (o1 == o2)
            {
                return 0;
            }
            if (o1 == null || o1.getRefreshState() == null)
            {
                return -1;
            }
            if (o2 == null || o2.getRefreshState() == null)
            {
                return +1;
            }
            if (o1.getRefreshState() != o2.getRefreshState())
            {
                return o2.getRefreshState().compareTo(o1.getRefreshState()); // Sort descending
            }
            if (o1.getRefreshStateChanged() == null)
            {
                return -1;
            }
            if (o2.getRefreshStateChanged() == null)
            {
                return +1;
            }
            return o1.getRefreshStateChanged().compareTo(o2.getRefreshStateChanged()); 
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(ObservationDepositUiController.class);

    /**
     * MVC Model key for invalid Observations (i.e. unparsable observation.xml files).
     */
    static final String INVALID_OBSERVATIONS_MODEL_KEY = "invalidObservations";

    /**
     * MVC Model key for maximum age of recently failed observations
     */
    static final String FAILED_OBSERVATIONS_MAX_AGE_MODEL_KEY = "failedObservationsMaximumAge";

    /**
     * MVC Model key for maximum age of recently completed refreshes
     */
    static final String COMPLETED_REFRESH_MAX_AGE_MODEL_KEY = "completedRefreshMaximumAge";

    /**
     * MVC Model key for maximum age of recently failed refreshes
     */
    static final String FAILED_REFRESH_MAX_AGE_MODEL_KEY = "failedRefreshesMaximumAge";

        
    /**
     * MVC Model key for single observation.
     */
    static final String OBSERVATION_MODEL_KEY = "observation";

    /**
     * Constant for deposit status page
     */
    static final String OBSERVATION_DEPOSIT_STATUS_PAGE = "observationDepositStatus";

    /**
     * Constant for the refresh status page
     */
    static final String OBSERVATION_REFRESH_STATUS_PAGE = "observationRefreshStatus";

    /**
     * Constant for observation show page
     */
    static final String OBSERVATION_SHOW_PAGE = "observation/show";

    /** MVC Model key for the refreshing observations list. */
    static final String OBSERVATION_REFRESH_MODEL_KEY = "observations";

    /** MVC Model key for the list of refreshing observations that failed. */
    static final String FAILED_OBSERVATION_REFRESH_MODEL_KEY = "failedObservations";
    
    /** MVC Model key for the number of refreshed observations. */
    static final String NUM_COMPLETED_OBSERVATIONS_MODEL_KEY = "numCompletedObservations";

    /** MVC Model key for the number of queued refresh tasks. */
    static final String NUM_QUWUED_REFRESH_TASKS_MODEL_KEY = "numQueuedTasks";

    
    /** MVC Model key for the active refresh job list. */
    static final String ACTIVE_REFRESH_JOBS_MODEL_KEY = "activeRefreshJobs";

    /** MVC Model key for the completed refresh jobs list. */
    static final String COMPLETED_REFRESH_JOBS_MODEL_KEY = "completedRefreshJobs";
    
    private static final ObservationSbidComparator OBSERVATION_COMPARATOR = new ObservationSbidComparator();
    private static final ObservationRefreshComparator OBSERVATION_REFRESH_COMPARATOR = new ObservationRefreshComparator();
    

    @Autowired
    private ObservationService observationService;

    @Autowired
    private ObservationDepositRecoveryService recoveryService;

    @Autowired
    private ObservationRepository observationRepository;

    @Autowired
    private FlashHelper flashHelper;

    @Autowired
    private DepositManagerService depositManagerService;

    @Autowired
    private ObservationRefreshHandler observationRefreshHandler;

    @Autowired
    @Value("${job.manager.recent.failed.age.days}")
    private int numDaysFailedJobs;

    @Autowired
    @Value("${job.manager.recent.refresh.age.days}")
    private int numDaysRefreshJobs;

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
     * A page showing a list of the data deposit jobs that are active, completed or failed.
     * 
     * @param model
     *            the web app model
     * @param request 
     *            the http servlet request object needed for access to the URL
     * @return The view to be displayed
     */
    @RequestMapping(method = RequestMethod.GET, value = "/level_5_deposits")
    public String observationDepositStatus(HttpServletRequest request, Model model)
    {
        logger.info("Deposit manager status page for observations requested.");

        String url = request.getRequestURL().toString();
        if(url.endsWith("/"))
        {
            url = url.substring(0, url.length()-1);
        }
        
        List<String> invalidObsIds =
                observationService.getInvalidObservationIds();
        model.addAttribute(INVALID_OBSERVATIONS_MODEL_KEY, invalidObsIds);
        
        DateTime recentCutoff = DateTime.now(DateTimeZone.UTC).minusDays(numDaysFailedJobs);
        List<Observation> failedObservations = observationService.findObservationsFailedSince(recentCutoff);
        failedObservations.sort(OBSERVATION_COMPARATOR);
        
        model.addAttribute(FAILED_PARENT_DEPOSITABLES_MODEL_KEY, failedObservations);

        List<Observation> activeObservations = observationService.findObservationsByDepositStateType(EnumSet.of(
        		DepositState.Type.STAGING, 
				DepositState.Type.PRIORITY_DEPOSITING, 
				DepositState.Type.DEPOSITING, 
                DepositState.Type.ARCHIVING, 
				DepositState.Type.NOTIFYING));
        
        activeObservations.sort(OBSERVATION_COMPARATOR);
        model.addAttribute(DEPOSITING_PARENT_DEPOSITABLES_MODEL_KEY, activeObservations);

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
        model.addAttribute(FAILED_DEPOSITABLES_MODEL_KEY, failedObservationDepositablesMap);

        List<Observation> completedObservations = observationService.findRecentlyCompletedObservations();
        completedObservations.sort(OBSERVATION_COMPARATOR);
        model.addAttribute(DEPOSITED_PARENT_DEPOSITABLES_MODEL_KEY, completedObservations);

        model.addAttribute(
        		DEPOSITED_PARENT_DEPOSITABLES_MAX_AGE_MODEL_KEY,
                DurationFormatUtils.formatDurationWords(
                        TimeUnit.HOURS.toMillis((long) getMaxAgeOfRecentCompletedJobs()), true, true));

        model.addAttribute(
                FAILED_OBSERVATIONS_MAX_AGE_MODEL_KEY,
                DurationFormatUtils.formatDurationWords(
                        TimeUnit.DAYS.toMillis((long) numDaysFailedJobs), true, true));

        model.addAttribute(DEPOSIT_STATUS_URL, url);
        
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
        observation.setDepositStateFactory(getDepositStateFactory());
        model.addAttribute(OBSERVATION_MODEL_KEY, observation);
        List<ChildDepositableArtefact> depositableArtefacts = new ArrayList<>(observation.getDepositableArtefacts());
        depositableArtefacts.sort(DEPOSITABLE_ARTEFACT_COMPARATOR);
        
        Map<String, DepositableStatusSummary> depositableSummariesByStatus = new LinkedHashMap<>();
        
        boolean artefactFailed = parseChildArtefacts(depositableArtefacts, depositableSummariesByStatus);

        model.addAttribute(ARTIFACT_FAILED_MODEL_KEY, artefactFailed);
        model.addAttribute(DEPOSITABLE_ARTEFACTS_MODEL_KEY, depositableArtefacts);
        model.addAttribute(ARTEFACT_SUMMARIES_MODEL_KEY, depositableSummariesByStatus.values());
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
    @RequestMapping(method = RequestMethod.POST, value = "/level_5_deposits/{sbid}/recover/all")
    public RedirectView recoverAllArtefacts(HttpServletRequest request, @PathVariable() Integer sbid)
            throws ResourceNotFoundException
    {
        logger.info("Deposit manager observation {} requested to resume deposit.", sbid);

        // make sure the observation exists because we will redirect to it later
        Set<ChildDepositableArtefact> failedArtefacts = getObservationForSbid(sbid).getFailedDepositableArtefacts();
        int failed = 0;
        
        for(ChildDepositableArtefact artefact : failedArtefacts)
        {
        	if(!recoverArtefact(sbid, artefact.getFileId()))
        	{
        		failed++;
        	}
        }

        if (failed > 0)
        {
            flashHelper.flash(request, "error", "Request to resume deposit of all artefacts resulted in " 
            		+ failed + " failures.");
        }
        else
        {
            flashHelper.flash(request, "success", "Deposit of all artefacts resumed.");
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

        boolean succeeded = recoverArtefact(sbid, fileId);

        if (succeeded)
        {
        	flashHelper.flash(request, "success", "Deposit of artefact " + fileId + " resumed.");
        }
        else
        {
            flashHelper.flash(request, "error", "Request to resume deposit of artefact " + fileId + " failed.");
        }

        return new RedirectView(getPathForShowObservation(sbid), true);
    }
    
    private boolean recoverArtefact(Integer sbid, String fileId)
    {
    	boolean succeed = false;
        try
        {
            recoveryService.recoverArtefact(sbid, fileId);
            succeed = true;
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
        return succeed;
    }

    /**
     * Spring MVC Controller method to redeposit a completed observation, for instance when extra artefacts are added.
     * 
     * @param request
     *            the HttpServletRequest
     * @param sbid
     *            the sbid of the observation
     * @return the name of the view to show
     * @throws ResourceNotFoundException
     *             if an Observation with the given scheduling block ID could not be found
     */
    @RequestMapping(method = RequestMethod.POST, value = "/level_5_deposits/{sbid}/redeposit")
    public RedirectView redeposit(HttpServletRequest request, @PathVariable() Integer sbid)
            throws ResourceNotFoundException
    {
        logger.info("Deposit manager observation {} requested to redeposit.", sbid);

        Observation observation = getObservationForSbid(sbid);
        if (!observation.isDeposited())
        {
            flashHelper.flash(request, "error", "Observation must be already deposited.");
            return new RedirectView(getPathForShowObservation(sbid), true);
        }

        boolean result = depositManagerService.redepositObservation(sbid);

        if (result)
        {
            logger.info(DepositManagerEvents.E153.messageBuilder().add(sbid).toString());
            flashHelper.flash(request, "success", "Observation redeposit started.");
        }
        else
        {
            logger.error(DepositManagerEvents.E152.messageBuilder().add(sbid).toString());
            flashHelper.flash(request, "error", "Request to redeposit observation failed.");
        }

        return new RedirectView(getPathForShowObservation(sbid), true);
    }

    /**
     * Spring MVC Controller method to refrehs the metadata of a completed observation, for instance when the metadata 
     * extraction rules have changed.
     * 
     * @param request
     *            the HttpServletRequest
     * @param sbid
     *            the sbid of the observation
     * @return the name of the view to show
     * @throws ResourceNotFoundException
     *             if an Observation with the given scheduling block ID could not be found
     */
    @RequestMapping(method = RequestMethod.POST, value = "/level_5_deposits/{sbid}/refresh")
    public RedirectView refreshObservation(HttpServletRequest request, @PathVariable() Integer sbid)
            throws ResourceNotFoundException
    {
        logger.info("Deposit manager observation {} requested to refresh metadata.", sbid);

        Observation observation = getObservationForSbid(sbid);
        if (!observation.isDeposited())
        {
            flashHelper.flash(request, "error", "Observation must be already deposited.");
            return new RedirectView(getPathForShowObservation(sbid), true);
        }

        boolean result = observationRefreshHandler.refreshObservation(sbid);

        if (result)
        {
            logger.info(DepositManagerEvents.E155.messageBuilder().add(sbid).toString());
            flashHelper.flash(request, "success", "Observation metadata refresh started.");
        }
        else
        {
            logger.error(DepositManagerEvents.E154.messageBuilder().add(sbid).toString());
            flashHelper.flash(request, "error", "Request to refresh observation metadata failed.");
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

    /**
     * Search for an observation by sbid and if found redirect to the page
     * 
     * @param request
     *            the http request object
     * @param sbid
     *            the sbid of the observation
     * @return A redirect to the observation page, or the parent page if not found
     */
    @RequestMapping(method = RequestMethod.POST, value = "/level_5_deposits", params = "search")
    public RedirectView observationDepositStatusSearch(HttpServletRequest request, @RequestParam Integer sbid)
    {
        if (sbid == null)
        {
            flashHelper.flash(request, "error", "Please provide an observation id  " + sbid + ".");
            return new RedirectView("/level_5_deposits", true);
        }

        Observation observation = observationRepository.findBySbid(sbid);
        if (observation == null)
        {
            flashHelper.flash(request, "error", "No observation exists with the id  " + sbid + ".");
            return new RedirectView("/level_5_deposits", true);
        }

        return new RedirectView("/level_5_deposits/"+sbid, true);
    }

    
    /**
     * A page showing a list of the observation refreah jobs that are active or completed.
     * 
     * @param model
     *            the web app model
     * @param request 
     *            the http servlet request object needed for access to the URL
     * @return The view to be displayed
     */
    @RequestMapping(method = RequestMethod.GET, value = "/level_5_refreshes")
    public String observationRefreshStatus(HttpServletRequest request, Model model)
    {
        logger.info("Deposit manager observation refresh status page requested.");

        String url = request.getRequestURL().toString();
        if (url.endsWith("/"))
        {
            url = url.substring(0, url.length() - 1);
        }

        // List of observations being refreshed
        List<ObservationRefresh> displayList = new ArrayList<>();
        List<ObservationRefresh> refreshList = observationRefreshHandler.getActiveRefreshes();
        int unrefreshedCount = 0;
        final int maxUnrefreshed = 10;
        for (ObservationRefresh observationRefresh : refreshList)
        {
            if (observationRefresh.getRefreshState() != RefreshStateType.UNREFRESHED)
            {
                displayList.add(observationRefresh);
            }
            else
            {
                unrefreshedCount++;
                if (unrefreshedCount < maxUnrefreshed)
                {
                    displayList.add(observationRefresh);
                }
            }
        }
        unrefreshedCount = unrefreshedCount < maxUnrefreshed ? 0 : unrefreshedCount - maxUnrefreshed;
        displayList.sort(OBSERVATION_REFRESH_COMPARATOR);
        model.addAttribute(OBSERVATION_REFRESH_MODEL_KEY, displayList);
        model.addAttribute(NUM_QUWUED_REFRESH_TASKS_MODEL_KEY, unrefreshedCount);

        // Counts of observations completed
        DateTime completedCutoff = DateTime.now(DateTimeZone.UTC).minusDays(1);
        int completedCount = observationRefreshHandler.countCompletedRefreshesSince(completedCutoff);
        model.addAttribute(NUM_COMPLETED_OBSERVATIONS_MODEL_KEY, completedCount);

        // List of failed observation refreshes in the last x days
        DateTime failedCutoff = DateTime.now(DateTimeZone.UTC).minusDays(numDaysFailedJobs);
        List<ObservationRefresh> failedRefreshes = observationRefreshHandler.getFailedRefreshesSince(failedCutoff);
        model.addAttribute(FAILED_OBSERVATION_REFRESH_MODEL_KEY, failedRefreshes);

        model.addAttribute(
                FAILED_REFRESH_MAX_AGE_MODEL_KEY,
                DurationFormatUtils.formatDurationWords(
                        TimeUnit.DAYS.toMillis((long) numDaysFailedJobs), true, true));

        // List of active refresh jobs
        List<RefreshJob> activeJobs = observationRefreshHandler.getUncompletedRefreshJobs();
        model.addAttribute(ACTIVE_REFRESH_JOBS_MODEL_KEY, activeJobs);

        // List of refresh jobs in the last x days
        DateTime recentCutoff = DateTime.now(DateTimeZone.UTC).minusDays(numDaysRefreshJobs);
        List<RefreshJob> completedJobs = observationRefreshHandler.findRefreshJobsCompletedSince(recentCutoff);
        model.addAttribute(COMPLETED_REFRESH_JOBS_MODEL_KEY, completedJobs);        

        model.addAttribute(
                COMPLETED_REFRESH_MAX_AGE_MODEL_KEY,
                DurationFormatUtils.formatDurationWords(
                        TimeUnit.DAYS.toMillis((long) numDaysRefreshJobs), true, true));

        model.addAttribute(DEPOSIT_STATUS_URL, url);
        
        return OBSERVATION_REFRESH_STATUS_PAGE;
    }

    /**
     * Initiate a refresh of the metadata for all deposited observations.
     * 
     * @param request
     *            the http request object
     * @return A redirect pack to the parent page
     */
    @RequestMapping(method = RequestMethod.POST, value = "/level_5_refreshes", params = "refreshAll")
    public RedirectView observationRefreshStatusRefeshAll(HttpServletRequest request)
    {
        observationRefreshHandler.refreshAllObservations();

        logger.info(DepositManagerEvents.E156.messageBuilder().toString());
        
        flashHelper.flash(request, "success", "Observation metadata refresh commenced at "
                + LocalDateTime.now().toString());

        return new RedirectView("/level_5_refreshes", true);
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
