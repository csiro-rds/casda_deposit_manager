package au.csiro.casda.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
 * Abstract base class for Data Transfer Objects encapsulating information about a CASDA Depositable.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public abstract class DepositableDTO implements Serializable
{
    private static final long serialVersionUID = 8340133700658899516L;

    private DepositStateDTO depositState;
    
    private String depositableTypeDescription;

    public DepositStateDTO getDepositState()
    {
        return depositState;
    }

    public void setDepositState(DepositStateDTO depositState)
    {
        this.depositState = depositState;
    }
    
    public String getDepositableTypeDescription()
    {
        return depositableTypeDescription;
    }

    public void setDepositableTypeDescription(String depositTypeDescription)
    {
        this.depositableTypeDescription = depositTypeDescription;
    }

    @JsonIgnore
    public boolean isBuildingDeposit()
    {
        return this.depositState == DepositStateDTO.BUILDING_DEPOSIT;
    }

    @JsonIgnore
    public boolean isDeposited()
    {
        return this.depositState == DepositStateDTO.DEPOSITED;
    }

    @JsonIgnore
    public boolean isDepositing()
    {
        return this.depositState == DepositStateDTO.DEPOSITING;
    }

    @JsonIgnore
    public boolean isFailedDeposit()
    {
        return this.depositState == DepositStateDTO.DEPOSIT_FAILED;
    }

}
