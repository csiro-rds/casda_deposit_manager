package au.csiro.casda.datadeposit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import au.csiro.casda.datadeposit.DepositState.Type;

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
 * Helper static methods to assert state.
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
public class StateChecks
{
    /**
     * Check is in initial state
     * 
     * @param depositableArtefact
     *            is what to check is in that state
     */
    public static void checkStateIsInitial(DepositableArtefact depositableArtefact)
    {
        assertThat(depositableArtefact.isNewDeposit(), is(true));
        assertThat(depositableArtefact.isDepositing(), is(false));
        assertThat(depositableArtefact.isProcessing(), is(false));
        assertThat(depositableArtefact.isProcessed(), is(false));
        assertThat(depositableArtefact.isArchiving(), is(false));
        assertThat(depositableArtefact.isArchived(), is(false));
        assertThat(depositableArtefact.isStaging(), is(false));
        assertThat(depositableArtefact.isStaged(), is(false));
        assertThat(depositableArtefact.isRegistering(), is(false));
        assertThat(depositableArtefact.isRegistered(), is(false));
        assertThat(depositableArtefact.isDeposited(), is(false));
        assertThat(depositableArtefact.isFailedDeposit(), is(false));
        assertThat(depositableArtefact.getCheckpointStateType(), is(Type.UNDEPOSITED));
    }

    public static void checkStateIsUndeposited(DepositableArtefact depositableArtefact)
    {
        checkStateIsInitial(depositableArtefact);
    }

    /**
     * Check is in processing state
     * 
     * @param depositableArtefact
     *            is what to check is in that state
     */
    public static void checkStateIsProcessing(DepositableArtefact depositableArtefact)
    {
        assertThat(depositableArtefact.isNewDeposit(), is(false));
        assertThat(depositableArtefact.isDepositing(), is(true));
        assertThat(depositableArtefact.isProcessing(), is(true));
        assertThat(depositableArtefact.isProcessed(), is(false));
        assertThat(depositableArtefact.isArchiving(), is(false));
        assertThat(depositableArtefact.isArchived(), is(false));
        assertThat(depositableArtefact.isStaging(), is(false));
        assertThat(depositableArtefact.isStaged(), is(false));
        assertThat(depositableArtefact.isRegistering(), is(false));
        assertThat(depositableArtefact.isRegistered(), is(false));
        assertThat(depositableArtefact.isDeposited(), is(false));
        assertThat(depositableArtefact.isFailedDeposit(), is(false));
        assertThat(depositableArtefact.getCheckpointStateType(), is(Type.PROCESSING));
    }

    /**
     * Check is in processed state
     * 
     * @param depositableArtefact
     *            is what to check is in that state
     */
    public static void checkStateIsProcessed(DepositableArtefact depositableArtefact)
    {
        assertThat(depositableArtefact.isNewDeposit(), is(false));
        assertThat(depositableArtefact.isDepositing(), is(true));
        assertThat(depositableArtefact.isProcessing(), is(false));
        assertThat(depositableArtefact.isProcessed(), is(true));
        assertThat(depositableArtefact.isArchiving(), is(false));
        assertThat(depositableArtefact.isArchived(), is(false));
        assertThat(depositableArtefact.isStaging(), is(false));
        assertThat(depositableArtefact.isStaged(), is(false));
        assertThat(depositableArtefact.isRegistering(), is(false));
        assertThat(depositableArtefact.isRegistered(), is(false));
        assertThat(depositableArtefact.isDeposited(), is(false));
        assertThat(depositableArtefact.isFailedDeposit(), is(false));
        assertThat(depositableArtefact.getCheckpointStateType(), is(Type.PROCESSED));
    }

    /**
     * Check is in staging state
     * 
     * @param depositableArtefact
     *            is what to check is in that state
     */
    public static void checkStateIsStaging(DepositableArtefact depositableArtefact)
    {
        assertThat(depositableArtefact.isNewDeposit(), is(false));
        assertThat(depositableArtefact.isDepositing(), is(true));
        assertThat(depositableArtefact.isProcessing(), is(false));
        assertThat(depositableArtefact.isProcessed(), is(false));
        assertThat(depositableArtefact.isArchiving(), is(false));
        assertThat(depositableArtefact.isArchived(), is(false));
        assertThat(depositableArtefact.isStaging(), is(true));
        assertThat(depositableArtefact.isStaged(), is(false));
        assertThat(depositableArtefact.isRegistering(), is(false));
        assertThat(depositableArtefact.isRegistered(), is(false));
        assertThat(depositableArtefact.isDeposited(), is(false));
        assertThat(depositableArtefact.isFailedDeposit(), is(false));
        assertThat(depositableArtefact.getCheckpointStateType(), is(Type.STAGING));
    }

    /**
     * Check is in staged state
     * 
     * @param depositableArtefact
     *            is what to check is in that state
     */
    public static void checkStateIsStaged(DepositableArtefact depositableArtefact)
    {
        assertThat(depositableArtefact.isNewDeposit(), is(false));
        assertThat(depositableArtefact.isDepositing(), is(true));
        assertThat(depositableArtefact.isProcessing(), is(false));
        assertThat(depositableArtefact.isProcessed(), is(false));
        assertThat(depositableArtefact.isArchiving(), is(false));
        assertThat(depositableArtefact.isArchived(), is(false));
        assertThat(depositableArtefact.isStaging(), is(false));
        assertThat(depositableArtefact.isStaged(), is(true));
        assertThat(depositableArtefact.isRegistering(), is(false));
        assertThat(depositableArtefact.isRegistered(), is(false));
        assertThat(depositableArtefact.isDeposited(), is(false));
        assertThat(depositableArtefact.isFailedDeposit(), is(false));
        assertThat(depositableArtefact.getCheckpointStateType(), is(Type.STAGING));
    }

    /**
     * Check is in Registering state
     * 
     * @param depositableArtefact
     *            is what to check is in that state
     */
    public static void checkStateIsRegistering(DepositableArtefact depositableArtefact)
    {
        assertThat(depositableArtefact.isNewDeposit(), is(false));
        assertThat(depositableArtefact.isDepositing(), is(true));
        assertThat(depositableArtefact.isProcessing(), is(false));
        assertThat(depositableArtefact.isProcessed(), is(false));
        assertThat(depositableArtefact.isArchiving(), is(false));
        assertThat(depositableArtefact.isArchived(), is(false));
        assertThat(depositableArtefact.isStaging(), is(false));
        assertThat(depositableArtefact.isStaged(), is(false));
        assertThat(depositableArtefact.isRegistering(), is(true));
        assertThat(depositableArtefact.isRegistered(), is(false));
        assertThat(depositableArtefact.isDeposited(), is(false));
        assertThat(depositableArtefact.isFailedDeposit(), is(false));
        assertThat(depositableArtefact.getCheckpointStateType(), is(Type.STAGING));
    }

    /**
     * Check is in Registered state
     * 
     * @param depositableArtefact
     *            is what to check is in that state
     */
    public static void checkStateIsRegistered(DepositableArtefact depositableArtefact)
    {
        assertThat(depositableArtefact.isNewDeposit(), is(false));
        assertThat(depositableArtefact.isDepositing(), is(true));
        assertThat(depositableArtefact.isProcessing(), is(false));
        assertThat(depositableArtefact.isProcessed(), is(false));
        assertThat(depositableArtefact.isArchiving(), is(false));
        assertThat(depositableArtefact.isArchived(), is(false));
        assertThat(depositableArtefact.isStaging(), is(false));
        assertThat(depositableArtefact.isStaged(), is(false));
        assertThat(depositableArtefact.isRegistering(), is(false));
        assertThat(depositableArtefact.isRegistered(), is(true));
        assertThat(depositableArtefact.isDeposited(), is(false));
        assertThat(depositableArtefact.isFailedDeposit(), is(false));
        assertThat(depositableArtefact.getCheckpointStateType(), is(Type.REGISTERED));
    }

    /**
     * Check is in Archiving state
     * 
     * @param depositableArtefact
     *            is what to check is in that state
     */
    public static void checkStateIsArchiving(DepositableArtefact depositableArtefact)
    {
        assertThat(depositableArtefact.isNewDeposit(), is(false));
        assertThat(depositableArtefact.isDepositing(), is(true));
        assertThat(depositableArtefact.isProcessing(), is(false));
        assertThat(depositableArtefact.isProcessed(), is(false));
        assertThat(depositableArtefact.isArchiving(), is(true));
        assertThat(depositableArtefact.isArchived(), is(false));
        assertThat(depositableArtefact.isStaging(), is(false));
        assertThat(depositableArtefact.isStaged(), is(false));
        assertThat(depositableArtefact.isRegistering(), is(false));
        assertThat(depositableArtefact.isRegistered(), is(false));
        assertThat(depositableArtefact.isDeposited(), is(false));
        assertThat(depositableArtefact.isFailedDeposit(), is(false));
        assertThat(depositableArtefact.getCheckpointStateType(), is(Type.ARCHIVING));
    }

    /**
     * Check is in Archived state
     * 
     * @param depositableArtefact
     *            is what to check is in that state
     */
    public static void checkStateIsArchived(DepositableArtefact depositableArtefact)
    {
        assertThat(depositableArtefact.isNewDeposit(), is(false));
        assertThat(depositableArtefact.isDepositing(), is(true));
        assertThat(depositableArtefact.isProcessing(), is(false));
        assertThat(depositableArtefact.isProcessed(), is(false));
        assertThat(depositableArtefact.isArchiving(), is(false));
        assertThat(depositableArtefact.isArchived(), is(true));
        assertThat(depositableArtefact.isStaging(), is(false));
        assertThat(depositableArtefact.isStaged(), is(false));
        assertThat(depositableArtefact.isRegistering(), is(false));
        assertThat(depositableArtefact.isRegistered(), is(false));
        assertThat(depositableArtefact.isDeposited(), is(false));
        assertThat(depositableArtefact.isFailedDeposit(), is(false));
        assertThat(depositableArtefact.getCheckpointStateType(), is(Type.ARCHIVED));
    }

    /**
     * Check is in deposited state
     * 
     * @param depositableArtefact
     *            is what to check is in that state
     */
    public static void checkStateIsDeposited(DepositableArtefact depositableArtefact)
    {
        assertThat(depositableArtefact.isNewDeposit(), is(false));
        assertThat(depositableArtefact.isDepositing(), is(false));
        assertThat(depositableArtefact.isProcessing(), is(false));
        assertThat(depositableArtefact.isProcessed(), is(false));
        assertThat(depositableArtefact.isArchiving(), is(false));
        assertThat(depositableArtefact.isArchived(), is(false));
        assertThat(depositableArtefact.isStaging(), is(false));
        assertThat(depositableArtefact.isStaged(), is(false));
        assertThat(depositableArtefact.isRegistering(), is(false));
        assertThat(depositableArtefact.isRegistered(), is(false));
        assertThat(depositableArtefact.isDeposited(), is(true));
        assertThat(depositableArtefact.isFailedDeposit(), is(false));
        assertThat(depositableArtefact.getCheckpointStateType(), is(Type.DEPOSITED));
    }

    /**
     * Check is in failed state
     * 
     * @param depositableArtefact
     *            is what to check is in that state
     */
    public static void checkStateIsFailed(DepositableArtefact depositableArtefact)
    {
        assertThat(depositableArtefact.isNewDeposit(), is(false));
        assertThat(depositableArtefact.isDepositing(), is(false));
        assertThat(depositableArtefact.isProcessing(), is(false));
        assertThat(depositableArtefact.isProcessed(), is(false));
        assertThat(depositableArtefact.isArchiving(), is(false));
        assertThat(depositableArtefact.isArchived(), is(false));
        assertThat(depositableArtefact.isStaging(), is(false));
        assertThat(depositableArtefact.isStaged(), is(false));
        assertThat(depositableArtefact.isRegistering(), is(false));
        assertThat(depositableArtefact.isRegistered(), is(false));
        assertThat(depositableArtefact.isDeposited(), is(false));
        assertThat(depositableArtefact.isFailedDeposit(), is(true));
        assertThat(depositableArtefact.getCheckpointStateType(), is(not(Type.FAILED)));
    }
}
