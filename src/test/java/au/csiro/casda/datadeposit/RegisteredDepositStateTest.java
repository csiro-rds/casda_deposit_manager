package au.csiro.casda.datadeposit;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import static org.hamcrest.CoreMatchers.is;

import java.io.File;
import java.io.IOException;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import au.csiro.casda.deposit.CasdaToolProcessJobBuilderFactory;
import au.csiro.casda.deposit.SingleJobMonitorFactory;
import au.csiro.casda.deposit.jdbc.SimpleJdbcRepository;
import au.csiro.casda.deposit.services.NgasService;
import au.csiro.casda.deposit.services.VoToolsService;
import au.csiro.casda.deposit.state.CasdaDepositStateFactory;
import au.csiro.casda.entity.observation.ImageCube;
import au.csiro.casda.entity.observation.MeasurementSet;
import au.csiro.casda.entity.observation.Observation;
import au.csiro.casda.entity.observation.Project;
import au.csiro.casda.jobmanager.CasdaToolProcessJobBuilder;
import au.csiro.casda.jobmanager.JavaProcessJobFactory;
import au.csiro.casda.jobmanager.JobManager;

/**
 * Tests for the RegisteredDepositState class.
 * <p>
 * Copyright 2017, CSIRO Australia. All rights reserved.
 */
public class RegisteredDepositStateTest
{

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private File observationParentDir;

    private File level7ParentDir;

    @Mock
    private JobManager jobManager;

    @Mock
    private NgasService ngasService;

    @Mock
    private CasdaToolProcessJobBuilderFactory factory;

    @Spy
    private SingleJobMonitorFactory singleJobMonitorFactory;

    @Mock
    private CasdaToolProcessJobBuilder processBuilder;

    @Mock
    private VoToolsService voToolsService;

    @Mock
    private SimpleJdbcRepository simpleJdbcRepository;

    private DepositStateFactory depositStateFactory;

    @Before
    public void setUp() throws IOException
    {
        MockitoAnnotations.initMocks(this);

        observationParentDir = tempFolder.newFolder("observation");
        level7ParentDir = tempFolder.newFolder("level7");
        
        this.depositStateFactory = spy(new CasdaDepositStateFactory(ngasService, jobManager, factory,
                new JavaProcessJobFactory(), singleJobMonitorFactory, voToolsService, simpleJdbcRepository, "",
                observationParentDir.getAbsolutePath(), level7ParentDir.getAbsolutePath(), "{\"stageCommand\"}",
                "SIMPLE", "stageCommandAndArgs", "{\"registerCommand\"}", "SIMPLE", "registerCommandAndArgs",
                "{\"archiveStatus\"}", "{\"archivePut\"}",
                " {\"stage_artefact\", \"1\", \"register_artefact\", \"4\" }",
                "{\"mapcommand\", \"<imageFile>\", \"<projectCode>\"}"));
        
    }
    
    @Test
    public void testProgressImageCubeWithCoverage()
    {
        String imageType = "spectral_restored_3d";
        when(simpleJdbcRepository.isImageTypeIncludeCoverage(imageType)).thenReturn(true);

        DepositState registeredState = mock(DepositState.class);
        when(registeredState.getType()).thenReturn(DepositState.Type.REGISTERED);
        
        ImageCube imageCube = createImageCube(imageType, registeredState);
        
        RegisteredDepositState registeredDepositState =
                new RegisteredDepositState(depositStateFactory, imageCube, simpleJdbcRepository);

        assertThat(imageCube.getDepositStateType(), is(DepositState.Type.REGISTERED));
        registeredDepositState.progress();
        assertThat(imageCube.getDepositStateType(), is(DepositState.Type.MAPPING));
    }
    
    @Test
    public void testProgressImageCubeWithNoCoverage()
    {
        String imageType = "spectral_cleanmodel_3d";
        when(simpleJdbcRepository.isImageTypeIncludeCoverage(imageType)).thenReturn(false);

        DepositState registeredState = mock(DepositState.class);
        when(registeredState.getType()).thenReturn(DepositState.Type.REGISTERED);
        
        ImageCube imageCube = createImageCube(imageType, registeredState);
        
        RegisteredDepositState registeredDepositState =
                new RegisteredDepositState(depositStateFactory, imageCube, simpleJdbcRepository);

        assertThat(imageCube.getDepositStateType(), is(DepositState.Type.REGISTERED));
        registeredDepositState.progress();
        assertThat(imageCube.getDepositStateType(), is(DepositState.Type.ARCHIVING));
    }
    
    @Test
    public void testProgressNonImageCube()
    {
        DepositState registeredState = mock(DepositState.class);
        when(registeredState.getType()).thenReturn(DepositState.Type.REGISTERED);
        
        MeasurementSet ms = new MeasurementSet();
        ms.setDepositState(registeredState);
        ms.setFilename("ms.tar");
        Observation obs = new Observation();
        obs.setSbid(42);
        ms.setParent(obs);
        Project project = new Project("AA000");
        ms.setProject(project);
        
        RegisteredDepositState registeredDepositState =
                new RegisteredDepositState(depositStateFactory, ms, simpleJdbcRepository);

        assertThat(ms.getDepositStateType(), is(DepositState.Type.REGISTERED));
        registeredDepositState.progress();
        assertThat(ms.getDepositStateType(), is(DepositState.Type.ARCHIVING));
    }

    private ImageCube createImageCube(String imageType, DepositState registeredState)
    {
        ImageCube imageCube = new ImageCube();
        imageCube.setType(imageType);
        imageCube.setDepositState(registeredState);
        Observation obs = new Observation();
        obs.setSbid(42);
        imageCube.setParent(obs);
        Project project = new Project("AA000");
        imageCube.setProject(project);
        imageCube.setFilename("image.fits");
        return imageCube;
    }

}
