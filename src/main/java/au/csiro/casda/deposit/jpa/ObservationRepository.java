package au.csiro.casda.deposit.jpa;

import java.util.EnumSet;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.dto.ObservationProjectDataProductsDTO;
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
 * JPA Repository declaration.
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
@Repository
public interface ObservationRepository extends CrudRepository<Observation, Long>
{
    /**
     * Returns an Observation with the given sbid
     * 
     * @param sbid
     *            the sbid to search for
     * @return the Observation with the given sbid, or null if there is no matching observation
     */
    public Observation findBySbid(Integer sbid);

    /**
     * Returns a list of project blocks (opal code, sbid combinations) associated with the given project with image
     * cubes that were released after the given date
     * 
     * @param opalCode
     *            the opal project code
     * @param date
     *            the date to match on - note that we are using {@link DateTime} which includes timezone information;
     *            timezone should be UTC
     * @return the list of project blocks.
     */
    @Query("SELECT distinct new au.csiro.casda.dto.ObservationProjectDataProductsDTO(o.sbid, ic.project.opalCode, "
            + "o.obsStart, ic.project.principalFirstName, ic.project.principalLastName) FROM Observation o, "
            + "ImageCube ic WHERE ic.releasedDate > :date AND "
            + "observation_id = o.id AND ic.project.opalCode = :opalCode")
    public List<ObservationProjectDataProductsDTO> findProjectBlocksWithReleasedImageCubesAfterDate(
            @Param("opalCode") String opalCode, @Param("date") DateTime date);

    /**
     * Returns a list of project blocks (opal code, sbid combinations) associated with the given project with
     * measurement sets that were released after the given date
     * 
     * @param opalCode
     *            the opal project code
     * @param date
     *            the date to match on - note that we are using {@link DateTime} which includes timezone information;
     *            timezone should be UTC
     * @return the list of project blocks.
     */
    @Query("SELECT distinct new au.csiro.casda.dto.ObservationProjectDataProductsDTO(o.sbid, ms.project.opalCode, "
            + "o.obsStart, ms.project.principalFirstName, ms.project.principalLastName) FROM Observation o, "
            + "MeasurementSet ms WHERE ms.releasedDate > :date AND "
            + "observation_id = o.id AND ms.project.opalCode = :opalCode")
    public List<ObservationProjectDataProductsDTO> findProjectBlocksWithReleasedMeasurementSetsAfterDate(
            @Param("opalCode") String opalCode, @Param("date") DateTime date);

    /**
     * Returns a list of project blocks (opal code, sbid combinations) associated with the given project with catalogues
     * that were released after the given date
     * 
     * @param opalCode
     *            the opal project code
     * @param date
     *            the date to match on - note that we are using {@link DateTime} which includes timezone information;
     *            timezone should be UTC
     * @return the list of project blocks.
     */
    @Query("SELECT distinct new au.csiro.casda.dto.ObservationProjectDataProductsDTO(o.sbid, cat.project.opalCode, "
            + "o.obsStart, cat.project.principalFirstName, cat.project.principalLastName) FROM Observation o, "
            + "Catalogue cat WHERE cat.releasedDate > :date AND "
            + "observation_id = o.id AND cat.project.opalCode = :opalCode")
    public List<ObservationProjectDataProductsDTO> findProjectBlocksWithReleasedCataloguesAfterDate(
            @Param("opalCode") String opalCode, @Param("date") DateTime date);

    /**
     * Gets observations that are in specific states.
     * 
     * @param depositStateTypeSet
     *            the set of DepositState.Type to search on.
     * @return a List of observations
     */
    @Query("select obs from Observation obs where depositStateType IN :typeList")
    List<Observation> findObservationsByDepositStateType(
            @Param("typeList") EnumSet<DepositState.Type> depositStateTypeSet);

    /**
     * Gets observations that were completed since the cutoff date.
     * 
     * @param recentCutoff
     *            The earliest completed date to be included.
     * @return a List of observations
     */
    @Query("select obs from Observation obs where depositStateType = 'DEPOSITED' AND " + " depositStateChanged >= ?")
    public List<Observation> findObservationsCompletedSince(DateTime recentCutoff);

    /**
     * Gets observations by that are currently being deposited, ie. not in DEPOSITED or FAILED states
     * 
     * @return A List of observations that are currently depositing.
     */
    @Query("FROM Observation WHERE deposit_state <> 'DEPOSITED' and deposit_state <> 'FAILED' order by id asc")
    public List<Observation> findDepositingObservations();

    /**
     * Finds the earliest observation start date for a given project code.
     * 
     * @param opalCode
     *            the project code
     * @return the earliest observation start date (with timezone) for a given project code.
     */
    @Query("SELECT min(o.obsStart) FROM Observation o JOIN o.projects p WHERE p.opalCode = :opalCode")
    public DateTime findEarliestObservationStartDateForProject(@Param("opalCode") String opalCode);

    /**
     * Finds the deposited project blocks with unreleased image cubes.
     * 
     * @return the list of project blocks (sbid, opal code combinations) with unreleased image cubes
     */
    @Query("SELECT distinct new au.csiro.casda.dto.ObservationProjectDataProductsDTO(o.sbid, ic.project.opalCode, "
            + "o.obsStart, ic.project.principalFirstName, ic.project.principalLastName) FROM Observation o, "
            + "ImageCube ic WHERE ic.releasedDate is null AND observation_id = o.id "
            + "AND o.depositStateType = 'DEPOSITED'")
    public List<ObservationProjectDataProductsDTO> findDepositedProjectBlocksWithUnreleasedImageCubes();

    /**
     * Finds the deposited project blocks with unreleased catalogues.
     * 
     * @return the list of project blocks (sbid, opal code combinations) with unreleased catalogues
     */
    @Query("SELECT distinct new au.csiro.casda.dto.ObservationProjectDataProductsDTO(o.sbid, cat.project.opalCode, "
            + "o.obsStart, cat.project.principalFirstName, cat.project.principalLastName) FROM Observation o, "
            + "Catalogue cat WHERE cat.releasedDate is null AND observation_id = o.id "
            + "AND o.depositStateType = 'DEPOSITED'")
    public List<ObservationProjectDataProductsDTO> findDepositedProjectBlocksWithUnreleasedCatalogues();

    /**
     * Finds the deposited project blocks with unreleased measurement sets.
     * 
     * @return the list of project blocks (sbid, opal code combinations) with unreleased measurement sets
     */
    @Query("SELECT distinct new au.csiro.casda.dto.ObservationProjectDataProductsDTO(o.sbid, ms.project.opalCode, "
            + "o.obsStart, ms.project.principalFirstName, ms.project.principalLastName) FROM Observation o, "
            + "MeasurementSet ms WHERE ms.releasedDate is null AND observation_id = o.id "
            + "AND o.depositStateType = 'DEPOSITED'")
    public List<ObservationProjectDataProductsDTO> findDepositedProjectBlocksWithUnreleasedMeasurementSets();

}
