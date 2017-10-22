package au.csiro.casda.deposit.jdbc;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

/**
 * 
 * Simple JDBC repository for executing arbitrary SQL statements
 * <p>
 * Copyright 2017, CSIRO Australia. All rights reserved.
 */
@Repository
public class SimpleJdbcRepository
{

    private JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource)
    {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    /**
     * Check if this image type should be included in coverage maps.
     * @param imageType The type of the image.
     * @return true if it should be in the map false otherwise.
     */
    public boolean isImageTypeIncludeCoverage(String imageType)
    {
        return this.jdbcTemplate.queryForObject("select include_coverage from casda.image_type where type_name = ?",
                Boolean.class, imageType);
    }

}
