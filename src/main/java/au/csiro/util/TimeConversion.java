package au.csiro.util;

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


import java.time.Duration;

/**
 * Utility class for dealing with time conversion.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
// TODO: Move this to casda_commons when we can use Java 1.8
public class TimeConversion
{
    private static final double NANOS_IN_A_MILLISECOND = 1.0e6;

    /**
     * Convert a duration to milliseconds
     * 
     * @param timeTaken
     *            the duration to convert
     * @return long Duration rounded to milliseconds
     */
    public static long durationToMillis(Duration timeTaken)
    {
        // Nanos used because they are the finest-grained time unit
        // that Duration can be in. We do a manual conversion to
        // milliseconds because Duration#toMillis effectively truncates
        // any 'remainder' time finer than a millisecond.
        return Math.round(timeTaken.toNanos() / NANOS_IN_A_MILLISECOND);
    }

}
