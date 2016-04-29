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


import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import au.csiro.casda.logging.CasdaLoggingSettings;

/**
 * Logging aspect - intercepts the CSIRO web endpoints and adds logging information to the MDC.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
@Aspect
@Component
public class LoggingAspect
{
    private CasdaLoggingSettings loggingSettings = 
            new CasdaLoggingSettings(DepositManagerApplication.APPLICATION_NAME);

    /**
     * Around any public csiro method, make sure the logging settings is updated, and an instance id is added. Also log
     * timing information before and after the request call.
     * 
     * @param proceedingJoinPoint
     *            the method that has been intercepted
     * @return Object return result from the method that has been intercepted
     * @throws Throwable
     *             from proceedingJoinPoint.proceed()
     */
    @Around("execution(* au.csiro..*(..))")
    public Object addLoggingContextInformation(ProceedingJoinPoint proceedingJoinPoint) throws Throwable
    {
        loggingSettings.addGeneralLoggingSettings();
        loggingSettings.addLoggingInstanceId();

        return proceedingJoinPoint.proceed();
    }

}
