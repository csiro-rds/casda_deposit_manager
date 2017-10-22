package au.csiro.casda.deposit;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import au.csiro.casda.logging.CasdaFormatter;
import au.csiro.casda.logging.CasdaMessageBuilder;
import au.csiro.casda.logging.DataLocation;
import au.csiro.util.TimeConversion;

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
 * CasdaMessageBuilder implementation for Data Deposit messages. Expands on CasdaMessageBuilder by allowing for the
 * additional of a set of optional 'field' values in the message output, eg: invoking {@link #addSource(DataLocation)}
 * will result in the message containing a "[source: &lt;some DataLocation&gt;]". These optional fields are added to the end
 * of the message in the following order:
 * <ol>
 * <li>startTime</li>
 * <li>endTime</li>
 * <li>source</li>
 * <li>volumeKb</li>
 * <li>fileId</li>
 * </ol>
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public class CasdaDepositManagerMessageBuilder implements CasdaMessageBuilder<CasdaDepositManagerMessageBuilder>
{
    /**
     * Internally used enum for dynamically added fields.
     * <p>
     * <em>WARNING:
     * </p>
     * The order of these enum values is crucial due to the way we parse our log files. If you modify it here you will
     * also need to modify casda_elk_config as well.
     */
    private static enum Field
    {
        START_TIME("startTime"), END_TIME("endTime"), SOURCE("source"), DESTINATION("destination"), VOLUME_KB(
                "volumeKB"), FILE_ID("fileId");

        private String name;

        private Field(String name)
        {
            this.name = name;
        }

        private String getName()
        {
            return this.name;
        }
    }

    private CasdaMessageBuilder<?> decoratedBuilder;

    private Map<Field, String> fields;

    private Instant startTime;

    private Instant endTime;

    // Deliberately shadows parent
    private String customMessage;

    /**
     * Constructor.
     * 
     * @param decoratedBuilder
     *            a CasdaMessageBuilder used to handle all the non-field message building.
     */
    public CasdaDepositManagerMessageBuilder(CasdaMessageBuilder<?> decoratedBuilder)
    {
        super();
        this.decoratedBuilder = decoratedBuilder;
        this.fields = new HashMap<>();
    }

    @Override
    public CasdaDepositManagerMessageBuilder add(Date dateTime)
    {
        this.decoratedBuilder.add(dateTime);
        return this;
    }

    @Override
    public CasdaDepositManagerMessageBuilder add(List<Path> files)
    {
        this.decoratedBuilder.add(files);
        return this;
    }

    @Override
    public CasdaDepositManagerMessageBuilder addCustomMessage(String customMessage)
    {
        this.customMessage = customMessage;
        return this;
    }

    @Override
    public CasdaDepositManagerMessageBuilder addTimeTaken(long timeTaken)
    {
        this.decoratedBuilder.addTimeTaken(timeTaken);
        return this;
    }

    @Override
    public CasdaDepositManagerMessageBuilder add(Object object)
    {
        this.decoratedBuilder.add(object);
        return this;
    }

    @Override
    public CasdaDepositManagerMessageBuilder addAll(List<Object> objects)
    {
        this.decoratedBuilder.add(objects);
        return this;
    }

    /**
     * Adds an optional "startTime" field to the message.
     * 
     * @param startTime
     *            an Instant
     * @return the builder object
     */
    public CasdaDepositManagerMessageBuilder addStartTime(Instant startTime)
    {
        this.startTime = startTime;
        this.fields.put(Field.START_TIME, CasdaFormatter.formatDateTimeForLog(Date.from(startTime)));
        return this;
    }

    /**
     * Adds an optional "endTime" field to the message.
     * 
     * @param endTime
     *            an Instant
     * @return the builder object
     */
    public CasdaDepositManagerMessageBuilder addEndTime(Instant endTime)
    {
        this.endTime = endTime;
        this.fields.put(Field.END_TIME, CasdaFormatter.formatDateTimeForLog(Date.from(endTime)));
        return this;
    }

    /**
     * Adds an optional "source" field to the message.
     * 
     * @param source
     *            a DataLocation
     * @return the builder object
     */
    public CasdaDepositManagerMessageBuilder addSource(DataLocation source)
    {
        this.fields.put(Field.SOURCE, source.toString());
        return this;
    }

    /**
     * Adds an optional "destination" field to the message.
     * 
     * @param destination
     *            a DataLocation
     * @return the builder object
     */
    public CasdaDepositManagerMessageBuilder addDestination(DataLocation destination)
    {
        this.fields.put(Field.DESTINATION, destination.toString());
        return this;
    }

    /**
     * Adds an optional "volumeKb" field to the message, where the value in bytes has been converted to kiloBytes
     * (rounding up).
     * 
     * @param volumeInBytes
     *            a long
     * @return the builder object
     */
    public CasdaDepositManagerMessageBuilder addVolumeBytes(long volumeInBytes)
    {
        this.fields.put(Field.VOLUME_KB,
                Long.toString(volumeInBytes / FileUtils.ONE_KB + (volumeInBytes % FileUtils.ONE_KB == 0 ? 0 : 1)));
        return this;
    }

    /**
     * Adds an optional "fileId" field to the message.
     * 
     * @param fileId
     *            a String
     * @return the builder object
     */
    public CasdaDepositManagerMessageBuilder addFileId(String fileId)
    {
        this.fields.put(Field.FILE_ID, fileId);
        return this;
    }

    @Override
    public String toString()
    {
        if (this.startTime != null && this.endTime != null)
        {
            this.addTimeTaken(TimeConversion.durationToMillis(Duration.between(this.startTime, this.endTime)));
        }
        StringBuffer buf = new StringBuffer(this.decoratedBuilder.toString());
        for (Field field : Field.values())
        {
            if (this.fields.containsKey(field))
            {
                buf.append(" [");
                buf.append(field.getName());
                buf.append(": ");
                buf.append(this.fields.get(field));
                buf.append("]");
            }
        }
        if (this.customMessage != null)
        {
            buf.append(" ");
            buf.append(customMessage);
        }
        return buf.toString();
    }
}
