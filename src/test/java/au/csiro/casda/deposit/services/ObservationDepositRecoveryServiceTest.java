package au.csiro.casda.deposit.services;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import au.csiro.casda.datadeposit.ChildDepositableArtefact;
import au.csiro.casda.datadeposit.DepositStateChangeListener;
import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.deposit.exception.ArtefactInvalidStateRecoveryException;
import au.csiro.casda.deposit.exception.ArtefactNotFoundException;
import au.csiro.casda.deposit.exception.ObservationNotFailedRecoveryException;
import au.csiro.casda.deposit.exception.ObservationNotFoundException;
import au.csiro.casda.deposit.jpa.ObservationRepository;
import au.csiro.casda.entity.observation.ImageCube;
import au.csiro.casda.entity.observation.Observation;

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
 * Test the ObservationDepositRecoveryService.
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
public class ObservationDepositRecoveryServiceTest
{

    @Mock
    private ObservationRepository mockObservationRepository;

    @Mock
    private DepositStateFactory depositStateFactory;

    @Mock
    private DepositStateChangeListener depositStateChangeListener;

    @InjectMocks
    private ObservationDepositRecoveryService recoveryService;

    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
    }

    /**
     * Test method for
     * {@link au.csiro.casda.deposit.services.ObservationDepositRecoveryService#recoverObservation(java.lang.Integer)}.
     * 
     * @throws Exception
     */
    @Test(expected = ObservationNotFoundException.class)
    public void testRecoverObservationNotFound() throws Exception
    {
        when(mockObservationRepository.findBySbid(Integer.valueOf(123))).thenReturn(null);
        recoveryService.recoverObservation(123);
    }

    /**
     * Test method for
     * {@link au.csiro.casda.deposit.services.ObservationDepositRecoveryService#recoverObservation(java.lang.Integer)}.
     * 
     * @throws Exception
     */
    @Test(expected = ObservationNotFailedRecoveryException.class)
    public void testRecoverObservationNotFailed() throws Exception
    {
        int sbid = 123;
        Observation depositingObs = new Observation();
        depositingObs.setSbid(sbid);
        when(mockObservationRepository.findBySbid(sbid)).thenReturn(depositingObs);
        recoveryService.recoverObservation(123);
    }

    /**
     * Test method for
     * {@link au.csiro.casda.deposit.services.ObservationDepositRecoveryService#recoverObservation(java.lang.Integer)}.
     * 
     * @throws Exception
     */
    @Test
    public void testRecoverObservation() throws Exception
    {
        int sbid = 123;
        Observation observation = spy(new Observation());
        when(observation.getSbid()).thenReturn(sbid);
        when(observation.isFailedDeposit()).thenReturn(true);
        
        when(mockObservationRepository.findBySbid(sbid)).thenReturn(observation);

        recoveryService.recoverObservation(123);
        verify(observation).recoverDeposit();
    }
    
    @Test(expected = ObservationNotFoundException.class)
    public void testRecoverArtefactObservationNotFound() throws Exception
    {
        when(mockObservationRepository.findBySbid(any(Integer.class))).thenReturn(null);
        
        recoveryService.recoverArtefact(123, "fileId");
    }
    
    @Test(expected = ArtefactInvalidStateRecoveryException.class)
    public void testRecoverArtefactObservationFailed() throws Exception
    {
        int sbid = 123;
        Observation observation = spy(new Observation(sbid));
        when(observation.isFailedDeposit()).thenReturn(true);
        
        when(mockObservationRepository.findBySbid(any(Integer.class))).thenReturn(observation);
        
        recoveryService.recoverArtefact(sbid, "fileId");
    }
    
    @Test(expected = ArtefactNotFoundException.class)
    public void testRecoverArtefactNotFound() throws Exception
    {
        int sbid = 123;
        Observation observation = spy(new Observation());
        when(observation.getSbid()).thenReturn(sbid);
        when(observation.isFailedDeposit()).thenReturn(false);
        when(observation.getDepositableArtefacts()).thenReturn(new HashSet<>());
        
        when(mockObservationRepository.findBySbid(sbid)).thenReturn(observation);
        
        recoveryService.recoverArtefact(sbid, "fileId");
    }
    
    @Test(expected = ArtefactInvalidStateRecoveryException.class)
    public void testRecoverArtefactNotFailed() throws Exception
    {
        int sbid = 123;
        String fileId = "some-file-id.xml";
        Observation observation = spy(new Observation());
        when(observation.getSbid()).thenReturn(sbid);
        when(observation.isFailedDeposit()).thenReturn(false);
        Set<ChildDepositableArtefact> depositableArtefacts = new HashSet<>();
        ImageCube imageCube = spy(new ImageCube());
        doReturn(fileId).when(imageCube).getFileId();
        when(imageCube.isFailedDeposit()).thenReturn(false);
        depositableArtefacts.add(imageCube);
        when(observation.getDepositableArtefacts()).thenReturn(depositableArtefacts);
        
        when(mockObservationRepository.findBySbid(sbid)).thenReturn(observation);
        
        recoveryService.recoverArtefact(sbid, fileId);
    }
    
    @Test
    public void testRecoverArtefact() throws Exception
    {
        int sbid = 123;
        String fileId = "some-file-id.xml";
        Observation observation = spy(new Observation());
        when(observation.getSbid()).thenReturn(sbid);
        when(observation.isFailedDeposit()).thenReturn(false);
        Set<ChildDepositableArtefact> depositableArtefacts = new HashSet<>();
        ImageCube imageCube = spy(new ImageCube());
        doReturn(fileId).when(imageCube).getFileId();
        when(imageCube.isFailedDeposit()).thenReturn(true);
        depositableArtefacts.add(imageCube);
        when(observation.getDepositableArtefacts()).thenReturn(depositableArtefacts);
        
        when(mockObservationRepository.findBySbid(sbid)).thenReturn(observation);
        
        recoveryService.recoverArtefact(sbid, fileId);
        
        verify(observation, never()).recoverDeposit();
        verify(imageCube).recoverDeposit();
    }

}
