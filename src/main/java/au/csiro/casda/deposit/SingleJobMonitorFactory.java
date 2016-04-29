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


import org.springframework.stereotype.Component;

import au.csiro.casda.jobmanager.SingleJobMonitor;

/**
 * Injected factory to create single job monitor instances. (Useful for testing purposes.) 
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
@Component
public class SingleJobMonitorFactory
{

    /**
     * Creates a new Single Job Monitor
     * 
     * @return new Single Job Monitor
     */
    public SingleJobMonitor createSingleJobMonitor()
    {
        return new SingleJobMonitor();
    }
}
