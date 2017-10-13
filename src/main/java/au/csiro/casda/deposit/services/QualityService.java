package au.csiro.casda.deposit.services;

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


import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import au.csiro.casda.deposit.jpa.ObservationProjectQualityFlagRepository;
import au.csiro.casda.deposit.jpa.QualityFlagRepository;
import au.csiro.casda.dto.DataProductDTO;
import au.csiro.casda.entity.ObservationProjectQualityFlag;
import au.csiro.casda.entity.QualityFlag;

/**
 * Service to retrieve and update quality information about data products and scheduling blocks (observation-project
 * combinations)
 *
 * Copyright 2015, CSIRO Australia All rights reserved.
 */
@Service
public class QualityService
{
    private static final Logger logger = LoggerFactory.getLogger(QualityService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private QualityFlagRepository qualityFlagRepository;

    @Autowired
    private ObservationProjectQualityFlagRepository observationProjectQualityFlagRepository;

    /**
     * Retrieves a list of quality flag codes for a given scheduling block (observation-project combination)
     * 
     * @param observationId
     *            the observation id
     * @param projectId
     *            the project id
     * @return the list of quality flag codes
     */
    public List<String> getQualityFlagCodesForObservationProject(long observationId, long projectId)
    {
        return observationProjectQualityFlagRepository.getQualityFlagCodesForObservationProject(observationId,
                projectId);
    }

    /**
     * Updates the quality level for catalogue rows for a given data product.
     * 
     * This uses the JDBC Template which will be faster than JPA to update up to 60,000 rows per catalogue.
     * 
     * This is not using a method in the JPA repository because it needs to dynamically choose the table.
     * 
     * @param dataProduct
     *            this contains details about the catalogue type, the catalogue and the new quality level for the rows
     */
    @Transactional
    public void updateQualityLevelForCatalogueRows(DataProductDTO dataProduct)
    {
        if (dataProduct.getSubType() != null)
        {
            String query =
                    "UPDATE casda." + dataProduct.getSubType().toLowerCase()
                            + " SET quality_level=?, last_modified=? WHERE catalogue_id=?";
            
            // converting quality level to String and last modified to Date because these are recognised
            // sql types.
            int response =
                    jdbcTemplate.update(query, dataProduct.getQualityLevel().name(), DateTime.now(DateTimeZone.UTC)
                            .toDate(), dataProduct.getId());

            logger.info("Updated quality level for {} catalogue rows in {} for catalogue id {} to {}", response,
                    dataProduct.getSubType().toLowerCase(), dataProduct.getId(),
                    dataProduct.getQualityLevel());
        }
    }

    /**
     * Replaces existing quality flags with the given list of codes for a given observation and project
     * 
     * @param observationId
     *            the observation id
     * @param projectId
     *            the project id
     * @param qualityFlagCodes
     *            the list of quality flag codes to add to this observation and project combination - note these must be
     *            active codes
     */
    @Transactional
    public void updateQualityFlagsForObservationProject(long observationId, long projectId,
            List<String> qualityFlagCodes)
    {
        List<ObservationProjectQualityFlag> newFlags = new ArrayList<>();

        for (String qualityFlagCode : qualityFlagCodes)
        {
            QualityFlag activeQualityFlag = qualityFlagRepository.findByCodeAndActive(qualityFlagCode, true);
            if (activeQualityFlag == null)
            {
                throw new IllegalArgumentException("No active flag available for code: " + qualityFlagCode);
            }
            else
            {
                ObservationProjectQualityFlag qualityFlag = new ObservationProjectQualityFlag();
                qualityFlag.setProjectId(projectId);
                qualityFlag.setObservationId(observationId);
                qualityFlag.setQualityFlagId(activeQualityFlag.getId());
                newFlags.add(qualityFlag);
            }
        }

        // delete existing flags
        observationProjectQualityFlagRepository.deleteByProjectIdAndObservationId(projectId, observationId);

        observationProjectQualityFlagRepository.save(newFlags);
    }

    /**
     * Finds the list of active quality flags, ordered by display order.
     * 
     * @return the list of ordered active quality flags
     */
    public List<QualityFlag> findOrderedActiveQualityFlags()
    {
        return qualityFlagRepository.findByActiveOrderByDisplayOrderAsc(true);
    }
}
