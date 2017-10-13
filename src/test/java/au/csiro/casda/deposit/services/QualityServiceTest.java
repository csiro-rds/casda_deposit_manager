package au.csiro.casda.deposit.services;

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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

import au.csiro.casda.deposit.jpa.ObservationProjectQualityFlagRepository;
import au.csiro.casda.deposit.jpa.QualityFlagRepository;
import au.csiro.casda.dto.DataProductDTO;
import au.csiro.casda.dto.DataProductDTO.DataProductType;
import au.csiro.casda.dto.DataProductDTO.QualityLevel;
import au.csiro.casda.entity.ObservationProjectQualityFlag;
import au.csiro.casda.entity.QualityFlag;
import au.csiro.casda.entity.observation.CatalogueType;

/**
 * Tests the quality service.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public class QualityServiceTest
{

    @Mock
    private QualityFlagRepository qualityFlagRepository;

    @Mock
    private ObservationProjectQualityFlagRepository observationProjectQualityFlagRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private QualityService qualityService;

    @Before
    public void setUp()
    {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetQualityFlagCodesForObservationProject()
    {
        List<String> codes = new ArrayList<>();
        codes.add("PIQ");
        when(observationProjectQualityFlagRepository.getQualityFlagCodesForObservationProject(12L, 17L)).thenReturn(
                codes);

        assertEquals(codes, qualityService.getQualityFlagCodesForObservationProject(12L, 17L));
    }

    @Test
    public void testUpdateQualityLevelForCatalogueRowsNoType()
    {
        DataProductDTO catalogueDataProduct =
                new DataProductDTO(12L, DataProductType.CATALOGUE, null, "file-id", QualityLevel.GOOD);

        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
        when(jdbcTemplate.update(queryCaptor.capture(), eq(QualityLevel.GOOD.name()), any(Date.class), eq(12L)))
                .thenReturn(15);

        qualityService.updateQualityLevelForCatalogueRows(catalogueDataProduct);

        verify(jdbcTemplate, never()).update(any(String.class), any(String.class), any(Date.class), any(Long.class));
    }

    @Test
    public void testUpdateQualityLevelForCatalogueRowsContinuumIsland()
    {
        CatalogueType[] catalogueTypes =
                new CatalogueType[] { CatalogueType.CONTINUUM_COMPONENT, CatalogueType.CONTINUUM_ISLAND,
                        CatalogueType.POLARISATION_COMPONENT };
        String[] tables = new String[] { "continuum_component", "continuum_island", "polarisation_component" };

        for (int i = 0; i < catalogueTypes.length; i++)
        {
            DataProductDTO catalogueDataProduct = new DataProductDTO(12L,
            		DataProductType.CATALOGUE, catalogueTypes[i].name(), "file-id", QualityLevel.GOOD);

            ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);
            when(jdbcTemplate.update(queryCaptor.capture(), eq(QualityLevel.GOOD.name()), any(Date.class), eq(12L)))
                    .thenReturn(15);

            qualityService.updateQualityLevelForCatalogueRows(catalogueDataProduct);
            assertEquals("UPDATE casda." + tables[i] + " SET quality_level=?, last_modified=? WHERE catalogue_id=?",
                    queryCaptor.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void tesUpdateQualityFlagsForObservationProjectInactiveCode()
    {
        List<String> codes = new ArrayList<>();
        codes.add("INACTIVE");
        codes.add("OK");

        QualityFlag ok = new QualityFlag();
        ok.setActive(true);
        ok.setCode("OK");

        doReturn(ok).when(qualityFlagRepository).findByCodeAndActive("OK", true);
        doReturn(null).when(qualityFlagRepository).findByCodeAndActive("INACTIVE", true);

        try
        {
            qualityService.updateQualityFlagsForObservationProject(12L, 18L, codes);
            fail("Should throw an exception");
        }
        catch (IllegalArgumentException e)
        {
            assertEquals("No active flag available for code: INACTIVE", e.getMessage());
            verify(observationProjectQualityFlagRepository, never()).deleteByProjectIdAndObservationId(any(Long.class),
                    any(Long.class));
            verify(observationProjectQualityFlagRepository, never()).save(
                    (List<ObservationProjectQualityFlag>) any(List.class));
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    public void tesUpdateQualityFlagsForObservationProjectActiveCodes()
    {
        List<String> codes = new ArrayList<>();
        codes.add("PIQ");
        codes.add("SB");

        QualityFlag piq = new QualityFlag();
        piq.setActive(true);
        piq.setCode("PIQ");
        piq.setId(2L);

        QualityFlag sb = new QualityFlag();
        sb.setActive(true);
        sb.setCode("SB");
        sb.setId(4L);

        doReturn(sb).when(qualityFlagRepository).findByCodeAndActive("SB", true);
        doReturn(piq).when(qualityFlagRepository).findByCodeAndActive("PIQ", true);

        qualityService.updateQualityFlagsForObservationProject(12L, 18L, codes);

        verify(observationProjectQualityFlagRepository).deleteByProjectIdAndObservationId(eq(18L), eq(12L));
        ArgumentCaptor<List> newFlagCaptor = ArgumentCaptor.forClass(List.class);
        verify(observationProjectQualityFlagRepository).save(
                (List<ObservationProjectQualityFlag>) newFlagCaptor.capture());

        List<ObservationProjectQualityFlag> newFlags = newFlagCaptor.getValue();

        assertEquals(2, newFlags.size());
        ObservationProjectQualityFlag firstNewFlag = newFlags.get(0);
        ObservationProjectQualityFlag secondNewFlag = newFlags.get(1);
        assertEquals(18L, firstNewFlag.getProjectId().longValue());
        assertEquals(18L, secondNewFlag.getProjectId().longValue());
        assertEquals(12L, firstNewFlag.getObservationId().longValue());
        assertEquals(12L, secondNewFlag.getObservationId().longValue());
        assertEquals(2L, firstNewFlag.getQualityFlagId().longValue());
        assertEquals(4L, secondNewFlag.getQualityFlagId().longValue());
    }

    @Test
    public void testFindOrderedActiveQualityFlags()
    {
        QualityFlag piq = new QualityFlag();
        piq.setActive(true);
        piq.setCode("PIQ");
        piq.setId(2L);

        QualityFlag sb = new QualityFlag();
        sb.setActive(true);
        sb.setCode("SB");
        sb.setId(4L);

        List<QualityFlag> activeFlags = new ArrayList<>();
        activeFlags.add(piq);
        activeFlags.add(sb);

        when(qualityFlagRepository.findByActiveOrderByDisplayOrderAsc(eq(true))).thenReturn(activeFlags);

        assertEquals(activeFlags, qualityService.findOrderedActiveQualityFlags());
    }
}
