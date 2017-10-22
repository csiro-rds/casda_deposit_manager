package au.csiro.casda.dto;

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


import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;

import au.csiro.AbstractMarshallingTest;
import au.csiro.casda.dto.DataProductDTO.DataProductType;
import au.csiro.casda.dto.DataProductDTO.QualityLevel;
import au.csiro.casda.entity.ValidationNote;
import au.csiro.casda.entity.observation.Catalogue;
import au.csiro.casda.entity.observation.CatalogueType;
import au.csiro.casda.entity.observation.Cubelet;
import au.csiro.casda.entity.observation.ImageCube;
import au.csiro.casda.entity.observation.MeasurementSet;
import au.csiro.casda.entity.observation.MomentMap;
import au.csiro.casda.entity.observation.Observation;
import au.csiro.casda.entity.observation.Project;
import au.csiro.casda.entity.observation.Spectrum;

/**
 * Tests JSON serialisation and deserialisation of ObservationProjectDataProductsDTO and DataProductsDTO classes
 * 
 * Copyright 2015, CSIRO Australia All rights reserved.
 *
 */
public class ObservationProjectDataProductsDTOTest extends AbstractMarshallingTest<ObservationProjectDataProductsDTO>
{
    @Override
    protected ObservationProjectDataProductsDTO getTestObject()
    {
        ObservationProjectDataProductsDTO dataProducts =
                new ObservationProjectDataProductsDTO(12345, "ABC123", DateTime.now(), "Bob", "Smith");
        DataProductDTO dataProduct =
                new DataProductDTO(12L, DataProductType.CATALOGUE, CatalogueType.CONTINUUM_COMPONENT.name(), "file-id",
                        QualityLevel.GOOD);
        dataProducts.getDataProducts().add(dataProduct);
        return dataProducts;
    }

    @Test
    public void testConstructor()
    {
        DateTime now = DateTime.now();

        Observation observation = mock(Observation.class);
        when(observation.getObsStart()).thenReturn(now);
        when(observation.getSbid()).thenReturn(12345);

        Set<Project> projects = new HashSet<>();
        Project project = new Project("ABC123");
        project.setPrincipalFirstName("Bob");
        project.setPrincipalLastName("Smith");
        projects.add(project);
        Project project2 = new Project("ABC124");

        projects.add(project);
        projects.add(project2);

        when(observation.getProjects()).thenReturn(projects);

        Catalogue catalogue = spy(new Catalogue(CatalogueType.CONTINUUM_COMPONENT));
        catalogue.setId(12L);
        catalogue.setProject(project);
        doReturn("catalogue-file-id").when(catalogue).getFileId();
        List<Catalogue> catalogues = new ArrayList<>();
        catalogues.add(catalogue);

        MeasurementSet measurementSet = spy(new MeasurementSet());
        measurementSet.setId(16L);
        measurementSet.setProject(project);
        doReturn("measurementset-file-id").when(measurementSet).getFileId();
        List<MeasurementSet> measurementSets = new ArrayList<>();
        measurementSets.add(measurementSet);

        ImageCube imageCube = spy(new ImageCube());
        imageCube.setId(19L);
        imageCube.setType("cont_cleanmodel_T0");
        imageCube.setProject(project);
        doReturn("imagecube-file-id").when(imageCube).getFileId();
        List<ImageCube> imageCubes = new ArrayList<>();
        imageCubes.add(imageCube);

        Spectrum spectrum = spy(new Spectrum());
        spectrum.setId(25L);
        spectrum.setProject(project);
        spectrum.setType("spectral_noise_restored");
        doReturn("spectrum-file-id").when(spectrum).getFileId();
        List<Spectrum> spectra = new ArrayList<>();
        spectra.add(spectrum);
        
        MomentMap momentMap = spy(new MomentMap());
        momentMap.setId(17L);
        momentMap.setProject(project);
        momentMap.setType("spectral_restored_mom1");
        doReturn("moment-map-file-id").when(momentMap).getFileId();
        List<MomentMap> momentMaps = new ArrayList<>();
        momentMaps.add(momentMap);
        
        
        Cubelet cubelet = spy(new Cubelet());
        cubelet.setId(17L);
        cubelet.setProject(project);
        cubelet.setType("spectral_restored_3d");
        doReturn("cubelet-file-id").when(cubelet).getFileId();
        List<Cubelet> cubelets = new ArrayList<Cubelet>();
        cubelets.add(cubelet);
        
        when(observation.getCatalogues()).thenReturn(catalogues);
        when(observation.getMeasurementSets()).thenReturn(measurementSets);
        when(observation.getImageCubes()).thenReturn(imageCubes);
        when(observation.getSpectra()).thenReturn(spectra);
        when(observation.getMomentMaps()).thenReturn(momentMaps);
        when(observation.getCubelets()).thenReturn(cubelets);

        List<String> qualityFlagCodes = new ArrayList<>();
        qualityFlagCodes.add("flag1");
        qualityFlagCodes.add("flag2");
        
        List<ValidationNote> validationNotes = new ArrayList<>();
        ValidationNote note = new ValidationNote();
        note.setId(14L);
        note.setCreated(DateTime.now(DateTimeZone.UTC));
        note.setPersonId("smi123");
        note.setPersonName("Bob Smith");
        note.setContent("Content for note 1");
        ValidationNote note2 = new ValidationNote();
        note2.setId(15L);
        note2.setCreated(DateTime.now(DateTimeZone.UTC).plusHours(1));
        note2.setPersonId("smi143");
        note2.setPersonName("Jane Smith");
        note2.setContent("Content for note 2");
        validationNotes.add(note);
        validationNotes.add(note2);

        ObservationProjectDataProductsDTO dataProducts =
                new ObservationProjectDataProductsDTO("ABC123", observation, qualityFlagCodes, validationNotes);

        assertEquals(now.getMillis(), dataProducts.getObsStartDateMillis().longValue());
        assertEquals(6, dataProducts.getDataProducts().size());
        assertEquals("ABC123", dataProducts.getOpalCode());
        assertEquals("Bob", dataProducts.getPrincipalFirstName());
        assertEquals("Smith", dataProducts.getPrincipalLastName());
        assertEquals(2, dataProducts.getQualityFlagCodes().size());
        assertThat(dataProducts.getQualityFlagCodes(), containsInAnyOrder("flag1", "flag2"));
        assertEquals(12345, dataProducts.getSbid().intValue());
        boolean hasSpectra = false;
        boolean hasMomentMap = false;
        boolean hasCubeletMap = false;
        for (DataProductDTO dataProduct : dataProducts.getDataProducts())
        {
            switch (dataProduct.getType())
            {
            case CATALOGUE:
                assertEquals(12L, dataProduct.getId());
                assertEquals(CatalogueType.CONTINUUM_COMPONENT.name(), dataProduct.getSubType());
                assertEquals(QualityLevel.NOT_VALIDATED, dataProduct.getQualityLevel());
                assertEquals("catalogue-file-id", dataProduct.getIdentifier());
                break;
            case MEASUREMENT_SET:
                assertEquals(16L, dataProduct.getId());
                assertNull(dataProduct.getSubType());
                assertEquals(QualityLevel.NOT_VALIDATED, dataProduct.getQualityLevel());
                assertEquals("measurementset-file-id", dataProduct.getIdentifier());
                break;
            case IMAGE_CUBE:
                assertEquals(19L, dataProduct.getId());
                assertEquals("cont_cleanmodel_T0", dataProduct.getSubType());
                assertEquals(QualityLevel.NOT_VALIDATED, dataProduct.getQualityLevel());
                assertEquals("imagecube-file-id", dataProduct.getIdentifier());
                break;
            case SPECTRUM:
                assertEquals(25L, dataProduct.getId());
                assertEquals("spectral_noise_restored", dataProduct.getSubType());
                assertEquals(QualityLevel.NOT_VALIDATED, dataProduct.getQualityLevel());
                assertEquals("spectrum-file-id", dataProduct.getIdentifier());
                hasSpectra = true;
                break;
            case MOMENT_MAP:
                assertEquals(17L, dataProduct.getId());
                assertEquals("spectral_restored_mom1", dataProduct.getSubType());
                assertEquals(QualityLevel.NOT_VALIDATED, dataProduct.getQualityLevel());
                assertEquals("moment-map-file-id", dataProduct.getIdentifier());
                hasMomentMap = true;
            	break;
            case CUBELET:
                assertEquals(17L, dataProduct.getId());
                assertEquals("spectral_restored_3d", dataProduct.getSubType());
                assertEquals(QualityLevel.NOT_VALIDATED, dataProduct.getQualityLevel());
                assertEquals("cubelet-file-id", dataProduct.getIdentifier());
                hasCubeletMap = true;
            	break;
            default:
                fail();
                break;
            }
        }
        assertEquals("Content for note 1", dataProducts.getValidationNotes().get(0).getContent());
        assertEquals("Content for note 2", dataProducts.getValidationNotes().get(1).getContent());
        assertEquals(2, dataProducts.getValidationNotes().size());
        assertTrue("Should have spectra", hasSpectra);
        assertTrue("Should have moment maps", hasMomentMap);
        assertTrue("Should have cubelets", hasCubeletMap);
    }
    
}