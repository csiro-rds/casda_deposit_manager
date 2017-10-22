package au.csiro.casda.dto;

import static org.junit.Assert.assertThat;

import static org.hamcrest.CoreMatchers.is;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositState.Type;
import au.csiro.casda.datadeposit.Depositable;
import au.csiro.casda.datadeposit.IntermediateDepositState;
import au.csiro.casda.entity.observation.Level7Collection;

/*
 * #%L
 * CSIRO Data Access Portal
 * %%
 * Copyright (C) 2010 - 2017 Commonwealth Scientific and Industrial Research Organisation (CSIRO) ABN 41 687 119 230.
 * %%
 * Licensed under the CSIRO Open Source License Agreement (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License in the LICENSE file.
 * #L%
 */

/**
 * test class for DepositStateDTO
 * <p>
 * Copyright 2017, CSIRO Australia. All rights reserved.
 */
public class DepositStateDTOTest
{

    @Mock
    private Depositable depositable;
    
    @Before
    public void setup()
    {
        MockitoAnnotations.initMocks(this);
    }
    
    @Test
    public void testValueForDepositable()
    {
        Level7Collection l7c = new Level7Collection();
        DepositState state = new IntermediateDepositState(Type.VALIDATING, null, l7c);
        l7c.setDepositState(state);
        assertThat(DepositStateDTO.valueForDepositable(l7c), is(DepositStateDTO.VALIDATING));
        assertThat(l7c.getDepositFailureCount(), is(0));

        state = new IntermediateDepositState(Type.UNVALIDATED, null, l7c);
        l7c.setDepositState(state);
        assertThat(DepositStateDTO.valueForDepositable(l7c), is(DepositStateDTO.VALIDATING));
        assertThat(l7c.getDepositFailureCount(), is(0));

        state = new IntermediateDepositState(Type.VALID, null, l7c);
        l7c.setDepositState(state);
        assertThat(DepositStateDTO.valueForDepositable(l7c), is(DepositStateDTO.VALID));
        assertThat(l7c.getDepositFailureCount(), is(0));

        state = new IntermediateDepositState(Type.INVALID, null, l7c);
        l7c.setDepositState(state);
        assertThat(DepositStateDTO.valueForDepositable(l7c), is(DepositStateDTO.INVALID));
        assertThat(l7c.getDepositFailureCount(), is(1));

        state = new IntermediateDepositState(Type.PREPARING, null, l7c);
        l7c.setDepositState(state);
        assertThat(DepositStateDTO.valueForDepositable(l7c), is(DepositStateDTO.BUILDING_DEPOSIT));
        assertThat(l7c.getDepositFailureCount(), is(1));
    }

    
}
