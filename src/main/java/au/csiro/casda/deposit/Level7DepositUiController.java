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


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import au.csiro.casda.ResourceNotFoundException;
import au.csiro.casda.datadeposit.ChildDepositableArtefact;
import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.deposit.jpa.Level7CollectionRepository;
import au.csiro.casda.deposit.services.Level7CollectionService;
import au.csiro.casda.entity.observation.Level7Collection;

/**
 * UI Controller for the Deposit Manager application.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
@Controller
public class Level7DepositUiController extends ParentDepositableController 
{
    private static final class Level7CollectionIdComparator
    implements Comparator<Level7Collection>
    {
        /** {@inheritDoc} */
        @Override
        public int compare(Level7Collection o1, Level7Collection o2)
        {
            if (o1 == o2)
            {
                return 0;
            }
            if (o1 == null)
            {
                return -1;
            }
            if (o2 == null)
            {
                return +1;
            }
            // sort descending
            return Long.valueOf(o2.getDapCollectionId()).compareTo(Long.valueOf(o1.getDapCollectionId()));
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(Level7DepositUiController.class);

    /**
     * MVC Model key for single level 7 collection.
     */
    static final String LEVEL7_COLLECTION_MODEL_KEY = "level7Collection";
    /**
     * Constant for deposit status page
     */
    static final String LEVEL7_DEPOSIT_STATUS_PAGE = "level7DepositStatus";

    /**
     * Constant for level 7 collection show page
     */
    static final String LEVEL7_SHOW_PAGE = "level7/show";

    private static final Level7CollectionIdComparator LEVEL7_COLLECTION_COMPARATOR = new Level7CollectionIdComparator();

    @Autowired
    private Level7CollectionService level7CollectionService;

    @Autowired
    private Level7CollectionRepository level7CollectionRepository;

    /**
     * Return the path for showing a particular level 7 collection
     * 
     * @param collectionId
     *            the level 7 collection's collection ID
     * @return the path
     */
    static String getPathForShowLevel7Collection(Long collectionId)
    {
        return "/level_7_deposits/" + collectionId;
    }

    /**
     * A page showing a list of data deposit jobs that are failed.
     * 
     * @param request 
     *            the http servlet request object needed for access to the URL
     * @param model
     *            the web app model
     * @return an exception
     */
    @RequestMapping(method = RequestMethod.GET, value = "/level_7_deposits")
    public String level7CollectionDepositStatus(HttpServletRequest request, Model model)
    {
        logger.info("Deposit manager status page for level 7 collections requested.");

        String url = request.getRequestURL().toString();
        if(url.endsWith("/"))
        {
            url = url.substring(0, url.length()-1);
        }
        
        List<Level7Collection> failedLevel7Collections = level7CollectionService.
        	findLevel7CollectionsByDepositStateType(EnumSet.of(DepositState.Type.FAILED, DepositState.Type.INVALID));
        failedLevel7Collections.sort(LEVEL7_COLLECTION_COMPARATOR);
        model.addAttribute(FAILED_PARENT_DEPOSITABLES_MODEL_KEY, failedLevel7Collections);

        List<Level7Collection> activeLevel7Collections =
                level7CollectionService.findLevel7CollectionsByDepositStateType(
                        EnumSet.of(DepositState.Type.STAGING, DepositState.Type.DEPOSITING, DepositState.Type.ARCHIVING,
                                DepositState.Type.CLEANUP, DepositState.Type.NOTIFYING));
        activeLevel7Collections.sort(LEVEL7_COLLECTION_COMPARATOR);
        model.addAttribute(DEPOSITING_PARENT_DEPOSITABLES_MODEL_KEY, activeLevel7Collections);

        Map<Long, List<ChildDepositableArtefact>> failedLevel7CollectionDepositablesMap = new HashMap<>();

        for (List<Level7Collection> level7Collections : Arrays.asList(activeLevel7Collections, failedLevel7Collections))
        {
            for (Level7Collection level7Collection : level7Collections)
            {
                List<ChildDepositableArtefact> failureList = new ArrayList<>();
                for (ChildDepositableArtefact depositable : level7Collection.getDepositableArtefacts())
                {
                    if (depositable.isFailedDeposit())
                    {
                        failureList.add(depositable);
                    }
                }
                if (!failureList.isEmpty())
                {
                    failureList.sort(DEPOSITABLE_ARTEFACT_COMPARATOR);
                    failedLevel7CollectionDepositablesMap.put(level7Collection.getDapCollectionId(), failureList);
                }
            }
        }
        model.addAttribute(FAILED_DEPOSITABLES_MODEL_KEY, failedLevel7CollectionDepositablesMap);

        List<Level7Collection> completedLevel7Collections =
                level7CollectionService.findRecentlyCompletedLevel7Collections();
        completedLevel7Collections.sort(LEVEL7_COLLECTION_COMPARATOR);
        model.addAttribute(DEPOSITED_PARENT_DEPOSITABLES_MODEL_KEY, completedLevel7Collections);
        
        List<Level7Collection> validatingLevel7Collections =
                level7CollectionService.findValidatingLevel7Collections();
        validatingLevel7Collections.sort(LEVEL7_COLLECTION_COMPARATOR);
        model.addAttribute(VALIDATING_PARENT_DEPOSITABLES_MODEL_KEY, validatingLevel7Collections);

        model.addAttribute(
        		DEPOSITED_PARENT_DEPOSITABLES_MAX_AGE_MODEL_KEY,
                DurationFormatUtils.formatDurationWords(
                        TimeUnit.HOURS.toMillis((long) getMaxAgeOfRecentCompletedJobs()), true, true));

        model.addAttribute(DEPOSIT_STATUS_URL, url);

        return LEVEL7_DEPOSIT_STATUS_PAGE;
    }

    /**
     * Spring MVC Controller method to show the Level 7 Collection with the given collection id.
     * 
     * @param model
     *            a Spring MVC Model
     * @param collectionId
     *            the collection id of the level 7 collection to show
     * @return the name of the view to show
     * @throws ResourceNotFoundException
     *             if a Level 7 Collection with the given collection ID could not be found
     */
    @RequestMapping(method = RequestMethod.GET, value = "/level_7_deposits/{collectionId}")
    public String showLevel7Collection(Model model, @PathVariable() Long collectionId) throws ResourceNotFoundException
    {
        logger.info("Deposit manager level 7 collection {} detail page requested.", collectionId);
        
        Level7Collection level7Collection = getLevel7CollectionForCollectionId(collectionId);
        level7Collection.setDepositStateFactory(getDepositStateFactory());
        model.addAttribute(LEVEL7_COLLECTION_MODEL_KEY, level7Collection);
        
        List<ChildDepositableArtefact> depositableArtefacts =
                new ArrayList<>(level7Collection.getDepositableArtefacts());
        Map<String, DepositableStatusSummary> depositableSummariesByStatus = new LinkedHashMap<>();
        
        boolean artefactFailed = parseChildArtefacts(depositableArtefacts, depositableSummariesByStatus);
        
        model.addAttribute(ARTIFACT_FAILED_MODEL_KEY, artefactFailed);
        depositableArtefacts.sort(DEPOSITABLE_ARTEFACT_COMPARATOR);
        model.addAttribute(DEPOSITABLE_ARTEFACTS_MODEL_KEY, depositableArtefacts);
        model.addAttribute(ARTEFACT_SUMMARIES_MODEL_KEY, depositableSummariesByStatus.values());
        return LEVEL7_SHOW_PAGE;
    }
  
    private Level7Collection getLevel7CollectionForCollectionId(Long dapCollectionId) throws ResourceNotFoundException
    {
        Level7Collection level7Collection = level7CollectionRepository.findByDapCollectionId(dapCollectionId);
        if (level7Collection == null)
        {
            throw new ResourceNotFoundException("No level 7 collection with collection id '" + dapCollectionId + "'");
        }

        return level7Collection;
    }
}
