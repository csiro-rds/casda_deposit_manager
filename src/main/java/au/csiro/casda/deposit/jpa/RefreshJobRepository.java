package au.csiro.casda.deposit.jpa;

import java.util.List;

import org.joda.time.DateTime;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import au.csiro.casda.entity.refresh.RefreshJob;

/**
 * JPA Repository declaration for managing RefreshJob objects.
 * <p>
 * Copyright 2017, CSIRO Australia. All rights reserved.
 */
@Repository
public interface RefreshJobRepository extends CrudRepository<RefreshJob, Long>
{

    /**
     * @return The refresh jobs that have not been completed yet.
     */
    @Query("FROM RefreshJob WHERE jobCompleteTime IS NULL order by jobStartTime asc")
    public List<RefreshJob> getUncompletedRefreshJobs();

    /**
     * @param completedCutoff The earliest date to search for.
     * @return The list of RefreshJob objects that have been completed since the specified date.
     */
    @Query("FROM RefreshJob WHERE jobCompleteTime > :completedCutoff")
    public List<RefreshJob> findRefreshJobsCompletedSince(@Param("completedCutoff") DateTime completedCutoff);
}
