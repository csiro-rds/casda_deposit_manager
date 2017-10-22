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

import static org.hamcrest.beans.SamePropertyValuesAs.samePropertyValuesAs;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.isA;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.RandomStringUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import au.csiro.casda.deposit.services.ObservationService;
import au.csiro.casda.dto.ObservationProjectDataProductsDTO;
import au.csiro.casda.dto.QualityFlagDTO;
import au.csiro.casda.dto.ValidationNoteDTO;
import au.csiro.casda.entity.QualityFlag;
import au.csiro.casda.services.dto.Message.MessageCode;
import au.csiro.casda.services.dto.MessageDTO;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Tests the endpoints in the observation controller
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 *
 */
public class ObservationControllerTest
{
    @Mock
    private ObservationService mockObservationService;

    @InjectMocks
    private ObservationController observationController;

    private MockMvc mockMvc;

    /**
     * Sets up the observation controller before each test.
     * 
     * @throws Exception
     *             any exception thrown during setup
     */
    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(observationController).build();
    }

    @Test
    public void testUnsuccessfulProjectBlockReleaseMissingDate() throws Exception
    {
        MessageCode messageCode = MessageCode.SUCCESS;
        String message = "some success message";

        MessageDTO successMessage = new MessageDTO(messageCode, message);
        when(mockObservationService.releaseProjectBlock(any(String.class), any(Integer.class), any(DateTime.class)))
                .thenReturn(successMessage);

        ResultActions resultIsOk = this.mockMvc.perform(put("/observations/123/projects/ABC123/release?date=100"))
                .andDo(print()).andExpect(status().isOk());
        resultIsOk.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        resultIsOk.andExpect(jsonPath("$").value(isA(Map.class)));
        resultIsOk.andExpect(jsonPath("$.messageCode").value(messageCode.toString()));
        resultIsOk.andExpect(jsonPath("$.message").value(message));
    }

    @Test
    public void testSuccessfulProjectBlockReleaseViaEndpoint() throws Exception
    {
        MessageCode messageCode = MessageCode.SUCCESS;
        String message = "some success message";

        MessageDTO successMessage = new MessageDTO(messageCode, message);
        when(mockObservationService.releaseProjectBlock(any(String.class), any(Integer.class), any(DateTime.class)))
                .thenReturn(successMessage);

        ResultActions resultIsOk = this.mockMvc.perform(put("/observations/123/projects/ABC123/release?date=100"))
                .andDo(print()).andExpect(status().isOk());
        resultIsOk.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        resultIsOk.andExpect(jsonPath("$").value(isA(Map.class)));
        resultIsOk.andExpect(jsonPath("$.messageCode").value(messageCode.toString()));
        resultIsOk.andExpect(jsonPath("$.message").value(message));
    }

    @Test
    public void testSuccessfulProjectBlockRelease() throws Exception
    {
        ArgumentCaptor<DateTime> timeCaptor = ArgumentCaptor.forClass(DateTime.class);

        Integer id = Integer.valueOf(143);
        String projectCode = "ABC123";
        MessageDTO successMessage = new MessageDTO(MessageCode.SUCCESS, "some success message");
        when(mockObservationService.releaseProjectBlock(any(String.class), any(Integer.class), any(DateTime.class)))
                .thenReturn(successMessage);

        observationController.releaseProjectBlock(id, projectCode, 100L);

        verify(mockObservationService).releaseProjectBlock(eq(projectCode), eq(id), timeCaptor.capture());

        assertEquals(100L, timeCaptor.getValue().getMillis());
    }

    @Test
    public void testSuccessfulGetReleasedProjectBlocksViaEndpoint() throws Exception
    {
        Set<ObservationProjectDataProductsDTO> projectBlocksOne = new HashSet<>();
        projectBlocksOne.add(new ObservationProjectDataProductsDTO(2, "AS123", DateTime.now(), "Bob", "Smith"));
        projectBlocksOne.add(new ObservationProjectDataProductsDTO(3, "AS123", DateTime.now(), "Bob", "Smith"));
        projectBlocksOne.add(new ObservationProjectDataProductsDTO(467, "AS123", DateTime.now(), "Bob", "Smith"));

        Set<ObservationProjectDataProductsDTO> projectBlocksTwo = new HashSet<>();
        projectBlocksTwo.add(new ObservationProjectDataProductsDTO(5, "AS123", DateTime.now(), "Bob", "Smith"));
        projectBlocksTwo.add(new ObservationProjectDataProductsDTO(6, "AS123", DateTime.now(), "Bob", "Smith"));
        projectBlocksTwo.add(new ObservationProjectDataProductsDTO(221, "AS123", DateTime.now(), "Bob", "Smith"));

        Long date = System.currentTimeMillis() - 200000;
        when(mockObservationService.getReleasedProjectBlocks("AS123", null)).thenReturn(projectBlocksOne);
        when(mockObservationService.getReleasedProjectBlocks("AS123", date)).thenReturn(projectBlocksTwo);

        ResultActions resultIsOk;

        resultIsOk = this.mockMvc.perform(get("/observations/all/projects?state=released&projectCode=AS123"))
                .andExpect(status().isOk());
        resultIsOk.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        resultIsOk.andExpect(jsonPath("$").isArray());
        resultIsOk.andExpect(jsonPath("$").value(hasSize(3)));
        resultIsOk.andExpect(jsonPath("$[*].sbid").value(containsInAnyOrder(2, 3, 467)));
        resultIsOk.andExpect(jsonPath("$[*].opalCode").value(containsInAnyOrder("AS123", "AS123", "AS123")));

        resultIsOk =
                this.mockMvc.perform(get("/observations/all/projects?state=released&projectCode=AS123&date=" + date))
                        .andExpect(status().isOk());
        resultIsOk.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        resultIsOk.andExpect(jsonPath("$").isArray());
        resultIsOk.andExpect(jsonPath("$").value(hasSize(3)));
        resultIsOk.andExpect(jsonPath("$[*].sbid").value(containsInAnyOrder(5, 6, 221)));
        resultIsOk.andExpect(jsonPath("$[*].opalCode").value(containsInAnyOrder("AS123", "AS123", "AS123")));
    }

    @Test
    public void testSuccessfulGetReleasedProjectBlocks() throws Exception
    {
        Long date = System.currentTimeMillis() - 200000;

        ArgumentCaptor<Long> timeCaptor = ArgumentCaptor.forClass(Long.class);

        observationController.getProjectBlocks("released", "AS123", date);

        verify(mockObservationService).getReleasedProjectBlocks(eq("AS123"), timeCaptor.capture());

        assertEquals(date, timeCaptor.getValue());

    }

    @Test
    public void testSuccessfulGetProjectBlocksRefreshed() throws Exception
    {
        Long date = System.currentTimeMillis() - 200000;

        ArgumentCaptor<Long> timeCaptor = ArgumentCaptor.forClass(Long.class);

        observationController.getProjectBlocks("refreshed", "AS123", date);

        verify(mockObservationService).getRefreshedProjectBlocks(timeCaptor.capture());

        assertEquals(date, timeCaptor.getValue());

    }

    @Test
    public void testInvalidDate() throws Exception
    {
        this.mockMvc.perform(get("/observations/all/projects?state=released&projectCode=AS123&date=blah"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testMissingState() throws Exception
    {
        this.mockMvc.perform(get("/observations/all/projects?projectCode=AS123")).andExpect(status().isBadRequest());
    }

    @Test
    public void testGetProjectBlocksWithNothingFound() throws Exception
    {
        when(mockObservationService.getUnreleasedProjectBlocks()).thenReturn(new HashSet<>());

        ResultActions resultIsOk =
                this.mockMvc.perform(get("/observations/all/projects?state=unreleased")).andExpect(status().isOk());
        resultIsOk.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        resultIsOk.andExpect(jsonPath("$").isArray());
        resultIsOk.andExpect(jsonPath("$").value(hasSize(0)));
    }

    @Test
    public void testGetUnreleasedProjectBlocks() throws Exception
    {
        String sbid = "1" + RandomStringUtils.randomNumeric(5);

        Set<ObservationProjectDataProductsDTO> projectBlocks = new HashSet<>();

        ObservationProjectDataProductsDTO project1 =
                new ObservationProjectDataProductsDTO(123, "Project1", DateTime.now(), "Bob", "Smith");
        ObservationProjectDataProductsDTO project2 =
                new ObservationProjectDataProductsDTO(311, "Project2", DateTime.now(), "Bob", "Smith");
        ObservationProjectDataProductsDTO project3 =
                new ObservationProjectDataProductsDTO(123, "Project3", DateTime.now(), "Bob", "Smith");

        projectBlocks.add(project1);
        projectBlocks.add(project2);
        projectBlocks.add(project3);

        when(mockObservationService.getUnreleasedProjectBlocks()).thenReturn(projectBlocks);

        ResultActions resultIsOk = this.mockMvc.perform(get("/observations/all/projects?state=unreleased", sbid))
                .andDo(print()).andExpect(status().isOk());
        resultIsOk.andExpect(content().contentType(MediaType.APPLICATION_JSON));

        resultIsOk.andExpect(jsonPath("$").isArray());
        resultIsOk.andExpect(jsonPath("$").value(hasSize(3)));

        resultIsOk
                .andExpect(jsonPath("$[*][?(@.sbid==123)].opalCode").value(containsInAnyOrder("Project1", "Project3")));
        resultIsOk.andExpect(jsonPath("$[*][?(@.sbid==311)].opalCode").value(containsInAnyOrder("Project2")));
    }

    @Test
    public void testGetQualityFlags() throws Exception
    {
        List<QualityFlag> qualityFlags = new ArrayList<>();
        QualityFlag flag = new QualityFlag();
        flag.setId(1L);
        QualityFlag flag2 = new QualityFlag();
        flag2.setId(2L);
        qualityFlags.add(flag);
        qualityFlags.add(flag2);

        when(mockObservationService.getActiveQualityFlags()).thenReturn(
                qualityFlags.stream().map(qualityFlag -> new QualityFlagDTO(qualityFlag)).collect(Collectors.toList()));

        ResultActions resultIsOk = this.mockMvc.perform(get("/observations/quality_flags")).andExpect(status().isOk());
        resultIsOk.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        resultIsOk.andExpect(jsonPath("$").isArray());
        resultIsOk.andExpect(jsonPath("$").value(hasSize(2)));
        resultIsOk.andExpect(jsonPath("$[0].id").value(1));
        resultIsOk.andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    public void testGetDataProductDetails() throws Exception
    {
        Integer sbid = 12345;
        String opalCode = "ABC123";

        ObservationProjectDataProductsDTO observationProjectDataProducts =
                new ObservationProjectDataProductsDTO(sbid, opalCode, DateTime.now(), "Bob", "Smith");
        when(mockObservationService.getObservationProjectDataProducts(sbid, opalCode))
                .thenReturn(observationProjectDataProducts);

        ResultActions resultIsOk =
                this.mockMvc.perform(get("/observations/{sbid}/projects/{opalCode}/dataproducts", sbid, opalCode))
                        .andExpect(status().isOk());
        resultIsOk.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        resultIsOk.andExpect(jsonPath("$.opalCode").value("ABC123"));
        resultIsOk.andExpect(jsonPath("$.sbid").value(12345));
    }

    @Test
    public void testUpdateDataProductDetailsMismatch() throws Exception
    {
        ObservationProjectDataProductsDTO observationProjectDataProducts =
                new ObservationProjectDataProductsDTO(445, "ABC123", DateTime.now(), "Bob", "Smith");

        ObjectMapper mapper = new ObjectMapper();
        StringWriter sw = new StringWriter();
        mapper.writeValue(sw, observationProjectDataProducts);
        String json = sw.toString();

        this.mockMvc.perform(put("/observation/{sbid}/project/{opalCode}", 201, "ABC122")
                .contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().is4xxClientError());

    }

    @Test
    public void testUpdateDataProductDetailsOk() throws Exception
    {
        ObservationProjectDataProductsDTO observationProjectDataProducts =
                new ObservationProjectDataProductsDTO(445, "ABC123", DateTime.now(), "Bob", "Smith");

        ObjectMapper mapper = new ObjectMapper();
        StringWriter sw = new StringWriter();
        mapper.writeValue(sw, observationProjectDataProducts);
        String json = sw.toString();

        this.mockMvc.perform(put("/observations/{sbid}/projects/{opalCode}/dataproducts", 445, "ABC123")
                .contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isOk());

        ArgumentCaptor<ObservationProjectDataProductsDTO> detailsCaptor =
                ArgumentCaptor.forClass(ObservationProjectDataProductsDTO.class);
        verify(mockObservationService).updateObservationProjectDataProducts(detailsCaptor.capture());

        assertThat(observationProjectDataProducts, samePropertyValuesAs(detailsCaptor.getValue()));
    }

    @Test
    public void testAddValidationNote() throws Exception
    {
        ValidationNoteDTO validationNoteDto = new ValidationNoteDTO();
        validationNoteDto.setContent("Some content");
        validationNoteDto.setCreatedDate(System.currentTimeMillis());
        validationNoteDto.setUserId("user123");
        validationNoteDto.setUserName("Bob Smith");

        ObjectMapper mapper = new ObjectMapper();
        StringWriter sw = new StringWriter();
        mapper.writeValue(sw, validationNoteDto);
        String json = sw.toString();

        ArgumentCaptor<ValidationNoteDTO> validationNoteCaptor = ArgumentCaptor.forClass(ValidationNoteDTO.class);
        when(mockObservationService.addValidationNote(eq(445), eq("ABC123"), validationNoteCaptor.capture()))
                .thenReturn(validationNoteDto);

        ResultActions resultIsOk =
                this.mockMvc.perform(post("/observations/{sbid}/projects/{opalCode}/validationnotes", 445, "ABC123")
                        .contentType(MediaType.APPLICATION_JSON).content(json)).andExpect(status().isOk());
        resultIsOk.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        resultIsOk.andExpect(jsonPath("$").value(isA(Map.class)));
        resultIsOk.andExpect(jsonPath("$.content").value("Some content"));

        assertThat(validationNoteDto, samePropertyValuesAs(validationNoteCaptor.getValue()));
    }

}
