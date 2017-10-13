package au.csiro.casda.deposit.jpa;

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


import java.util.EnumSet;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.entity.observation.Level7Collection;


/**
 * JPA Repository for the Level 7 Collection table.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
@Repository
public interface Level7CollectionRepository extends CrudRepository<Level7Collection, Long>
{

    /** 
     * Finds a level 7 collection by its collection id.
     * @param dapCollectionId the dap collection id
     * @return the matching level 7 collection record, or null.
     */
    public Level7Collection findByDapCollectionId(long dapCollectionId);
    
    /**
     * Gets level 7 collections that are currently being deposited, ie. not in DEPOSITED or FAILED states
     * 
     * @return A List of level 7 collections that are currently depositing.
     */
    @Query("FROM Level7Collection WHERE deposit_state NOT IN "
            + "('DEPOSITED', 'FAILED', 'PREPARING', 'INVALID', 'VALID') order by id asc")
    public List<Level7Collection> findDepositingLevel7Collections();
    
    /**
     * Gets level 7 collections that are in specific states.
     * 
     * @param depositStateTypeSet
     *            the set of DepositState.Type to search on.
     * @return a List of level 7 collections
     */
    @Query("select lsev from Level7Collection lsev where depositStateType IN :typeList")
    List<Level7Collection> findLevel7CollectionsByDepositStateType(
            @Param("typeList") EnumSet<DepositState.Type> depositStateTypeSet);

    /**
     * Gets level 7 collections that were completed since the cutoff date.
     * 
     * @param recentCutoff
     *            The earliest completed date to be included.
     * @return a List of level 7 collections
     */
    @Query("select lsev from Level7Collection lsev where depositStateType = 'DEPOSITED' AND " + " depositStateChanged >= ?")
    public List<Level7Collection> findLevel7CollectionsCompletedSince(DateTime recentCutoff);
    
    /**
     * @return The list of level 7 collection currently validating or waiting to start validating
     */
    @Query("FROM Level7Collection WHERE deposit_state IN ('VALIDATING', 'UNVALIDATED', 'VALID') order by id asc")
    public List<Level7Collection> findValidatingLevel7Collections();
}
