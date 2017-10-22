package au.csiro.casda.deposit.manager;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.deposit.DepositManagerEvents;
import au.csiro.casda.deposit.exception.ImportException;
import au.csiro.casda.deposit.exception.PollingException;
import au.csiro.casda.deposit.jpa.ObservationRepository;
import au.csiro.casda.entity.observation.Observation;
import au.csiro.casda.logging.CasdaLogMessageBuilderFactory;
import au.csiro.casda.logging.LogEvent;

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
 * DepositManagerService monitors a directory for new observations and progresses any new observations.
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class DepositManagerService
{
    private static final Logger logger = LoggerFactory.getLogger(DepositManagerService.class);

    private final String depositObservationParentDirectory;

    private ObservationDepositProgressor progressor;
    private ObservationRepository observationRepository;
    private ObservationsJobsHandler observationsJobsHandler;
    private ObservationRefreshHandler observationRefreshHandler;
    private CasdaDepositStatusProgressMonitor casdaDepositStatusProgressMonitor;

    private ZonedDateTime lastSuccessfulPollTime;
    private ZonedDateTime failedPollNotificationSentTime = null;

    private long rtcPollFailureNotificationThresholdMillis;

    /**
     * Constructor. Also starts the polling timeout timer.
     * 
     * @param progressor
     *            the obervation deposit progressor
     * @param observationRepository
     *            the observation repository
     * @param observationsJobsHandler
     *            the obervation jobs handler
     * @param observationRefreshHandler
     *            the handler for observation refreshes.
     * @param casdaDepositStatusProgressMonitor
     *            the casda deposit status progress monitor
     * @param depositObservationParentDirectory
     *            the parent directory location where observations are deposited
     * @param rtcPollFailThresholdMs
     *            failure notification will be sent if a successful poll hasn't occurred in this number of millis
     */
    @Autowired
    public DepositManagerService(ObservationDepositProgressor progressor, ObservationRepository observationRepository,
            ObservationsJobsHandler observationsJobsHandler,
            ObservationRefreshHandler observationRefreshHandler,
            CasdaDepositStatusProgressMonitor casdaDepositStatusProgressMonitor,
            @Value("${deposit.observation.parent.directory}") String depositObservationParentDirectory,
            @Value("${deposit.rtc.poll.failure.notification.threshold.millis}") long rtcPollFailThresholdMs)
    {
        this.progressor = progressor;
        this.observationRepository = observationRepository;
        this.observationsJobsHandler = observationsJobsHandler;
        this.observationRefreshHandler = observationRefreshHandler;
        this.casdaDepositStatusProgressMonitor = casdaDepositStatusProgressMonitor;
        this.depositObservationParentDirectory = depositObservationParentDirectory;
        this.rtcPollFailureNotificationThresholdMillis = rtcPollFailThresholdMs;
        this.lastSuccessfulPollTime = ZonedDateTime.now(ZoneId.of("UTC"));
    }

    /**
     * Polls the status of observations, to make sure they have completed within a reasonable time.
     */
    @Scheduled(fixedRateString = "${deposit.status.poll.period.millis}")
    public synchronized void pollDepositProgress()
    {
        logger.debug("Polling deposit progress");
        casdaDepositStatusProgressMonitor.checkDepositableStatuses(System.currentTimeMillis());
    }

    /**
     * Polls the RTC for new observations to deposit.
     */
    @Scheduled(fixedRateString = "${deposit.rtc.poll.period.millis}")
    public synchronized void pollRtc()
    {
        logger.debug("Polling rtc");
        try
        {
            observationsJobsHandler.run(depositObservationParentDirectory);
            lastSuccessfulPollTime = ZonedDateTime.now(ZoneId.of("UTC"));
            // Log successful poll
            logger.info(DepositManagerEvents.E072.messageBuilder().toString());
        }
        catch (PollingException e)
        {
            logger.error(DepositManagerEvents.E013.messageBuilder().toString(), e);

            if (isTimeToNotifyFailedPoll())
            {
                logger.error(DepositManagerEvents.E014.messageBuilder().toString());
                failedPollNotificationSentTime = ZonedDateTime.now(ZoneId.of("UTC"));
            }
        }
    }

    /**
     * Redeposit the observation by processing the updated observation XML file.
     * @param sbid The scheduling block id of the observation.
     * @return True if the observation could be redeposited, false if there was an error.
     */
    public boolean redepositObservation(int sbid)
    {
        boolean result = false;
        try
        {
            observationsJobsHandler.redepositObservation(depositObservationParentDirectory, sbid);
            result = true;
        }
        catch (ImportException e)
        {
            logger.error(DepositManagerEvents.E073.messageBuilder().add(e.getSbid()).toString(), e);
        }
        
        return result;
    }
    
    /**
     * Progresses any non-DEPOSITED Observations (excluding archived as they will be handled by a less frequent poll).
     */
    @Scheduled(fixedDelayString = "${deposit.workflow.progression.delay.millis}")
    public void progressNonArchivingObservations()
    {
        //a collection containing all types except deposited, archiving and failed
        EnumSet<DepositState.Type> typeList = EnumSet.allOf((DepositState.Type.class));
        typeList.removeAll
                (Arrays.asList(DepositState.Type.DEPOSITED, DepositState.Type.ARCHIVING, DepositState.Type.FAILED));

        progressObservations(typeList);
    }
    
    /**
     * Progresses any ARCHIVING Observations.
     */
    @Scheduled(fixedDelayString = "${deposit.archiving.workflow.progression.delay.millis}")
    public void progressArchivingObservations()
    {
        //a list containing only archiving type
        EnumSet<DepositState.Type> typeList = EnumSet.of(DepositState.Type.ARCHIVING);

        progressObservations(typeList);
    }
    
    private void progressObservations(EnumSet<DepositState.Type> typeList)
    {
        logger.debug("Progressing observations");

        List<Observation> observations = 
                observationRepository.findDepositingObservationsForDepositStateTypeOrdered(typeList);
        if (CollectionUtils.isNotEmpty(observations))
        {
            logger.debug("{}", "-------------------------------------------------------");
        }
        for (Observation observation : observations)
        {
            logger.debug("{}", "Progressing Observation " + observation.getSbid().toString());

            long startTime = (new Date()).getTime();
            try
            {
                progressor.progressObservation(observation.getSbid());
            }
            catch (ObjectOptimisticLockingFailureException e)
            {
                // CASDA's Data Deposit application might update the details in an artefact while we are progressing the
                // observation, which will cause an optimistic locking failure (CASDA-4440). We log the event, and
                // continue because this will be retried automatically the next time this scheduled method is run.
                logger.warn(
                        CasdaLogMessageBuilderFactory
                                .getCasdaMessageBuilder(LogEvent.UNKNOWN_EVENT)
                                .addCustomMessage(
                                        "Rolling back, observation modified by another process "
                                                + observation.getSbid()).toString(), e);
            }
            long endTime = (new Date()).getTime();
            logger.info("Progressed observation " + observation.getSbid() + " in " + (endTime - startTime) + " ms.");

            Observation changedObservation = observationRepository.findBySbid(observation.getSbid());

            logger.debug("{}", String.format("Observation %d now %s", changedObservation.getSbid(), changedObservation
                    .getDepositStateType().toString()));
            logger.debug("{}", "-------------------------------------------------------");
        }
    }
    
    /**
     * Progresses any active refresh jobs.
     */
    @Scheduled(fixedDelayString = "${deposit.workflow.refresh.delay.millis}")
    public void progressRefreshObservations()
    {
        long startTime = (new Date()).getTime();
        boolean anyActive = observationRefreshHandler.progressRefreshObservations();
        if (anyActive)
        {
            long endTime = (new Date()).getTime();
            logger.info("Progressed refresh in " + (endTime - startTime) + " ms.");
        }
    }

    private boolean isTimeToNotifyFailedPoll()
    {
        return // notify if it's been too long since the last successful rtc poll AND
        tooLongSinceLastSuccessfulRtcPoll()
        // either no notification has been sent yet
                && (failedPollNotificationSentTime == null ||
                // or the last time notification was sent was too long ago
                durationBetweenTimeAndNow(failedPollNotificationSentTime) > rtcPollFailureNotificationThresholdMillis);
    }

    /**
     * Checks whether it has been too long since the last successful poll
     * 
     * @return true if the last successful poll was more than rtcPollFailureNotificationThresholdMillis ago
     */
    public boolean tooLongSinceLastSuccessfulRtcPoll()
    {
        return durationBetweenTimeAndNow(lastSuccessfulPollTime) > rtcPollFailureNotificationThresholdMillis;
    }

    private long durationBetweenTimeAndNow(ZonedDateTime time)
    {
        return Duration.between(time, ZonedDateTime.now(ZoneId.of("UTC"))).toMillis();
    }

    /**
     * Gets the time of the last successful RTC poll
     * 
     * @return time of the last successful RTC poll, with UTC timezone.
     */
    public ZonedDateTime getLastSuccessfulRtcPoll()
    {
        return lastSuccessfulPollTime;
    }

    public String getDepositObservationParentDirectory()
    {
        return depositObservationParentDirectory;
    }

}
