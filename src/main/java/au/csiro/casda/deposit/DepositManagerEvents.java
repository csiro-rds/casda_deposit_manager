package au.csiro.casda.deposit;

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


import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import au.csiro.casda.logging.CasdaEvent;
import au.csiro.casda.logging.CasdaLogMessageBuilderFactory;

/**
 * The known events for the CASDA Deposit Manager. For more information see
 * https://wiki.csiro.au/display/CASDA/APPENDIX+F:+Events+and+Notifications
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 *
 */
public enum DepositManagerEvents implements CasdaEvent
{

    /**
     * Deposit - Unsuccessful poll
     */
    E013,

    /**
     * Deposit - Polling timeout
     */
    E014,

    /**
     * deposit.job.created
     */
    E035,
    /**
     * level 7 collection deposit job created
     */
    E036,
    /**
     * deposit.step.artifact.progressed
     */
    E050,

    /**
     * deposit.step.failed.controlled
     */
    E052,

    /**
     * deposit.failure.parameters
     */
    E070,

    /**
     * deposit.poll.success
     */
    E072,

    /**
     * Deposit - Unsuccessful data deposit
     */
    E073,

    /**
     * Deposit - Artifact progress timeout
     */
    E074,

    /**
     * Deposit - Status change
     */
    E075,

    /**
     * Deposit - Observation deposit timeout
     */
    E076,

    /**
     * Deposit - Observation complete
     */
    E077,

    /**
     * Invalid observation for release
     */
    E085,

    /**
     * Problem saving changes, can't release observation
     */
    E086,

    /**
     * deposit.stage.ngas.failure
     */
    E088,

    /**
     * Successfully released observation
     */
    E089,

    /**
     * deposit.recover.failure.observationNotFailed
     */
    E094,

    /**
     * deposit.recover.failed
     */
    E096,

    /**
     * deposit.recover.success
     */
    E097,

    /**
     * deposit.recover.failure.observationDoesntExist
     */
    E099,

    /**
     * User do not have access to requested resource
     */
    E107,

    /**
     * level 7 failed deposit
     */
    E114,
    /**
     * level 7 successful deposit
     */
    E115,
    /**
     * level 7 collection deposit progressed
     */
    E119,
    /**
     * level 7 collection's child artefact (catalogue) deposit progressed
     */
    E120,
    /**
     * level 7 collection's child artefact (catalogue) deposit failed
     */
    E121,
    /**
     * connection to ngas lost while depositing level 7 collection's child artefact (catalogue)
     */
    E122,
    /**
     * deposit.level7.recovery.notRequired
     */
    E123,
    /**
     * deposit.level7.recovery.malformedParameters
     */
    E124,
    /**
     * deposit.level7.recovery.failed
     */
    E125,
    /**
     * deposit.level7.recovery.succeeded
     */
    E126,
    /**
     * deposit.level7.recovery.noSuchDeposit
     */
    E127,
    /**
     * recovery isn't required for an observation's artefact 
     */
    E136,
    /**
     * recovery was unsuccessful for an observation's artefact
     */
    E137,
    /**
     * recovery was successful for an observation's artefact
     */
    E138,

    /**
     * Observation failed to be redeposited.
     */
    E152,

    /**
     * Observation redeposit success
     */
    E153,

    /**
     * Failed to refresh observation.
     */
    E154,

    /**
     * Successful refresh of observation
     */
    E155,

    /**
     * Successful bulk refresh of observation metadata
     */
    E156,

    /**
     * Refresh failed
     */
    E157,

    /**
     * Refresh completed
     */
    E158,

    /**
     * Refresh started
     */
    E159;
    

    private static Properties eventProperties = new Properties();

    static
    {
        InputStream propertiesStream =
                Thread.currentThread().getContextClassLoader()
                        .getResourceAsStream("event-casda_deposit_manager.properties");
        try
        {
            eventProperties.load(propertiesStream);
        }
        catch (IOException e)
        {
            throw new RuntimeException("Could not load event properties");
        }

    }

    /**
     * Get the format string. For known events, this is the Standard Content, see:
     * https://wiki.csiro.au/display/CASDA/APPENDIX+F:+Events+and+Notifications
     * 
     * @return standard java format string representing the standard content for this event see
     *         (http://docs.oracle.com/javase/8/docs/api/java/util/Formatter.html)
     */
    @Override
    public String getFormatString()
    {
        return eventProperties.getProperty(this.getCode() + ".description");
    }

    /**
     * Get the type of event. For known events, this is the Event Title, see:
     * https://wiki.csiro.au/display/CASDA/APPENDIX+F:+Events+and+Notifications
     * 
     * @return event title
     */
    @Override
    public String getType()
    {
        return eventProperties.getProperty(this.getCode() + ".title");
    }

    /**
     * Get the Event Code, see: https://wiki.csiro.au/display/CASDA/APPENDIX+F:+Events+and+Notifications
     * 
     * @return event code, eg E001
     */
    @Override
    public String getCode()
    {
        return this.name();
    }

    /**
     * @return a message builder that can be used to build a message of this type
     */
    public CasdaDepositManagerMessageBuilder messageBuilder()
    {
        return new CasdaDepositManagerMessageBuilder(CasdaLogMessageBuilderFactory.getCasdaMessageBuilder(this));
    }

}
