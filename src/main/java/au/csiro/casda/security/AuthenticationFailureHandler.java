package au.csiro.casda.security;

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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.savedrequest.SavedRequest;

import au.csiro.casda.deposit.DepositManagerEvents;
import au.csiro.casda.logging.CasdaLogMessageBuilderFactory;

/**
 * 
 * Authentication Failure Handler for rest end points
 * 
 * Copyright 2015, CSIRO Australia
 * All rights reserved.
 *
 */
public class AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler
{
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFailureHandler.class);

    /**
     * Construct AuthenticationFailureHandler with failureUrl
     * 
     * @param failureUrl
     *            String url for login fail
     */
    public AuthenticationFailureHandler(String failureUrl)
    {
        super(failureUrl);
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException
    {
        //initial request made to secured resource is cached by spring; fetch it.
        SavedRequest savedRequest = (SavedRequest) request.getSession().getAttribute("SPRING_SECURITY_SAVED_REQUEST");
        
        //savedRequest is null when user is retrying to login or has directly opened the login url
        String loginSuccessUrl = savedRequest != null ? savedRequest.getRedirectUrl() : request.getRequestURL()
                .toString();
        String message = CasdaLogMessageBuilderFactory.getCasdaMessageBuilder(DepositManagerEvents.E107)
                .add(loginSuccessUrl).toString();
        logger.error(message, exception);

        super.onAuthenticationFailure(request, response, exception);
    }
}
