package au.csiro.casda.deposit.jpa;

import java.util.EnumSet;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import au.csiro.casda.entity.refresh.ObservationRefresh;
import au.csiro.casda.entity.refresh.RefreshStateType;

/**
 * JPA Repository declaration for managing ObservationRefresh objects.
 * <p>
 * Copyright 2017, CSIRO Australia. All rights reserved.
 */
@Repository
public interface ObservationRefreshRepository extends CrudRepository<ObservationRefresh, Long>
{

    /**
     * Retrieve the ObservationRefresh object for an observation where the processing has not yet started.
     * @param observationId The id of the target observation.
     * @return The ObservationRefresh object
     */
    @Query("FROM ObservationRefresh WHERE refreshState = 'UNREFRESHED' and observationId = :observationId")
    public ObservationRefresh findUnprocessedTaskForObservation(@Param("observationId") Long observationId);

    /**
     * @return A list of ids and sbids for observations which do nto have a queued refresh task. 
     */
    @Query("select id, sbid from Observation obs where depositStateType = 'DEPOSITED' and not exists "
            + "(select id from ObservationRefresh obsr "
            + "where obsr.refreshState = 'UNREFRESHED' and obsr.observationId = obs.id)")
    public List<Object[]> findObservationsToRefresh();

    /**
     * @param stateList The list of RefreshStateTypes to find
     * @return A list of all ObservationRefresh objects in the requested states.
     */
    @Query("FROM ObservationRefresh WHERE refreshState in :stateList "
            + "order by refreshState desc, refreshStateChanged asc")
    public List<ObservationRefresh> getRefreshesinStates(@Param("stateList") EnumSet<RefreshStateType> stateList);

    /**
     * @param stateList The list of RefreshStateTypes to find
     * @param recentCutoff The earliest date to be found.
     * @return A list of all ObservationRefresh objects in the requested states.
     */
    @Query("FROM ObservationRefresh WHERE refreshState in :stateList  and refreshStateChanged > :recentCutoff "
            + "order by refreshState desc, refreshStateChanged asc")
    public List<ObservationRefresh> getRefreshesinStatesSince(@Param("stateList") EnumSet<RefreshStateType> stateList,
            @Param("recentCutoff") DateTime recentCutoff);

    /**
     * @param completedCutoff The earliest date to search for.
     * @return The number of ObservationRefresh objects that have been completed since the specified date.
     */
    @Query("SELECT count(*) FROM ObservationRefresh "
            + "WHERE refreshState = 'COMPLETED' and refreshStateChanged > :completedCutoff")
    public int countCompletedRefreshesSince(@Param("completedCutoff") DateTime completedCutoff);
    
    /**
     * Retrieve the oldest ObservationRefresh record in the specified state.
     * @param refreshState The RefreshStateType to find
     * @return The first observation in the state. 
     */
    public ObservationRefresh findFirstByRefreshStateOrderById(RefreshStateType refreshState);

    /**
     * Retrieve the ids of all observations which have been refreshed since the specified date.
     * @param completedCutoff The earliest date to search for.
     * @return The ids of the observations. 
     */
    @Query("SELECT observationId FROM ObservationRefresh "
            + "WHERE refreshState = 'COMPLETED' and refreshStateChanged > :completedCutoff")
    public List<Long> getIdsOfObservationsRefreshedSince(@Param("completedCutoff") DateTime completedCutoff);
}
