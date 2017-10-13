package au.csiro.casda.deposit.jobqueue;

import au.csiro.casda.jobmanager.JobManager.Job;
import au.csiro.casda.jobmanager.JobManager.JobMonitor;

/**
 * Dummy Job implementation.
 * <p>
 * Copyright 2017, CSIRO Australia. All rights reserved.
 */
public class TestJob implements Job
{
    private String jobId;
    private String jobType;

    public TestJob(String jobId, String jobType)
    {
        this.jobId = jobId;
        this.jobType = jobType;
        
    }
    @Override
    public String getId()
    {
        return jobId;
    }

    @Override
    public String getType()
    {
        return jobType;
    }

    @Override
    public void run(JobMonitor monitor)
    {
        // Do nothing
        
    }
    
}