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

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import au.csiro.casda.entity.ValidationNote;

/**
 * JPA Repository for the Validation note table.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
@Repository
public interface ValidationNoteRepository extends CrudRepository<ValidationNote, Long>
{
    /**
     * Finds the list of validation notes for a given project block (sbid and project id combination)
     * 
     * @param sbid
     *            the observation scheduling block id
     * @param projectId
     *            the project id
     * @return the list of validation notes, ordered by ascending created date
     */
    public List<ValidationNote> findBySbidAndProjectIdOrderByCreatedAsc(Integer sbid, Long projectId);

}
