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

import au.csiro.casda.entity.QualityFlag;

/**
 * JPA Repository for the Quality Flag table.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
@Repository
public interface QualityFlagRepository extends CrudRepository<QualityFlag, Long>
{
    /**
     * Finds the list of quality flags whose active flag is set to the given value, ordered by display order.
     * 
     * @param active
     *            the quality flag's active flag matches this value
     * @return the list of matching quality flags
     */
    public List<QualityFlag> findByActiveOrderByDisplayOrderAsc(boolean active);

    /**
     * Finds the quality flag that matches the given code and active status.
     * 
     * @param code
     *            the quality flag code
     * @param active
     *            the active status
     * @return the matching quality flag
     */
    public QualityFlag findByCodeAndActive(String code, boolean active);
}