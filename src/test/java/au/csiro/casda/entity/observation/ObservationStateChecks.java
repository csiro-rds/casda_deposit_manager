package au.csiro.casda.entity.observation;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import au.csiro.casda.entity.CasdaDepositableEntity;

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
 * Helper static methods to assert state of CasdaDepositableEntity (Observation).
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
public class ObservationStateChecks
{
    /**
     * Check is in initial state
     * 
     * @param depositableArtefact
     *            to check state of
     * */
    public static void checkStateIsInitial(CasdaDepositableEntity depositableArtefact)
    {
        assertThat(depositableArtefact.isNewDeposit(), is(true));
        assertThat(depositableArtefact.isPriorityDepositing(), is(false));
        assertThat(depositableArtefact.isDepositing(), is(false));
        assertThat(depositableArtefact.isDeposited(), is(false));
        assertThat(depositableArtefact.isFailedDeposit(), is(false));
    }

    public static void checkStateIsUndeposited(CasdaDepositableEntity depositableArtefact)
    {
        checkStateIsInitial(depositableArtefact);
    }

    /**
     * Check is in depositing state
     * 
     * @param depositableArtefact
     *            to check state of
     * */
    public static void checkStateIsDepositing(CasdaDepositableEntity depositableArtefact)
    {
        assertThat(depositableArtefact.isNewDeposit(), is(false));
        assertThat(depositableArtefact.isPriorityDepositing(), is(false));
        assertThat(depositableArtefact.isDepositing(), is(true));
        assertThat(depositableArtefact.isDeposited(), is(false));
        assertThat(depositableArtefact.isFailedDeposit(), is(false));
    }
    
    /**
     * Check is in priority depositing state
     * 
     * @param depositableArtefact
     *            to check state of
     * */
    public static void checkStateIsPriorityDepositing(CasdaDepositableEntity depositableArtefact)
    {
        assertThat(depositableArtefact.isNewDeposit(), is(false));
        assertThat(depositableArtefact.isPriorityDepositing(), is(true));
        assertThat(depositableArtefact.isDepositing(), is(false));
        assertThat(depositableArtefact.isDeposited(), is(false));
        assertThat(depositableArtefact.isFailedDeposit(), is(false));
    }

    /**
     * Check is in deposited state
     * 
     * @param depositableArtefact
     *            to check state of
     * */
    public static void checkStateIsDeposited(CasdaDepositableEntity depositableArtefact)
    {
        assertThat(depositableArtefact.isNewDeposit(), is(false));
        assertThat(depositableArtefact.isPriorityDepositing(), is(false));
        assertThat(depositableArtefact.isDepositing(), is(false));
        assertThat(depositableArtefact.isDeposited(), is(true));
        assertThat(depositableArtefact.isFailedDeposit(), is(false));
    }

    /**
     * Check is in failed state
     * 
     * @param depositableArtefact
     *            to check state of
     * */
    public static void checkStateIsFailed(CasdaDepositableEntity depositableArtefact)
    {
        assertThat(depositableArtefact.isNewDeposit(), is(false));
        assertThat(depositableArtefact.isPriorityDepositing(), is(false));
        assertThat(depositableArtefact.isDepositing(), is(false));
        assertThat(depositableArtefact.isDeposited(), is(false));
        assertThat(depositableArtefact.isFailedDeposit(), is(true));
    }

    /**
     * Check is in notifying state
     * 
     * @param depositableArtefact
     *            to check state of
     * */
    public static void checkStateIsNotifying(CasdaDepositableEntity depositableArtefact)
    {
        assertThat(depositableArtefact.isNewDeposit(), is(false));
        assertThat(depositableArtefact.isPriorityDepositing(), is(false));
        assertThat(depositableArtefact.isDepositing(), is(false));
        assertThat(((Observation) depositableArtefact).isNotifying(), is(true));
        assertThat(depositableArtefact.isDeposited(), is(false));
        assertThat(depositableArtefact.isFailedDeposit(), is(false));
    }
}
