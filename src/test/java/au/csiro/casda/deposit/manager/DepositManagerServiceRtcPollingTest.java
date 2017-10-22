package au.csiro.casda.deposit.manager;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

import java.time.ZonedDateTime;

import org.apache.logging.log4j.Level;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import au.csiro.casda.deposit.DepositManagerEvents;
import au.csiro.casda.deposit.Log4JTestAppender;
import au.csiro.casda.deposit.exception.ImportException;
import au.csiro.casda.deposit.exception.PollingException;
import au.csiro.casda.deposit.jpa.ObservationRepository;

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
 * Test the timer that causes an Event to be logged every x hours (currently 1).
 * 
 * Copyright 2014, CSIRO Australia. All rights reserved.
 * 
 */
public class DepositManagerServiceRtcPollingTest
{
    private Log4JTestAppender testAppender;

    @Mock
    private ObservationsJobsHandler observationsJobsHandler;

    @Mock
    private ObservationRefreshHandler observationRefreshHandler;

    @Mock
    private ObservationRepository observationRepository;

    @Mock
    private CasdaDepositStatusProgressMonitor casdaDepositStatusProgressMonitor;

    @Mock
    private ObservationDepositProgressor progressor;

    private static final String DATA_DEPOSIT_TEST_ROOTDIR = "DATA_DEPOSIT_TEST_ROOTDIR";

    @Before
    public void setUp() throws Exception
    {
        testAppender = Log4JTestAppender.createAppender();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testPollFailsAndTimeoutLogs() throws PollingException, IllegalArgumentException,
            IllegalAccessException, ImportException, InterruptedException
    {
        long timeExpiresValue = 500L;

        DepositManagerService depositManagerService = spy(new DepositManagerService(progressor, observationRepository,
                observationsJobsHandler, observationRefreshHandler, casdaDepositStatusProgressMonitor,
                DATA_DEPOSIT_TEST_ROOTDIR, timeExpiresValue));

        Exception theException = new PollingException("Mock ImportException");
        doThrow(theException).when(observationsJobsHandler).run(DATA_DEPOSIT_TEST_ROOTDIR);

        // the last successful poll is set on start up
        ZonedDateTime lastSuccessfulPoll = depositManagerService.getLastSuccessfulRtcPoll();

        // poll the rtc first time
        depositManagerService.pollRtc();

        // wait
        Thread.sleep(timeExpiresValue);

        // poll the rtc second time
        depositManagerService.pollRtc();

        // poll the rtc third time
        depositManagerService.pollRtc();

        // wait
        Thread.sleep(timeExpiresValue);

        // poll the rtc fourth time
        depositManagerService.pollRtc();

        // make sure that it keeps the value of the last successful poll after unsuccessful polls
        assertEquals(lastSuccessfulPoll, depositManagerService.getLastSuccessfulRtcPoll());

        // the first poll the we expect a E013 because it hasn't timed out
        testAppender.verifyLogMessage(Level.ERROR, DepositManagerEvents.E013.messageBuilder().toString(),
                PollingException.class, "");
        // the second poll, it has timed out, so we expect an E013 and and E014
        testAppender.verifyLogMessage(Level.ERROR, DepositManagerEvents.E013.messageBuilder().toString(),
                PollingException.class, "");
        testAppender.verifyLogMessage(Level.ERROR, DepositManagerEvents.E014.messageBuilder().toString());
        // the third poll, it has timed out, but a notification was sent recently so expect only E013
        testAppender.verifyLogMessage(Level.ERROR, DepositManagerEvents.E013.messageBuilder().toString(),
                PollingException.class, "");
        // the fourth poll, it has timed out, and last notification was sent outside that expiry period, so expect E013
        // and E014
        testAppender.verifyLogMessage(Level.ERROR, DepositManagerEvents.E013.messageBuilder().toString(),
                PollingException.class, "");
        testAppender.verifyLogMessage(Level.ERROR, DepositManagerEvents.E014.messageBuilder().toString());
    }

    @Test
    public void testPollRtcSuccessfulLogging() throws Exception
    {
        DepositManagerService depositManagerService = spy(new DepositManagerService(progressor, observationRepository,
                observationsJobsHandler, observationRefreshHandler, casdaDepositStatusProgressMonitor,
                DATA_DEPOSIT_TEST_ROOTDIR, 20000L));

        depositManagerService.pollRtc();

        testAppender.verifyLogMessage(Level.INFO, DepositManagerEvents.E072.messageBuilder().toString());

    }

}
