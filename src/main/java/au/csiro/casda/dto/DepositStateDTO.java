package au.csiro.casda.dto;

import au.csiro.casda.datadeposit.Depositable;

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
 * Data Transfer Object encapsulating information about a CASDA Depositable's deposit state
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public enum DepositStateDTO
{
    /**
     * The deposit status used when a ParentDepositableArtefact is being 'built' for a deposit.
     */
    BUILDING_DEPOSIT,

    /**
     * The deposit status used when a Depositable is being deposited.
     */
    DEPOSITING,

    /**
     * The deposit status used when a Depositable has been deposited.
     */
    DEPOSITED,

    /**
     * The deposit status used when the deposit of a Depositable has failed.
     */
    DEPOSIT_FAILED;

    /**
     * @param depositable
     *            a Depositable
     * @return a DepositStateDTO for the given Depositable
     */
    public static DepositStateDTO valueForDepositable(Depositable depositable)
    {
        if (depositable.isFailedDeposit())
        {
            return DEPOSIT_FAILED;
        }
        else if (depositable.isDeposited())
        {
            return DEPOSITED;
        }
        else
        {
            return DEPOSITING;
        }
    }
}
