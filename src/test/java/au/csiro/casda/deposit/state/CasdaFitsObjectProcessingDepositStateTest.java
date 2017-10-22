package au.csiro.casda.deposit.state;

import static org.junit.Assert.assertThat;

import static org.hamcrest.CoreMatchers.is;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;

import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.entity.observation.Cubelet;
import au.csiro.casda.entity.observation.FitsObject;
import au.csiro.casda.entity.observation.ImageCube;
import au.csiro.casda.entity.observation.Level7Collection;
import au.csiro.casda.entity.observation.MomentMap;
import au.csiro.casda.entity.observation.Observation;
import au.csiro.casda.entity.observation.Spectrum;
import au.csiro.casda.jobmanager.CasdaToolProcessJobBuilder;
import au.csiro.casda.jobmanager.JobManager;

/**
 * Test class for CasdaFitsObjectProcessingDepositState
 * <p>
 * Copyright 2017, CSIRO Australia. All rights reserved.
 */
public class CasdaFitsObjectProcessingDepositStateTest
{
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Mock
    private DepositStateFactory stateFactory;

    @Mock
    private CasdaToolProcessJobBuilder processBuilder;

    @Mock
    private JobManager jobManager;

    @Test
    public void testGetJobIdImageCube() throws IOException
    {
        Observation obs = new Observation(12345);
        ImageCube imageCube = createImageCube(obs);

        CasdaFitsObjectProcessingDepositState state = createState(imageCube);
        assertThat(state.getJobId("fits-import"),
                is("fits-import-observations/12345/image_cubes/SB12345-spectral-cube.fits-0"));
    }

    @Test
    public void testGetJobIdSpectrum() throws IOException
    {
        Observation obs = new Observation(12345);
        Spectrum spectrum = new Spectrum();
        ImageCube imageCube = createImageCube(obs);
        spectrum.setImageCube(imageCube);
        spectrum.setParent(obs);

        CasdaFitsObjectProcessingDepositState state = createState(spectrum);
        assertThat(state.getJobId("fits-import"),
                is("fits-import-spectrum-observations/12345/image_cubes/SB12345-spectral-cube.fits-0"));
    }

    @Test
    public void testGetJobIdMomentMap() throws IOException
    {
        Observation obs = new Observation(12345);
        MomentMap momentMap = new MomentMap();
        ImageCube imageCube = createImageCube(obs);
        momentMap.setImageCube(imageCube);
        momentMap.setParent(obs);

        CasdaFitsObjectProcessingDepositState state = createState(momentMap);
        assertThat(state.getJobId("fits-import"),
                is("fits-import-moment-map-observations/12345/image_cubes/SB12345-spectral-cube.fits-0"));
    }

    @Test
    public void testGetJobIdCubelet() throws IOException
    {
        Observation obs = new Observation(12345);
        Cubelet cubelet = new Cubelet();
        ImageCube imageCube = createImageCube(obs);
        cubelet.setImageCube(imageCube);
        cubelet.setParent(obs);

        CasdaFitsObjectProcessingDepositState state = createState(cubelet);
        assertThat(state.getJobId("fits-import"),
                is("fits-import-cubelet-observations/12345/image_cubes/SB12345-spectral-cube.fits-0"));
    }

    @Test
    public void testGetJobIdLevel7Spectrum() throws IOException
    {
        Level7Collection l7c = new Level7Collection(191);
        Spectrum spectrum = new Spectrum();
        spectrum.setParent(l7c);
        spectrum.setFilename("spectrum.fits");

        CasdaFitsObjectProcessingDepositState state = createState(spectrum);
        assertThat(state.getJobId("fits-import"),
                is("fits-import-level7/191/spectra/spectrum.fits-0"));
    }

    @Test
    public void testGetJobIdLevel7MomentMap() throws IOException
    {
        Level7Collection l7c = new Level7Collection(191);
        MomentMap momentMap = new MomentMap();
        momentMap.setParent(l7c);
        momentMap.setFilename("mom0.fits");

        CasdaFitsObjectProcessingDepositState state = createState(momentMap);
        assertThat(state.getJobId("fits-import"),
                is("fits-import-level7/191/moment_maps/mom0.fits-0"));
    }

    private ImageCube createImageCube(Observation obs)
    {
        ImageCube imageCube = new ImageCube();
        imageCube.setFilename("SB12345-spectral-cube.fits");
        imageCube.setParent(obs);
        return imageCube;
    }

    private CasdaFitsObjectProcessingDepositState createState(FitsObject fitsObject) throws IOException
    {
        File parentFolder = tempFolder.newFolder();
        CasdaFitsObjectProcessingDepositState state =
                new CasdaFitsObjectProcessingDepositState(stateFactory, fitsObject, parentFolder.getCanonicalPath(),
                        parentFolder.getCanonicalPath(), processBuilder, jobManager);
        return state;
    }
}
