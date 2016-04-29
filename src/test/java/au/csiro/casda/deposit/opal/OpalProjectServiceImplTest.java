package au.csiro.casda.deposit.opal;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import au.csiro.casda.BadRequestException;
import au.csiro.casda.OpalProjectServiceException;
import au.csiro.casda.ResourceNotFoundException;
import au.csiro.casda.deposit.jpa.ObservationRepository;
import au.csiro.casda.deposit.jpa.ProjectRepository;
import au.csiro.casda.entity.observation.Project;

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
 * Testing OpalProjectServiceImpl
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
public class OpalProjectServiceImplTest
{

    @InjectMocks
    private OpalProjectServiceImpl opalProjectServiceImpl;

    @Mock
    private ProjectRepository projectRepository;
    
    @Mock
    private ObservationRepository observationRepository;

    @Before
    public void setUp() throws Exception
    {
        MockitoAnnotations.initMocks(this);

        when(projectRepository.save(any(Project.class))).then(returnsFirstArg());
    }

    @Test
    public void testGetNewProjectsNoResults() throws Exception
    {
        when(projectRepository.findNewProjectCodes()).thenReturn(null);
        assertThat(opalProjectServiceImpl.getNewProjects(), is(empty()));
        
        when(projectRepository.findNewProjectCodes()).thenReturn(new ArrayList<>());
        assertThat(opalProjectServiceImpl.getNewProjects(), is(empty()));
    }
    
    @Test
    public void testGetNewProjects() throws Exception 
    {
        List<String> projectCodes = new ArrayList<>();
        projectCodes.add("ABC123");
        projectCodes.add("ABC125");
        
        when(projectRepository.findNewProjectCodes()).thenReturn(projectCodes);
        
        when(observationRepository.findEarliestObservationStartDateForProject("ABC123")).thenReturn(new DateTime(123456));
        // this one shouldn't be included
        when(observationRepository.findEarliestObservationStartDateForProject("ABC125")).thenReturn(null);
        
        List<Map<String, Object>> results = opalProjectServiceImpl.getNewProjects();
        
        assertEquals(1, results.size());
        
        Map<String, Object> projectOne = results.get(0);
        assertEquals("ABC123", projectOne.get("opalCode"));
        assertEquals(123456L, projectOne.get("earliestObservationDate"));
    }
    
    @Test
    public void testUpdateProjectWithOpalData() throws ResourceNotFoundException, BadRequestException
    {
        Project matchingProject = new Project();
        matchingProject.setOpalCode("A007");
        when(projectRepository.findByOpalCode("A007")).thenReturn(matchingProject);

        Project response = opalProjectServiceImpl.updateProjectWithOpalData("A007", "EMU", "Bob", "Smith");

        verify(projectRepository, times(1)).save(matchingProject);

        assertTrue(response.getKnownProject());
        assertEquals("A007", response.getOpalCode());
        assertEquals("EMU", response.getShortName());
        assertEquals("Bob", response.getPrincipalFirstName());
        assertEquals("Smith", response.getPrincipalLastName());
    }

    @Test(expected = BadRequestException.class)
    public void testBadRequestNullOpalCode() throws ResourceNotFoundException, BadRequestException
    {
        opalProjectServiceImpl.updateProjectWithOpalData(null, "shortName", "principalFirstName", "principalLastName");
    }

    @Test(expected = BadRequestException.class)
    public void testBadRequestBlankOpalCode() throws ResourceNotFoundException, BadRequestException
    {
        opalProjectServiceImpl.updateProjectWithOpalData(" ", "shortName", "principalFirstName", "principalLastName");
    }

    @Test(expected = BadRequestException.class)
    public void testBadRequestNullShortName() throws ResourceNotFoundException, BadRequestException
    {
        opalProjectServiceImpl.updateProjectWithOpalData("opalCode", null, "principalFirstName", "principalLastName");
    }

    @Test(expected = BadRequestException.class)
    public void testBadRequestBlankShortName() throws ResourceNotFoundException, BadRequestException
    {
        opalProjectServiceImpl.updateProjectWithOpalData("opalCode", " ", "principalFirstName", "principalLastName");
    }

    @Test(expected = ResourceNotFoundException.class)
    public void testResourceNotFoundIfNoProjectMatchesOpalCode() throws ResourceNotFoundException, BadRequestException
    {
        when(projectRepository.findByOpalCode("A007")).thenReturn(null);
        opalProjectServiceImpl.updateProjectWithOpalData("A007", "EMU", "Bob", "Smith");
    }

    @Test(expected = OpalProjectServiceException.class)
    public void testOpalProjectServiceExceptionIfCantSave() throws ResourceNotFoundException, BadRequestException
    {
        Project matchingProject = new Project();
        matchingProject.setOpalCode("A007");
        when(projectRepository.findByOpalCode("A007")).thenReturn(matchingProject);
        when(projectRepository.save(any(Project.class))).thenThrow(new IllegalArgumentException("couldn't save"));

        opalProjectServiceImpl.updateProjectWithOpalData("A007", "EMU", "Bob", "Smith");

        verify(projectRepository, times(1)).save(matchingProject);

    }

}
