package au.csiro.casda.deposit.manager;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.MockitoAnnotations;

import au.csiro.casda.deposit.exception.PollingException;
import au.csiro.casda.deposit.jpa.ObservationRepository;
import au.csiro.casda.deposit.services.ObservationService;
import au.csiro.casda.jobmanager.JavaProcessJobFactory;
import au.csiro.casda.jobmanager.SynchronousProcessJobManager;

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
 * Test of DepositManager.findReadyFilesDirs() method.
 * 
 * Copyright 2014, CSIRO Australia. All rights reserved.
 * 
 */
public class ObservationsJobsHandlerFindFilesTest
{
    private ObservationsJobsHandler observationsJobsHandler;

    private Path testObsRootDir = null;

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private ObservationService observationService;
    
    @Before
    public void setUp() throws IOException
    {
        MockitoAnnotations.initMocks(this);
        testObsRootDir = Paths.get(tempFolder.newFolder("DATA_DEPOSIT_TEST_ROOTDIR").getPath());
        observationService = new ObservationService();
        observationsJobsHandler = new ObservationsJobsHandler(new JavaProcessJobFactory(), ".", ".", null,
                mock(SynchronousProcessJobManager.class), observationService,
                mock(ObservationRepository.class));
    }

    @Test
    public void testNoFileNull() throws PollingException, IOException
    {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("depositObservationParentDirectory isBlank");
        observationsJobsHandler.findNewObservationDirs(null);
    }

    @Test
    public void testNoFileEmpty() throws PollingException, IOException
    {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("depositObservationParentDirectory isBlank");
        observationsJobsHandler.findNewObservationDirs("");
    }

    @Test
    public void testNoFileSpaces() throws PollingException, IOException
    {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage("depositObservationParentDirectory isBlank");
        observationsJobsHandler.findNewObservationDirs("   ");
    }

    /**
     * ROOT_DIR / obs01 / {UNKNOWN} - NO 'READY' file
     * 
     * @throws PollingException
     *             if an IOException
     * @throws IOException
     *             if an IOException
     */
    @Test
    public void testFindReadyFilesNOReadyFilesExist() throws PollingException, IOException
    {
        String subDirName = "obs01";
        Path obsDir1 = Files.createTempDirectory(testObsRootDir, subDirName);
        Path obsDir1ReadyFile2 = Files.createFile(Paths.get(obsDir1.toString(), "UNKNOWN"));

        Assert.assertTrue(obsDir1ReadyFile2.toFile().exists());

        List<Path> readyFiles = observationsJobsHandler.findNewObservationDirs(testObsRootDir.toString());

        Assert.assertEquals(0, readyFiles.size());
    }

    /**
     * ROOT_DIR / obs01 / {READY, UNKNOWN}
     * 
     * @throws PollingException
     *             if an IOException
     * @throws IOException
     *             if an IOException
     */
    @Test
    public void testFindReadyFilesOneDirsReadyFilesExist() throws PollingException, IOException
    {
        String subDirName = "obs01";
        Path obsDir1 = Files.createTempDirectory(testObsRootDir, subDirName);
        Path obsDir1ReadyFile1 = Files.createFile(Paths.get(obsDir1.toString(), "READY"));
        Path obsDir1ReadyFile2 = Files.createFile(Paths.get(obsDir1.toString(), "UNKNOWN"));

        Assert.assertTrue(obsDir1ReadyFile1.toFile().exists());
        Assert.assertTrue(obsDir1ReadyFile2.toFile().exists());

        List<Path> readyFiles = observationsJobsHandler.findNewObservationDirs(testObsRootDir.toString());

        Assert.assertEquals(1, readyFiles.size());
        Assert.assertThat(readyFiles.get(0).toString(), containsString(testObsRootDir.toString()));
        Assert.assertThat(readyFiles.get(0).toString(), containsString(subDirName));
    }

    /**
     * ROOT_DIR / obs01 / {READY}
     * <p>
     * ROOT_DIR / obs02 / {READY}
     * 
     * @throws IOException
     * @throws PollingException
     */
    @Test
    public void testFindReadyFilesTwoDirsReadyFilesExist() throws IOException, PollingException
    {
        String subDirName1 = "obs01";
        String subDirName2 = "obs02";

        Path obsDir1 = Files.createTempDirectory(testObsRootDir, subDirName1);
        Path obsDir1ReadyFile1 = Files.createFile(Paths.get(obsDir1.toString(), "READY"));
        Path obsDir2 = Files.createTempDirectory(testObsRootDir, subDirName2);
        Path obsDir1ReadyFile2 = Files.createFile(Paths.get(obsDir2.toString(), "READY"));

        Assert.assertTrue(obsDir1ReadyFile1.toFile().exists());
        Assert.assertTrue(obsDir1ReadyFile2.toFile().exists());

        List<Path> readyFiles = observationsJobsHandler.findNewObservationDirs(testObsRootDir.toString());

        Assert.assertEquals(2, readyFiles.size());
    }

    /**
     * ROOT_DIR / obs01 / {READY}
     * <p>
     * ROOT_DIR / obs02 / {READY}
     * <p>
     * ROOT_DIR / obs03 / {READY, DONE}
     * 
     * @throws IOException
     * @throws PollingException
     */
    @Test
    public void testFindReadyFilesThreeDirsReadyFilesAndOneDoneExist() throws IOException, PollingException
    {
        String subDirName1 = "obs01";
        String subDirName2 = "obs02";
        String subDirName3 = "obs03";

        Path obsDir1 = Files.createTempDirectory(testObsRootDir, subDirName1);
        Path obsDir1ReadyFile1 = Files.createFile(Paths.get(obsDir1.toString(), "READY"));
        Path obsDir2 = Files.createTempDirectory(testObsRootDir, subDirName2);
        Path obsDir1ReadyFile2 = Files.createFile(Paths.get(obsDir2.toString(), "READY"));
        Path obsDir3 = Files.createTempDirectory(testObsRootDir, subDirName3);
        Path obsDir3ReadyFile3 = Files.createFile(Paths.get(obsDir3.toString(), "READY"));
        Path obsDir3DoneFile1 = Files.createFile(Paths.get(obsDir3.toString(), "DONE"));

        Assert.assertTrue(obsDir1ReadyFile1.toFile().exists());
        Assert.assertTrue(obsDir1ReadyFile2.toFile().exists());
        Assert.assertTrue(obsDir3ReadyFile3.toFile().exists());
        Assert.assertTrue(obsDir3DoneFile1.toFile().exists());

        List<Path> readyFiles = observationsJobsHandler.findNewObservationDirs(testObsRootDir.toString());

        // The obs dir with the DONE file should not be found
        Assert.assertEquals(2, readyFiles.size());
    }
    
    /**
     * Same as test above but checks that directories containing an ERROR file is also skipped
     * ROOT_DIR / obs01 / {READY}
     * <p>
     * ROOT_DIR / obs02 / {READY}
     * <p>
     * ROOT_DIR / obs03 / {READY, DONE}
     * 
     * @throws IOException
     * @throws PollingException
     */
    @Test
    public void testFindErrorFilesThreeDirsReadyFilesAndOneDoneExist() throws IOException, PollingException
    {
        String subDirName1 = "obs01";
        String subDirName2 = "obs02";
        String subDirName3 = "obs03";

        Path obsDir1 = Files.createTempDirectory(testObsRootDir, subDirName1);
        Path obsDir1ReadyFile1 = Files.createFile(Paths.get(obsDir1.toString(), "READY"));
        Path obsDir2 = Files.createTempDirectory(testObsRootDir, subDirName2);
        Path obsDir1ReadyFile2 = Files.createFile(Paths.get(obsDir2.toString(), "READY"));
        Path obsDir3 = Files.createTempDirectory(testObsRootDir, subDirName3);
        Path obsDir3ReadyFile3 = Files.createFile(Paths.get(obsDir3.toString(), "READY"));
        Path obsDir3DoneFile1 = Files.createFile(Paths.get(obsDir3.toString(), "ERROR"));

        Assert.assertTrue(obsDir1ReadyFile1.toFile().exists());
        Assert.assertTrue(obsDir1ReadyFile2.toFile().exists());
        Assert.assertTrue(obsDir3ReadyFile3.toFile().exists());
        Assert.assertTrue(obsDir3DoneFile1.toFile().exists());

        List<Path> readyFiles = observationsJobsHandler.findNewObservationDirs(testObsRootDir.toString());

        // The obs dir with the DONE file should not be found
        Assert.assertEquals(2, readyFiles.size());
        
        // Ensure that the entry with an ERROR file was registered for display
        String obsFolderName = obsDir3.getFileName().toString();
        Assert.assertThat(observationService.getInvalidObservationIds(), contains(obsFolderName));
    }

    @Test
    public void testConsumerThrows() throws PollingException, IOException
    {
        // I added a ConsumerThrows interface that extends Java 8's Consumer for Lambdas. And want to check this
        // operates as expected.
        exception.expect(PollingException.class);
        observationsJobsHandler.findNewObservationDirs("some/path/that/willnot/exist");
    }
  
    @Test
    public void testBrokenLinkForParentDir() throws PollingException, IOException
    {
        //this line can be commented out and the test run on windows if you have opened you eclipse/cmd prompt as Admin
        Assume.assumeTrue(!System.getProperty("os.name").startsWith("Windows"));
        Path firstDir = Files.createTempDirectory(testObsRootDir, "firstDir");

        // contents of first directory
        List<Path> pathList = new ArrayList<Path>();

        // symbolic link
        Path symLink = Files.createSymbolicLink(Paths.get(testObsRootDir.toString() + "/linkDirectory"), firstDir);
        pathList.add(symLink.getFileName());
        
        //break the symbolic link
        Files.delete(firstDir);
        
        List<Path> paths = observationsJobsHandler.getAllObservationDirs(testObsRootDir.toString());

        // List should be empty as this symbolic link was broken
        Assert.assertEquals(0, paths.size());
    }
    
    @Test
    public void testWorkingLinkForParentDir() throws IOException, PollingException
    {
        //this line can be commented out and the test run on windows if you have opened you eclipse/cmd prompt as Admin
        Assume.assumeTrue(!System.getProperty("os.name").startsWith("Windows"));
        Path firstDir = Files.createTempDirectory(testObsRootDir, "firstDir");

        // contents of first directory
        List<Path> pathList = new ArrayList<Path>();
        pathList.add(Files.createTempDirectory(firstDir, "dir1").getFileName());
        pathList.add(Files.createTempDirectory(firstDir, "dir2").getFileName());
        pathList.add(Files.createTempDirectory(firstDir, "dir3").getFileName());

        Files.createTempFile(firstDir, "myFile", ".txt");
        // symbolic link
        Path symLink = Files.createSymbolicLink(Paths.get(testObsRootDir.toString() + "/linkDirectory"), firstDir);
        pathList.add(symLink.getFileName());
        
        List<Path> paths = observationsJobsHandler.getAllObservationDirs(testObsRootDir.toString());

        // should count one directory and one symbolic link to directory
        Assert.assertEquals(2, paths.size());
    }
}
