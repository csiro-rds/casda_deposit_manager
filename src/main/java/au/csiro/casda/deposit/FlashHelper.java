package au.csiro.casda.deposit;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.RequestContextUtils;

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
 * Helper class used to assist with adding 'flash' messages.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class FlashHelper
{
    /**
     * Adds a 'flash' message to the FlashMap associated with the given request. The 'flash' is a Map<String,
     * String> that is automatically added to the controller Model, and available with the key 'flash'.
     * 
     * @param request
     *            an HttpServletRequest
     * @param key
     *            the key of the message in the 'flash'
     * @param message
     *            the message
     */
    @SuppressWarnings("unchecked")
    protected void flash(HttpServletRequest request, String key, String message)
    {
        if (RequestContextUtils.getOutputFlashMap(request).get("flash") == null)
        {
            RequestContextUtils.getOutputFlashMap(request).put("flash", new HashMap<>());
        }
        ((Map<String, String>) RequestContextUtils.getOutputFlashMap(request).get("flash")).put(key, message);
    }
}
