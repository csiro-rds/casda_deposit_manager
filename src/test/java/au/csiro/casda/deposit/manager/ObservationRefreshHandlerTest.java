package au.csiro.casda.deposit.manager;

import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import static org.hamcrest.CoreMatchers.is;

import org.apache.logging.log4j.Level;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import au.csiro.casda.deposit.CasdaToolProcessJobBuilderFactory;
import au.csiro.casda.deposit.DepositManagerEvents;
import au.csiro.casda.deposit.Log4JTestAppender;
import au.csiro.casda.deposit.jobqueue.QueuedJobManager;
import au.csiro.casda.deposit.jobqueue.QueuedJobStatus;
import au.csiro.casda.deposit.jpa.ObservationRefreshRepository;
import au.csiro.casda.deposit.jpa.ObservationRepository;
import au.csiro.casda.deposit.jpa.RefreshJobRepository;
import au.csiro.casda.deposit.services.NgasService;
import au.csiro.casda.deposit.services.NgasService.ServiceCallException;
import au.csiro.casda.deposit.services.NgasService.Status;
import au.csiro.casda.entity.observation.ImageCube;
import au.csiro.casda.entity.observation.Observation;
import au.csiro.casda.entity.refresh.ObservationRefresh;
import au.csiro.casda.entity.refresh.RefreshJob;
import au.csiro.casda.entity.refresh.RefreshStateType;
import au.csiro.casda.jobmanager.CasdaToolProcessJobBuilder;
import au.csiro.casda.jobmanager.ProcessJobBuilder.ProcessJobFactory;
import au.csiro.casda.util.EncapsulationTools;

/**
 * Tests for the ObservationRefreshHandler class.
 * <p>
 * Copyright 2017, CSIRO Australia. All rights reserved.
 */
public class ObservationRefreshHandlerTest
{
    private Log4JTestAppender testAppender;

    @Mock
    private ObservationRepository observationRepository;

    @Mock
    private ObservationRefreshRepository observationRefreshRepository;

    @Mock
    private RefreshJobRepository refreshJobRepository;

    @Mock
    private QueuedJobManager jobManager;

    @Mock
    private CasdaToolProcessJobBuilderFactory casdaToolBuilderFactory;

    @Mock
    private ProcessJobFactory processJobFactory;

    @Mock
    private NgasService ngasService;

    @Mock
    private EncapsulationTools encapsulationTools;

    private ObservationRefreshHandler obsRefreshHandler;

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        testAppender = Log4JTestAppender.createAppender();
        MockitoAnnotations.initMocks(this);
        
        String encapsWorkingPath = "";
        String archiveGetCommandAndArgs = "{\"/dev/cygwin/bin/bash\",\"-c\",\"echo\"}";
        obsRefreshHandler = new ObservationRefreshHandler(observationRepository, observationRefreshRepository,
                refreshJobRepository, jobManager, casdaToolBuilderFactory, processJobFactory, ngasService,
                encapsulationTools, encapsWorkingPath, archiveGetCommandAndArgs, archiveGetCommandAndArgs, 0, "");
    }

    
    @Test
    public void testRefreshAnObservation() throws ServiceCallException
    {
        final int obsId = 100;
        final Integer sbid = 123;
        Observation obs = new Observation(obsId);
        ImageCube imageCube = new ImageCube();
        imageCube.setFilename("something.fits");
        obs.addImageCube(imageCube);
        
        // Set up an OR with a single image cube
        ObservationRefresh obsRefresh = new ObservationRefresh();
        obsRefresh.setObservationId(obsId);
        obsRefresh.setSbid(sbid);
        when(observationRepository.findBySbid(sbid)).thenReturn(obs);
        
        when(jobManager.isQueueIdle(anyString())).thenReturn(true, false);
        when(jobManager.getJobStatus(anyString())).thenReturn(null, QueuedJobStatus.RUNNING, QueuedJobStatus.COMPLETED);
        Status ngasStatus = Mockito.mock(Status.class);
        when(ngasStatus.wasSuccess()).thenReturn(true);
        when(ngasStatus.getMountPoint()).thenReturn("/drive");
        when(ngasStatus.getFileName()).thenReturn("filename");
        when(ngasService.getStatus(anyString())).thenReturn(ngasStatus);
        CasdaToolProcessJobBuilder jobBuilder = Mockito.mock(CasdaToolProcessJobBuilder.class);
        when(jobBuilder.addCommandArgument(anyString(), anyString())).thenReturn(jobBuilder);
        when(jobBuilder.setCommand(anyString())).thenReturn(jobBuilder);
        
        when(casdaToolBuilderFactory.createBuilder()).thenReturn(jobBuilder);
        
        // Advance it first to create the job
        obsRefreshHandler.refreshAnObservation(obsRefresh);
        assertThat(obsRefresh.getRefreshState(), is(RefreshStateType.PROCESSING));
        testAppender.verifyLogMessage(Level.INFO, DepositManagerEvents.E159.messageBuilder().add(sbid).toString());
        
        // Advance it a second time while the job is still running
        obsRefreshHandler.refreshAnObservation(obsRefresh);
        assertThat(obsRefresh.getRefreshState(), is(RefreshStateType.PROCESSING));
        
        // Advance it a third time now that the job is complete
        obsRefreshHandler.refreshAnObservation(obsRefresh);
        assertThat(obsRefresh.getRefreshState(), is(RefreshStateType.PROCESSED));
        testAppender.verifyLogMessage(Level.INFO, DepositManagerEvents.E158.messageBuilder().add(sbid).toString());
        
    }

    
    @Test
    public void testEnsureObservationIsOnline() throws ServiceCallException
    {
        final int obsId = 100;
        final Integer sbid = 123;
        Observation obs = new Observation(obsId);
        ImageCube imageCube = new ImageCube();
        imageCube.setFilename("something.fits");
        obs.addImageCube(imageCube);
        
        // Set up an OR with a single image cube
        ObservationRefresh obsRefresh = new ObservationRefresh();
        obsRefresh.setObservationId(obsId);
        obsRefresh.setSbid(sbid);
        when(observationRepository.findOne((long) obsId)).thenReturn(obs);
        
        when(jobManager.isQueueIdle(anyString())).thenReturn(true, false);
        when(jobManager.getJobStatus(anyString())).thenReturn(null, QueuedJobStatus.RUNNING, QueuedJobStatus.COMPLETED);
        Status ngasStatus = Mockito.mock(Status.class);
        when(ngasStatus.wasSuccess()).thenReturn(true);
        when(ngasStatus.getMountPoint()).thenReturn("/drive");
        when(ngasStatus.getFileName()).thenReturn("filename");
        when(ngasService.getStatus(anyString())).thenReturn(ngasStatus);
        CasdaToolProcessJobBuilder jobBuilder = Mockito.mock(CasdaToolProcessJobBuilder.class);
        when(jobBuilder.addCommandArgument(anyString(), anyString())).thenReturn(jobBuilder);
        when(jobBuilder.setCommand(anyString())).thenReturn(jobBuilder);
        
        when(casdaToolBuilderFactory.createBuilder()).thenReturn(jobBuilder);
        
        // Advance it first to create the job
        obsRefreshHandler.ensureObservationIsOnline(obsRefresh);
        assertThat(obsRefresh.getRefreshState(), is(RefreshStateType.UNREFRESHED));
        testAppender.verifyLogMessage(Level.INFO, "Bringing paths");
        
        // Advance it a second time while the job is still running
        obsRefreshHandler.ensureObservationIsOnline(obsRefresh);
        assertThat(obsRefresh.getRefreshState(), is(RefreshStateType.UNREFRESHED));
        
        // Advance it a third time now that the job is complete
        obsRefreshHandler.ensureObservationIsOnline(obsRefresh);
        assertThat(obsRefresh.getRefreshState(), is(RefreshStateType.READY));
        testAppender.verifyLogMessage(Level.INFO, "Files now online");
        
    }

    
    @Test
    public void testFinaliseRefresh() throws ServiceCallException, InterruptedException
    {
        final int obsId = 100;
        final Integer sbid = 123;
        Observation obs = new Observation(obsId);
        Thread.sleep(1); // Ensure the deposit started date is in the past
        ImageCube imageCube = new ImageCube();
        imageCube.setFilename("something.fits");
        obs.addImageCube(imageCube);
        
        // Set up an OR with a single image cube
        RefreshJob refreshJob = new RefreshJob();
        ObservationRefresh obsRefresh = new ObservationRefresh();
        obsRefresh.setObservationId(obsId);
        obsRefresh.setSbid(sbid);
        obsRefresh.setRefreshState(RefreshStateType.PROCESSED);
        refreshJob.addObservationRefresh(obsRefresh);
        when(observationRepository.findOne((long) obsId)).thenReturn(obs);
        
        when(jobManager.isQueueIdle(anyString())).thenReturn(true, false);
        when(jobManager.getJobStatus(anyString())).thenReturn(null, QueuedJobStatus.RUNNING, QueuedJobStatus.COMPLETED);
        Status ngasStatus = Mockito.mock(Status.class);
        when(ngasStatus.wasSuccess()).thenReturn(true);
        when(ngasStatus.getMountPoint()).thenReturn("/drive");
        when(ngasStatus.getFileName()).thenReturn("filename");
        when(ngasService.getStatus(anyString())).thenReturn(ngasStatus);
        CasdaToolProcessJobBuilder jobBuilder = Mockito.mock(CasdaToolProcessJobBuilder.class);
        when(jobBuilder.addCommandArgument(anyString(), anyString())).thenReturn(jobBuilder);
        when(jobBuilder.setCommand(anyString())).thenReturn(jobBuilder);
        
        when(casdaToolBuilderFactory.createBuilder()).thenReturn(jobBuilder);
        
        // Advance it first to create the job
        obsRefreshHandler.finaliseRefresh(obsRefresh);
        assertThat(obsRefresh.getRefreshState(), is(RefreshStateType.PROCESSED));
        testAppender.verifyLogMessage(Level.INFO, "Finalising observation 123");
        testAppender.verifyLogMessage(Level.INFO, "Sending paths");
        testAppender.verifyNoMessages();

        // Advance it a second time while the job is still running
        obsRefreshHandler.finaliseRefresh(obsRefresh);
        assertThat(obsRefresh.getRefreshState(), is(RefreshStateType.PROCESSED));
        testAppender.verifyLogMessage(Level.INFO, "Finalising observation 123");
        testAppender.verifyNoMessages();
        
        // Advance it a third time now that the job is complete
        obsRefreshHandler.finaliseRefresh(obsRefresh);
        assertThat(obsRefresh.getRefreshState(), is(RefreshStateType.COMPLETED));
        testAppender.verifyLogMessage(Level.INFO, "Finalising observation 123");
        testAppender.verifyNoMessages();
        
    }
    
}
