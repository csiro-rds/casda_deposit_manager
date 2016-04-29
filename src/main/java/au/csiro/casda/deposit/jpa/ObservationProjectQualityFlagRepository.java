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


import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import au.csiro.casda.entity.ObservationProjectQualityFlag;

/**
 * JPA Repository for the Observation-Project-Quality Flag join table.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
@Repository
public interface ObservationProjectQualityFlagRepository extends CrudRepository<ObservationProjectQualityFlag, Long>
{
    /**
     * Deletes the existing observation project quality flag records for this observation-project combination
     * 
     * @param projectId
     *            the project id
     * @param observationId
     *            the observation id
     */
    @Transactional
    @Modifying
    @Query("DELETE from ObservationProjectQualityFlag opqf WHERE opqf.projectId = :projectId "
            + "AND opqf.observationId = :observationId")
    public void deleteByProjectIdAndObservationId(@Param("projectId") long projectId,
            @Param("observationId") long observationId);
    
    /**
     * Gets the list of quality flag codes associated with a given observation and project.
     * 
     * @param observationId
     *            the observation id
     * @param projectId
     *            the project id
     * @return the list of quality flag codes associated with a given observation and project
     */
    @Query("SELECT qf.code FROM QualityFlag qf, ObservationProjectQualityFlag opqf WHERE qf.id = opqf.qualityFlagId"
            + " AND opqf.projectId = :projectId AND opqf.observationId = :observationId")
    public List<String> getQualityFlagCodesForObservationProject(@Param("observationId") long observationId,
            @Param("projectId") long projectId);
}