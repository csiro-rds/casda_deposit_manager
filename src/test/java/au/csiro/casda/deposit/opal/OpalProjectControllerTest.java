package au.csiro.casda.deposit.opal;

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


import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import au.csiro.casda.entity.observation.Project;

/**
 * Tests the Opal Project Controller which implements the endpoint for the OPAL data sync service.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
public class OpalProjectControllerTest
{
    @Mock
    private OpalProjectService mockService;

    @InjectMocks
    private OpalProjectController opalProjectController;

    private MockMvc mockMvc;

    /**
     * Set up the mocks for the controller before each test.
     * 
     * @throws Exception
     *             any exception thrown during set up
     */
    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(opalProjectController).build();
    }

    /**
     * Basic test of the testGetNewProjects endpoint
     * 
     * @throws Exception if something went wrong in the server
     */
    @Test
    public void testGetNewProjects() throws Exception
    {
        List<Map<String, Object>> mockResults = new ArrayList<>();
        Map<String, Object> projectOne = new HashMap<>();
        projectOne.put("opalCode", "A007");
        projectOne.put("earliestObservationDate", 123456);
        mockResults.add(projectOne);
        Map<String, Object> projectTwo = new HashMap<>();
        projectTwo.put("opalCode", "A008");
        projectTwo.put("earliestObservationDate", 654321);
        mockResults.add(projectTwo);
        Mockito.when(mockService.getNewProjects()).thenReturn(mockResults);
        
        ResultActions resultIsOk =
                this.mockMvc.perform(get("/projects?state=new")).andExpect(status().isOk());
        resultIsOk.andExpect(jsonPath("$").isArray());
        resultIsOk.andExpect(jsonPath("$").value(hasSize(2)));
        resultIsOk.andExpect(jsonPath("$[0]['opalCode']").value("A007"));
        resultIsOk.andExpect(jsonPath("$[0]['earliestObservationDate']").value(123456));
        resultIsOk.andExpect(jsonPath("$[1]['opalCode']").value("A008"));
        resultIsOk.andExpect(jsonPath("$[1]['earliestObservationDate']").value(654321));
    }

    /**
     * Basic test of the testGetNewProjects endpoint
     * 
     * @throws Exception if something went wrong in the server
     */
    @Test
    public void testGetNewProjectsNoResults() throws Exception
    {
        Mockito.when(mockService.getNewProjects()).thenReturn(new ArrayList<>());
        
        // This is what we expect to get back in the response body: 
        // a JSON representation of the list (in this case empty)
        String expectedResponse = "[]";
        
        this.mockMvc.perform(get("/projects?state=new")).andExpect(
                content().string(expectedResponse)).andDo(print());
    }
    
    /**
     * Tests the method that marks a project as being known to DAP 
     *
     * @throws Exception if something went wrong in the server
     */
    @Test
    public void testFlagProjectAsKnown() throws Exception
    {
        Project expectedResponse = new Project();
        expectedResponse.setKnownProject(true);
        expectedResponse.setOpalCode("A007");
        expectedResponse.setOpalCode("EMU");
        expectedResponse.setPrincipalFirstName("Bob");
        expectedResponse.setPrincipalLastName("Smith");
        
        Mockito.when(mockService.updateProjectWithOpalData("A007","EMU", "Bob", "Smith")).thenReturn(expectedResponse);
        
        this.mockMvc.perform(put("/projects/A007?shortName=EMU&"
                + "principalFirstName=Bob&principalLastName=Smith")).
            andExpect(status().is2xxSuccessful());
        
        // missing parameters should result in an error
        this.mockMvc.perform(put("/projects?shortName=EMU")).
            andExpect(status().is4xxClientError());
        
        this.mockMvc.perform(put("/projects/A007")).
            andExpect(status().is4xxClientError());
        
        this.mockMvc.perform(put("/projects/")).
            andExpect(status().is4xxClientError());
    }    
    
}
