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


import static org.hamcrest.Matchers.array;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Level;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import au.csiro.casda.BadRequestException;
import au.csiro.casda.ResourceNotFoundException;
import au.csiro.casda.deposit.manager.Level7DepositService;
import au.csiro.casda.dto.DepositStateDTO;
import au.csiro.casda.dto.ParentDepositableDTO;
import au.csiro.casda.jobmanager.CasdaToolProcessJobBuilder;
import au.csiro.casda.jobmanager.ProcessJob;
import au.csiro.casda.jobmanager.SingleJobMonitor;
import au.csiro.casda.services.dto.Message.MessageCode;
import au.csiro.casda.services.dto.MessageDTO;

/**
 * Tests the DepositProcessingController.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public class Level7DepositProcessingControllerTest
{
    private Log4JTestAppender testAppender;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private Level7DepositProcessingController controller;

    private File tempLevel7Folder;

    @Mock
    private Level7DepositService level7DepositService;

    @Mock
    private CasdaToolProcessJobBuilderFactory processBuilderFactory;

    @Mock
    private CasdaToolProcessJobBuilder processBuilder;

    @Mock
    private SingleJobMonitorFactory singleJobMonitorFactory;

    @Mock
    private ProcessJob processJob;

    @Mock
    private SingleJobMonitor monitor;

    @Before
    public void setUp() throws Exception
    {
        testAppender = Log4JTestAppender.createAppender();
        MockitoAnnotations.initMocks(this);
        tempLevel7Folder = tempFolder.newFolder("level7");

        controller =
                new Level7DepositProcessingController(tempFolder.newFolder("validation").toString(),
                        level7DepositService, processBuilderFactory, singleJobMonitorFactory);
    }

    @Test
    public void testSaveCatalogueForAlreadyProcessedCollection() throws Exception
    {
        exception.expect(BadRequestException.class);
        exception.expectMessage("Level 7 collection with id '123456' deposit has already been initiated");

        ParentDepositableDTO parentDTO = new ParentDepositableDTO();
        parentDTO.setDepositState(DepositStateDTO.DEPOSITING);

        when(level7DepositService.getLevel7CollectionSummary("ABC123", new Long(123456))).thenReturn(parentDTO);
        controller.saveCatalogueForDeposit("ABC123", "123456", mock(MultipartFile.class));
    }

    @Test
    public void testSaveCatalogueForProcessingEmptyFile() throws Exception
    {
        exception.expect(BadRequestException.class);
        exception.expectMessage("Incoming file is empty originalFilename.txt");

        MultipartFile file = mock(MultipartFile.class);
        doReturn(true).when(file).isEmpty();
        doReturn("originalFilename.txt").when(file).getOriginalFilename();

        controller.saveCatalogueForDeposit("ABC123", "123456", file);
    }

    @Test
    public void testSaveCatalogueForProcessingNonEmptyFile() throws Exception
    {
        String filename = "src/test/resources/testFile/testFile.txt";
        File savedCatalogueFolder = new File(tempLevel7Folder, "14231");
        File savedFile = new File(savedCatalogueFolder, "testFile.txt");

        MultipartFile file = mock(MultipartFile.class);
        doReturn(false).when(file).isEmpty();
        doReturn("testFile.txt").when(file).getOriginalFilename();
        doReturn(new FileInputStream(new File(filename))).when(file).getInputStream();

        when(level7DepositService.saveFileForLevel7CollectionDeposit(eq(new Long(14231)), eq("testFile.txt"), any()))
                .thenReturn(savedFile.toPath().toString());

        MessageDTO result = controller.saveCatalogueForDeposit("ABC123", "14231", file);
        assertEquals("Successfully created file " + savedFile.toPath(), result.getMessage());
        assertEquals(MessageCode.SUCCESS, result.getMessageCode());
    }

    @Test
    public void testInitiateLevel7CollectionDepositValidatesCollectionIdZero() throws Exception
    {
        exception.expect(BadRequestException.class);
        exception.expectMessage("Invalid collection id");

        controller.initiateLevel7CollectionDeposit("ABC213", "0");
    }

    @Test
    public void testInitiateLevel7CollectionDepositValidatesCollectionIdNegative() throws Exception
    {
        exception.expect(BadRequestException.class);
        exception.expectMessage("Invalid collection id");

        controller.initiateLevel7CollectionDeposit("ABC213", "-5");
    }

    @Test
    public void testInitiateLevel7CollectionDepositValidatesCollectionDecimal() throws Exception
    {
        exception.expect(BadRequestException.class);
        exception.expectMessage("Invalid collection id");

        controller.initiateLevel7CollectionDeposit("ABC213", "5.1");
    }

    @Test
    public void testInitiateLevel7CollectionDepositValidatesCollectionNotNumber() throws Exception
    {
        exception.expect(BadRequestException.class);
        exception.expectMessage("Invalid collection id");

        controller.initiateLevel7CollectionDeposit("ABC213", "string");
    }

    @Test
    public void testInitiateLevel7CollectionForCollectionInIllegalState() throws Exception
    {
        Level7DepositService.CollectionIllegalStateException expectedException =
                mock(Level7DepositService.CollectionIllegalStateException.class);
        when(expectedException.getMessage()).thenReturn("Weee!");
        doThrow(expectedException).when(level7DepositService).initiateLevel7CollectionDeposit("ABC213",
                new Long(515198));

        exception.expect(BadRequestException.class);
        exception.expectMessage("Weee!");

        ParentDepositableDTO parentDTO = new ParentDepositableDTO();
        parentDTO.setDepositState(DepositStateDTO.BUILDING_DEPOSIT);
        when(level7DepositService.getLevel7CollectionSummary("ABC213", new Long(515198))).thenReturn(parentDTO);

        controller.initiateLevel7CollectionDeposit("ABC213", "515198");
    }

    @Test
    public void testInitiateLevel7CollectionDepositSuccess() throws Exception
    {
        File catalogueDirectory = new File(tempLevel7Folder, "515198");
        catalogueDirectory.mkdirs();
        File file = new File(catalogueDirectory, "catalogue.xml");
        FileUtils.writeStringToFile(file, "some data");
        assertTrue(file.exists());

        ParentDepositableDTO parentDTO = new ParentDepositableDTO(catalogueDirectory);
        when(level7DepositService.getLevel7CollectionSummary("ABC213", new Long(515198))).thenReturn(parentDTO);

        MessageDTO result = controller.initiateLevel7CollectionDeposit("ABC213", "515198");
        assertEquals(MessageCode.SUCCESS, result.getMessageCode());
        assertEquals("Successfully created level 7 collection 515198", result.getMessage());

        testAppender.verifyLogMessage(Level.INFO,
                "Hit the controller for '/projects/ABC213/level_7_collections/515198/ready");
        testAppender.verifyLogMessage(Level.INFO, "E036] [New deposit job] [New Level 7 collection with ID of 515198 ");
    }

    @Test
    public void testRecoverLevel7CollectionForUnknownCollection() throws Exception
    {
        Level7DepositService.UnknownCollectionException expectedException =
                mock(Level7DepositService.UnknownCollectionException.class);
        when(expectedException.getMessage()).thenReturn("Weee!");
        doThrow(expectedException).when(level7DepositService).recoverFailedLevel7CollectionDeposit("ABC213",
                new Long(515198));

        try
        {
            controller.recoverFailedLevel7CollectionDeposit("ABC213", "515198");
            fail("Expected ResourceNotFoundException");
        }
        catch (ResourceNotFoundException ex)
        {
            assertThat(ex.getMessage(), equalTo("Weee!"));
            testAppender.verifyLogMessage(Level.INFO,
                    "Hit the controller for '/projects/ABC213/level_7_collections/515198/recover");
            testAppender.verifyLogMessage(Level.ERROR, "[E127] [Level 7 deposit doesn't exist] "
                    + "[Level 7 deposit 515198 does not exist.] Project code: ABC213");
        }
    }

    @Test
    public void testRecoverLevel7CollectionForMismatchingCollection() throws Exception
    {
        Level7DepositService.CollectionProjectCodeMismatchException expectedException =
                mock(Level7DepositService.CollectionProjectCodeMismatchException.class);
        when(expectedException.getMessage()).thenReturn("Weee!");
        doThrow(expectedException).when(level7DepositService).recoverFailedLevel7CollectionDeposit("ABC213",
                new Long(515198));

        try
        {
            controller.recoverFailedLevel7CollectionDeposit("ABC213", "515198");
            fail("Expected BadRequestException");
        }
        catch (BadRequestException ex)
        {
            assertThat(ex.getMessage(), equalTo("Weee!"));
            testAppender.verifyLogMessage(Level.INFO,
                    "Hit the controller for '/projects/ABC213/level_7_collections/515198/recover");
            testAppender.verifyLogMessage(Level.ERROR, "[E124] [Level 7 deposit has malformed parameters] "
                    + "[Not able to retry Level 7 deposit as parameters incorrectly entered.]", expectedException);
        }
    }

    @Test
    public void testRecoverLevel7CollectionForNonFailedCollection() throws Exception
    {
        Level7DepositService.CollectionIllegalStateException expectedException =
                mock(Level7DepositService.CollectionIllegalStateException.class);
        when(expectedException.getMessage()).thenReturn("Weee!");
        doThrow(expectedException).when(level7DepositService).recoverFailedLevel7CollectionDeposit("ABC213",
                new Long(515198));

        try
        {
            controller.recoverFailedLevel7CollectionDeposit("ABC213", "515198");
            fail("Expected ResourceNotFoundException");
        }
        catch (BadRequestException ex)
        {
            assertThat(ex.getMessage(), equalTo("Weee!"));
            testAppender.verifyLogMessage(Level.INFO,
                    "Hit the controller for '/projects/ABC213/level_7_collections/515198/recover");
            testAppender.verifyLogMessage(Level.ERROR, "[E123] [Level 7 recovery not required] "
                    + "[Not able to recover Level 7 deposit 515198 as it is not in a failed state.] "
                    + "Project code: ABC213");
        }
    }

    @Test
    public void testRecoverLevel7CollectionFailure() throws Exception
    {
        ParentDepositableDTO parentDTO = new ParentDepositableDTO();
        parentDTO.setDepositState(DepositStateDTO.DEPOSIT_FAILED);
        when(level7DepositService.getLevel7CollectionSummary("ABC213", new Long(515198))).thenReturn(parentDTO);

        RuntimeException runtime = new RuntimeException("foo");
        doThrow(runtime).when(level7DepositService).recoverFailedLevel7CollectionDeposit("ABC213", new Long(515198));

        try
        {
            controller.recoverFailedLevel7CollectionDeposit("ABC213", "515198");
            fail("Expected ResourceNotFoundException");
        }
        catch (RuntimeException ex)
        {
            assertThat(ex.getMessage(), equalTo("foo"));
            testAppender.verifyLogMessage(Level.INFO,
                    "Hit the controller for '/projects/ABC213/level_7_collections/515198/recover");
            testAppender.verifyLogMessage(Level.ERROR, "[E125] [Level 7 unsuccessful reset] "
                    + "[Level 7 deposit 515198 was unable to be reset.] Project code: ABC213", runtime);
        }
    }

    @Test
    public void testRecoverLevel7CollectionDepositSuccess() throws Exception
    {
        ParentDepositableDTO parentDTO = new ParentDepositableDTO();
        parentDTO.setDepositState(DepositStateDTO.DEPOSIT_FAILED);
        when(level7DepositService.getLevel7CollectionSummary("ABC213", new Long(515198))).thenReturn(parentDTO);

        MessageDTO result = controller.recoverFailedLevel7CollectionDeposit("ABC213", "515198");
        assertEquals(MessageCode.SUCCESS, result.getMessageCode());
        assertEquals("Successfully recovered level 7 collection 515198", result.getMessage());

        testAppender.verifyLogMessage(Level.INFO,
                "Hit the controller for '/projects/ABC213/level_7_collections/515198/recover");
        testAppender.verifyLogMessage(Level.INFO, "[E126] [Level 7 successful reset] "
                + "[Level 7 deposit 515198 was successfully reset.] Project code: ABC213");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testValidateEmptyCatalogue() throws Exception
    {
        MockMultipartFile file = new MockMultipartFile("catalogue.xml", "".getBytes(Charsets.UTF_8));
        String[] messages = controller.validateLevel7Catalogue("AS030", "119911", file);
        assertThat(messages, is(array(equalTo("File is empty"))));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testValidateCatalogue() throws Exception
    {
        when(processBuilderFactory.createBuilder()).thenReturn(processBuilder);
        when(processBuilder.setCommand(anyString())).thenReturn(processBuilder);
        when(processBuilder.addCommandArgument(anyString(), anyString())).thenReturn(processBuilder);
        when(processBuilder.addCommandSwitch(anyString())).thenReturn(processBuilder);
        when(processBuilder.createJob(anyString(), anyString())).thenReturn(processJob);
        when(singleJobMonitorFactory.createSingleJobMonitor()).thenReturn(monitor);
        when(monitor.isJobFailed()).thenReturn(false);

        when(monitor.getJobOutput()).thenReturn(
                "Error in PARAM: 1\nNot an error\nError in something:\n"
                        + "Error in FIELD: 2\nAnother message\nError in TABLE: 3\n\nAfter blank line\n"
                        + "Error in PARAM 2\nSome Error in\n    \t Error in TABLE: something");
        MockMultipartFile file = new MockMultipartFile("catalogue.xml", "<xml></xml>".getBytes(Charsets.UTF_8));
        String[] messages = controller.validateLevel7Catalogue("AS030", "119911", file);
        assertThat(
                messages,
                is(array(equalTo("Error in PARAM: 1"), equalTo("Error in something:"), equalTo("Error in FIELD: 2"),
                        equalTo("Error in TABLE: 3"), equalTo("Error in TABLE: something"))));

        when(monitor.getJobOutput()).thenReturn("");
        messages = controller.validateLevel7Catalogue("AS030", "119911", file);
        assertThat(messages, is(emptyArray()));
    }

}
