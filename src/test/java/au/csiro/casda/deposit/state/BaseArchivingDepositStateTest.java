package au.csiro.casda.deposit.state;

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


import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import au.csiro.casda.datadeposit.ArchivedDepositState;
import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.datadeposit.FailedDepositState;
import au.csiro.casda.deposit.Log4JTestAppender;
import au.csiro.casda.deposit.SingleJobMonitorFactory;
import au.csiro.casda.deposit.services.NgasService;
import au.csiro.casda.deposit.services.NgasService.ServiceCallException;
import au.csiro.casda.deposit.services.NgasService.Status;
import au.csiro.casda.jobmanager.ProcessJobBuilder;

/**
 * Verify the functions of the CasdaArchivingDepositState class.
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
public abstract class BaseArchivingDepositStateTest
{

    protected static final String DMF_FILE_LOCATION_PARAMETER = "dmf_file_location";

    protected Log4JTestAppender log4jTestAppender;

    @Autowired
    @Value("${archivingDepositStateTest.failing.command}")
    protected String failingCommand;

    @Autowired
    @Value("${archivingDepositStateTest.failing.unknown.command}")
    protected String failingUnknownCommand;

    @Autowired
    @Value("${archivingDepositStateTest.success.ofl.command}")
    protected String successOflCommand;

    @Autowired
    @Value("${archivingDepositStateTest.success.dul.command}")
    protected String successDulCommand;

    @Autowired
    @Value("${archivingDepositStateTest.success.reg.command}")
    protected String successRegCommand;

    @Autowired
    @Value("${archivingDepositStateTest.success.mig.command}")
    protected String successMigCommand;

    @Mock
    protected NgasService ngasService;

    @Mock
    protected Status status;

    @Mock
    protected DepositStateFactory stateFactory;

    protected SingleJobMonitorFactory singleJobMonitorFactory = new SingleJobMonitorFactory();

    @Mock
    protected ProcessJobBuilder archiveStatusBuilder;

    @Mock
    protected ProcessJobBuilder archivePutBuilder;

    protected String expectedFilename;

    @Before
    public void setup() throws Exception
    {
        log4jTestAppender = Log4JTestAppender.createAppender();
        MockitoAnnotations.initMocks(this);
        when(stateFactory.createState(eq(DepositState.Type.ARCHIVED), any())).thenReturn(
                new ArchivedDepositState(stateFactory, null));
        when(stateFactory.createState(eq(DepositState.Type.FAILED), any())).thenReturn(
                new FailedDepositState(stateFactory, null));

        // unique identifier attribute used to access ngas status for artifact
        when(ngasService.getStatus(getTestFileId())).thenReturn(status);
        when(status.getFileName()).thenReturn("bob.xml");
        when(status.getMountPoint()).thenReturn("/here");

        expectedFilename = "/here" + IOUtils.DIR_SEPARATOR + "bob.xml";
    }

    @Test
    public void testCommandError()
    {

        CasdaArchivingDepositState state = getState(failingCommand);

        state.progress();
        assertThat(state.getDepositable().getDepositState().getType(), is(DepositState.Type.FAILED));
    }

    @Test
    public void testCommandReturnUnknown()
    {

        CasdaArchivingDepositState state = getState(failingUnknownCommand);

        state.progress();
        assertThat(state.getDepositable().getDepositState().getType(), is(DepositState.Type.FAILED));
        verify(archiveStatusBuilder).setProcessParameter(DMF_FILE_LOCATION_PARAMETER, expectedFilename);
    }

    @Test
    public void testCommandReturnOFL()
    {

        CasdaArchivingDepositState state = getState(successOflCommand);

        state.progress();
        assertThat(state.getDepositable().getDepositState().getType(), is(DepositState.Type.ARCHIVED));
        verify(archiveStatusBuilder).setProcessParameter(DMF_FILE_LOCATION_PARAMETER, expectedFilename);
    }

    @Test
    public void testCommandReturnDUL()
    {

        CasdaArchivingDepositState state = getState(successDulCommand);

        state.progress();
        assertThat(state.getDepositable().getDepositState().getType(), is(DepositState.Type.ARCHIVED));
        verify(archiveStatusBuilder).setProcessParameter(DMF_FILE_LOCATION_PARAMETER, expectedFilename);
    }

    @Test
    public void testCommandReturnREGDualPutOK()
    {
        // in progress - will also request dual status same command is fine for tests.
        CasdaArchivingDepositState state = getState(successRegCommand, successRegCommand);

        state.progress();
        assertThat(state.getDepositable().getDepositState().getType(), is(DepositState.Type.ARCHIVING));
    }

    @Test
    public void testCommandReturnREGDualPutError()
    {
        // in progress - will also request dual status same command is fine for tests.
        CasdaArchivingDepositState state = getState(successRegCommand, failingCommand);

        state.progress();
        assertThat(state.getDepositable().getDepositState().getType(), is(DepositState.Type.FAILED));
    }

    @Test
    public void testCommandReturnMIG()
    {
        // in progress
        CasdaArchivingDepositState state = getState(successMigCommand);

        state.progress();
        assertThat(state.getDepositable().getDepositState().getType(), is(DepositState.Type.ARCHIVING));
    }

    private CasdaArchivingDepositState getState(String commandString)
    {
        return this.getState(commandString, "{}");
    }

    @Test
    public void testLoggingForNgasError() throws ServiceCallException
    {
        Exception exception = new ServiceCallException("BaD");
        when(ngasService.getStatus(getTestFileId())).thenThrow(exception);
        // this command is not fired as contacting ngas for dmf location will fail first
        CasdaArchivingDepositState state = getState(failingCommand);

        state.progress();
        // depositable remains in archiving state
        assertThat(state.getDepositable().getDepositState().getType(), is(DepositState.Type.ARCHIVING));

        checkTestAppenderForNgasError(exception);

    }

    protected abstract String getTestFileId();

    protected abstract CasdaArchivingDepositState getState(String statusCommandString, String putCommandString);

    protected abstract void checkTestAppenderForNgasError(Exception exception);

}
