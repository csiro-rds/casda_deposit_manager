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

/*
 * CSIRO CASDA Deposit Manager
 * 
 * Copyright (C) 2010 - 2012 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * 
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * 
 */

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.boot.orm.jpa.EntityScan;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import au.csiro.casda.Utils;
import au.csiro.casda.deposit.jobqueue.QueuedJobManager;
import au.csiro.casda.jobmanager.AsynchronousJobManager;
import au.csiro.casda.jobmanager.CommandRunnerServiceProcessJobFactory;
import au.csiro.casda.jobmanager.JavaProcessJobFactory;
import au.csiro.casda.jobmanager.JobManager;
import au.csiro.casda.jobmanager.ProcessJobBuilder.ProcessJobFactory;
import au.csiro.casda.jobmanager.SlurmJobManager;
import au.csiro.casda.jobmanager.SynchronousProcessJobManager;
import au.csiro.casda.logging.CasdaLoggingSettings;

/**
 * Initialises spring boot application.
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
@Configuration
@EntityScan("au.csiro.casda.entity")
@EnableAutoConfiguration
@ComponentScan(basePackages = { "au.csiro.casda" })
@EnableJpaRepositories
@EnableAsync
@EnableScheduling
public class DepositManagerApplication extends SpringBootServletInitializer
{
    private static Logger logger = LoggerFactory.getLogger(DepositManagerApplication.class);

    /**
     * application name
     */
    protected static final String APPLICATION_NAME = "CasdaDepositManager";

    private static final String CONFIG_FOLDER = "config";

    @Autowired
    private ApplicationContext context;

    private Class<JobManager> unthrottledJobManagerClass;

    private HashMap<String, Integer> jobManagerThrottlingMap;

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application)
    {
        File userDir = new File(System.getProperty("user.dir"));
        File configDir = new File(userDir, CONFIG_FOLDER);

        CasdaLoggingSettings loggingSettings = new CasdaLoggingSettings(APPLICATION_NAME);

        loggingSettings.addGeneralLoggingSettings();

        logger.info("Config being read from {} and {}", configDir.getAbsolutePath(), userDir.getAbsolutePath());

        SpringApplicationBuilder app = application.sources(DepositManagerApplication.class);
        app.profiles("casda_deposit_manager");

        return app;
    }

    /**
     * Configures the throttling behaviour (how many jobs of a particular type can run concurrently) of the throttled
     * JobManager. The string must be in the format of a Spring EL list, where the elements of the list are name/value
     * pairs, eg: {&quot;stage_artefact&quot;, &quot;1&quot;, &quot;register_artefact&quot;, &quot;4&quot; }
     * 
     * @param jobManagerThrottlingConfig
     *            the configuration String
     */
    @Autowired
    @Value("${job.manager.throttled.config}")
    public void setJobManagerThrottlingConfig(String jobManagerThrottlingConfig)
    {
        Map<String, String> map = Utils.elStringToMap(jobManagerThrottlingConfig);
        this.jobManagerThrottlingMap = new HashMap<>();
        for (String key : map.keySet())
        {
            this.jobManagerThrottlingMap.put(key, Integer.parseInt(map.get(key)));
        }
    }

    /**
     * Sets the class name for the non-throttled JobManager class to be used to run Jobs.
     * 
     * @param className
     *            the name of a class that implements the JobManager interface
     */
    @SuppressWarnings("unchecked")
    @Autowired
    @Value("${job.manager.unthrottled.class.name}")
    public void setUnthrottledJobManagerClassName(String className)
    {
        try
        {
            unthrottledJobManagerClass = (Class<JobManager>) Class.forName(className);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return the JobManager to be used for observation import (required by ObservationJobHandler)
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public SynchronousProcessJobManager observationImportJobManager()
    {
        return new SynchronousProcessJobManager();
    }

    /**
     * @return the JobManager to be used for level 7 import (required by Level7JobsHandler)
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public SynchronousProcessJobManager level7ImportJobManager()
    {
        return new SynchronousProcessJobManager();
    }

    /**
     * Create a new SlurmJobManager instance for use by this application. Called by Spring on startup.
     * 
     * @param processJobFactory
     *            the factory to be used to create job processes.
     * @param slurmJobStatusSeparator
     *            the String used to separate the Slurm status elements
     * @param runningJobsCountCommandAndArgs
     *            the command and args used to find out how many jobs are running for a particular job type
     * @param jobStatusCommandAndArgs
     *            the command and args used to find out a job's status
     * @param startJobCommandAndArgsPrologue
     *            the command and args used to start a job
     * @param cancelJobCommandAndArgs
     *            the command and args used to cancel a job
     * @return A new SlurmJobManager instance.
     */
    @Bean
    public SlurmJobManager slurmJobManager(ProcessJobFactory processJobFactory,
            @Value("${slurm.job.status.separator}") String slurmJobStatusSeparator,
            @Value("${slurm.jobs.running.count.command}") String runningJobsCountCommandAndArgs,
            @Value("${slurm.job.status.command}") String jobStatusCommandAndArgs,
            @Value("${slurm.job.start.command.prologue}") String startJobCommandAndArgsPrologue,
            @Value("${slurm.job.cancel.command}") String cancelJobCommandAndArgs)
    {
        return new SlurmJobManager(processJobFactory, slurmJobStatusSeparator, runningJobsCountCommandAndArgs,
                jobStatusCommandAndArgs, startJobCommandAndArgsPrologue, cancelJobCommandAndArgs);
    }

    /**
     * Create a new AsynchronousJobManager instance for use by this application. Called by Spring on startup.
     * 
     * @return The AsynchronousJobManager instance.
     */
    @Bean
    public AsynchronousJobManager asynchronousJobManager()
    {
        return new AsynchronousJobManager();
    }

    /**
     * @return the JobManager bean to be used throughout the application. This JobManager will be throttled.
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public JobManager jobManager()
    {
        return new QueuedJobManager(getUnthrottledJobManager(), this.jobManagerThrottlingMap);
    }

    /**
     * Create an instance of the base job manager implementation as specified in the job.manager.unthrottled.class.name
     * config value. It should be wrapped in a throttling manager before use.
     * 
     * @return the unthrottled JobManager bean.
     */
    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
    public JobManager getUnthrottledJobManager()
    {
        if (unthrottledJobManagerClass.isAssignableFrom(SlurmJobManager.class))
        {
            return (JobManager) context.getBean("slurmJobManager");
        }

        return context.getAutowireCapableBeanFactory().createBean(this.unthrottledJobManagerClass);
    }

    /**
     * Return the factory which will create ProcessJob instance for us to run commands. 
     * @param cmdWebServiceUrl The URL of the command runner web service.
     * @param factoryName The name of the factory to use, if not using the default.
     * @return The ProcessJobFactory instance
     */
    @Bean
    public ProcessJobFactory getProcessJobFactory(@Value("${command.webservice.url:}") String cmdWebServiceUrl,
            @Value("${command.process.job.factory:}") String factoryName)
    {
        if ("CommandRunnerServiceProcessJobFactory".equalsIgnoreCase(factoryName))
        {
            if (StringUtils.isBlank(cmdWebServiceUrl))
            {
                throw new IllegalArgumentException(
                        "command.webservice.url must be configured to use CommandRunnerServiceProcessJobFactory");
            }
            return new CommandRunnerServiceProcessJobFactory(cmdWebServiceUrl, APPLICATION_NAME);
        }
        
        return new JavaProcessJobFactory();
    }
}
