package au.csiro.casda;

import java.sql.Types;

import org.hibernate.spatial.dialect.h2geodb.GeoDBDialect;

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
 * Dialect for use in tests with the H2 database. This maps the "OTHER" database type to text (see {@link Types}). It
 * means that we can continue to use the H2 in memory database for our unit tests, even though we are using a pg_sphere
 * spherical polygon (spoly) type in the actual implementation (which comes back from the database as an "OTHER" type).
 * <p>
 * NOTE: reading and writing to the columns that use the spoly type will not work as expected with this dialect.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public class H2SphericalDialect extends GeoDBDialect
{

    private static final long serialVersionUID = -4396082809106275145L;

    public H2SphericalDialect()
    {
        super();
        registerColumnType(Types.OTHER, "varchar");
        registerHibernateType(Types.OTHER, "varchar");
    }
}
