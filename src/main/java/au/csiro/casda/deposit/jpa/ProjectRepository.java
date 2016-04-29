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

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import au.csiro.casda.entity.observation.Project;

/**
 * JPA Repository for the Project table.
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
@Repository
public interface ProjectRepository extends CrudRepository<Project, Long>
{
    /**
     * Gets a list of OPAL codes for projects which have not yet been sync'ed by DAP, and which have at least one fully
     * deposited observation.
     * 
     * @return a List of Strings being the Opal codes of the projects
     */
    @Query("SELECT p.opalCode FROM Project p WHERE (p.knownProject is null OR p.knownProject = false) AND"
            + " exists (SELECT o FROM Observation o join o.projects p WHERE o.depositStateType = 'DEPOSITED')")
    List<String> findNewProjectCodes();

    /**
     * Returns the first Project with the given opalCode
     * 
     * @param opalCode
     *            the opalCode to search for
     * @return a Project
     */
    Project findByOpalCode(String opalCode);

}
