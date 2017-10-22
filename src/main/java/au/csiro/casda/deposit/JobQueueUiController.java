package au.csiro.casda.deposit;

import java.io.IOException;
import java.time.LocalDateTime;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import au.csiro.casda.deposit.jobqueue.QueuePoller;
import au.csiro.casda.deposit.jobqueue.QueuedJob;
import au.csiro.casda.jobmanager.JobManager;
import au.csiro.casda.jobmanager.JobManager.JobStatus;
import au.csiro.casda.logging.CasdaLogMessageBuilderFactory;
import au.csiro.casda.logging.CasdaMessageBuilder;
import au.csiro.casda.logging.LogEvent;

/**
 * UI Controller for the Job Queue page
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
@Controller
public class JobQueueUiController
{

    private static final Logger logger = LoggerFactory.getLogger(JobQueueUiController.class);

    /**
     * Constant for deposit status page
     */
    static final String JOB_QUEUE_PAGE = "jobQueue";

    /** Constant for the map of queued jobs */
    static final String QUEUED_JOB_MAP_KEY = "queuedJobMap";

    /** Constant for the list of job types being managed */
    static final String JOB_TYPE_LIST_KEY = "jobTypeList";

    /** Constant for the map of number of jobs allowed for each job type */
    static final String JOB_TYPE_MAX_JOBS_KEY = "jobTypeMaxJobsMap";

    /** Constant for the maximum number of jobs in limited queued */
    static final String MAX_JOBS_KEY = "maxJobs";

    /** Constant for the maximum number of jobs in limited queued */
    static final String NUM_JOBS_KEY = "numJobs";

    /** Constant for the requested URL */
    protected static final String URL = "url";

    /** Constant for the indicating if the queues are paused */
    static final String PAUSED = "paused";
    
    
    @Autowired
    private FlashHelper flashHelper;

    @Autowired
    private JobManager jobManager;

    /**
     * A page showing a list of the queued deposit batch jobs.
     * 
     * @param request
     *            the http servlet request object needed for access to the URL
     * @param model
     *            the web app model
     * @return an exception
     */
    @RequestMapping(method = RequestMethod.GET, value = "/jobs")
    public String queuedJobStatus(HttpServletRequest request, Model model)
    {
        logger.info("Deposit manager job queue page requested.");

        String url = request.getRequestURL().toString();
        if (url.endsWith("/"))
        {
            url = url.substring(0, url.length() - 1);
        }

        Map<String, List<QueuedJob>> queuedJobMap = new HashMap<>();
        Map<String, Integer> maxJobsMap = new HashMap<>();
        int maxJobs = 0;
        int numRunningJobs = 0;
        boolean queuesPaused = false;
        if (QueuePoller.class.isAssignableFrom(jobManager.getClass()))
        {
            QueuePoller queueManager = (QueuePoller) jobManager;
            queuedJobMap = queueManager.getQueuedJobMap();
            queuesPaused = queueManager.isQueuesPaused();
            for (String jobType : queuedJobMap.keySet())
            {
                int numAllowedJobs = queueManager.getNumAllowedJobs(jobType);
                maxJobsMap.put(jobType, numAllowedJobs);
                if (numAllowedJobs > 0)
                {
                    maxJobs += numAllowedJobs;
                }
            }
        }

        Map<String, QueueSummary> queueSummaryMap = buildQueueSummaryMap(queuedJobMap, maxJobsMap);
        for (Entry<String, QueueSummary> entry : queueSummaryMap.entrySet())
        {
            for (QueuedJob job : entry.getValue().getDisplayJobs())
            {
                if (job.getStatus().isRunning())
                {
                    numRunningJobs++;
                }
            }
        }

        model.addAttribute(QUEUED_JOB_MAP_KEY, queueSummaryMap);

        List<String> jobTypeList = new ArrayList<>(queuedJobMap.keySet());
        Collections.sort(jobTypeList);
        model.addAttribute(JOB_TYPE_LIST_KEY, jobTypeList);

        model.addAttribute(JOB_TYPE_MAX_JOBS_KEY, maxJobsMap);
        model.addAttribute(NUM_JOBS_KEY, numRunningJobs);
        model.addAttribute(MAX_JOBS_KEY, maxJobs);
        model.addAttribute(URL, url);
        model.addAttribute(PAUSED, queuesPaused);
        
        return JOB_QUEUE_PAGE;
    }

    private Map<String, QueueSummary> buildQueueSummaryMap(Map<String, List<QueuedJob>> queuedJobMap,
            Map<String, Integer> maxJobsMap)
    {
        final int maxQueuedJobs = 10;
        Map<String, QueueSummary> queueSummaryMap = new HashMap<>();
        for (Entry<String, List<QueuedJob>> queueEntry : queuedJobMap.entrySet())
        {
            String jobType = queueEntry.getKey();
            QueueSummary summary = new QueueSummary();
            List<QueuedJob> displayJobs = new ArrayList<>();
            int numQueuedJobs = 0;

            for (QueuedJob job : queueEntry.getValue())
            {
                JobStatus status = job.getStatus();
                if (status.isRunning() || status.isFailed())
                {
                    displayJobs.add(job);
                }
                else if (status.isReady())
                {
                    if (numQueuedJobs < maxQueuedJobs)
                    {
                        displayJobs.add(job);
                        numQueuedJobs++;
                    }
                    else
                    {
                        summary.numQueuedJobs++;
                    }
                }
                else if (status.isFinished())
                {
                    summary.numCompletedJobs++;
                }
                else
                {
                    logger.warn("Ignoring unexpected job status of " + status + " for job " + job);
                }
            }
            summary.displayJobs = displayJobs;
            summary.maxAllowedJobs = maxJobsMap.get(jobType);
            queueSummaryMap.put(jobType, summary);
        }

        return queueSummaryMap;
    }

    /**
     * Download the job output for a specified job.
     * 
     * @param response
     *            the response to stream the file data to
     * @param jobType
     *            the job type or queue that the job belongs to.
     * @param jobId
     *            The id of the target job 
     */
    @RequestMapping(method = RequestMethod.GET, value = "/jobs/{jobType}/joboutput")
    public void downloadJobOutput(HttpServletResponse response, @PathVariable String jobType,
            @RequestParam String jobId)
    {
        logger.info("Deposit manager job queue page requested.");
        response.addHeader("Content-disposition", "attachment;filename=jobOutput.txt");
        response.setContentType("text/plain");

        JobStatus jobStatus = jobManager.getJobStatus(jobId);
        if (jobStatus == null)
        {
            return;
        }
        try
        {
            response.getWriter().append(jobStatus.getJobOutput());
            response.flushBuffer();
        }
        catch (IOException e)
        {
            CasdaMessageBuilder<?> builder =
                    CasdaLogMessageBuilderFactory.getCasdaMessageBuilder(LogEvent.UNKNOWN_EVENT);
            builder.add("Could not send job status for jobid " + jobId);
            logger.error(builder.toString(), e);
        }
    }

    /**
     * Halt the commencement of new batch jobs. Jobs can still be added to the queues though.
     * 
     * @param request
     *            the http request object
     * @return A redirect pack to the parent page
     */
    @RequestMapping(method = RequestMethod.POST, value = "/jobs", params = "pause")
    public RedirectView pauseJobQueues(HttpServletRequest request)
    {
        logger.info("Pausing job manager queues.");

        if (QueuePoller.class.isAssignableFrom(jobManager.getClass()))
        {
            QueuePoller queueManager = (QueuePoller) jobManager;
            queueManager.setQueuesPaused(true);
        }

        flashHelper.flash(request, "success", "Paused the job queues at: "
                + LocalDateTime.now().toString());

        return new RedirectView("/jobs", true);
    }

    /**
     * Restart the commencement of new batch jobs. 
     * 
     * @param request
     *            the http request object
     * @return A redirect pack to the parent page
     */
    @RequestMapping(method = RequestMethod.POST, value = "/jobs", params = "unpause")
    public RedirectView unpauseJobQueues(HttpServletRequest request)
    {
        logger.info("Unpausing job manager queues.");

        if (QueuePoller.class.isAssignableFrom(jobManager.getClass()))
        {
            QueuePoller queueManager = (QueuePoller) jobManager;
            queueManager.setQueuesPaused(false);
        }

        flashHelper.flash(request, "success", "Unpaused the job queues at: "
                + LocalDateTime.now().toString());

        return new RedirectView("/jobs", true);
    }
    
    /**
     * DTO class to hold a summary of the state of a job queue. Used for displaying the queue state.
     */
    public static final class QueueSummary
    {
        private List<QueuedJob> displayJobs;

        private int numCompletedJobs;
        private int numQueuedJobs;
        private int maxAllowedJobs;

        public List<QueuedJob> getDisplayJobs()
        {
            return displayJobs;
        }

        public void setDisplayJobs(List<QueuedJob> displayJobs)
        {
            this.displayJobs = displayJobs;
        }

        public int getNumCompletedJobs()
        {
            return numCompletedJobs;
        }

        public void setNumCompletedJobs(int numCompletedJobs)
        {
            this.numCompletedJobs = numCompletedJobs;
        }

        public int getNumQueuedJobs()
        {
            return numQueuedJobs;
        }

        public void setNumQueuedJobs(int numQueuedJobs)
        {
            this.numQueuedJobs = numQueuedJobs;
        }

        public int getMaxAllowedJobs()
        {
            return maxAllowedJobs;
        }

        public void setMaxAllowedJobs(int maxAllowedJobs)
        {
            this.maxAllowedJobs = maxAllowedJobs;
        }
        
    }
}
