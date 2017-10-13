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


import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;

import au.csiro.casda.security.SecuredRestTemplate;
import au.csiro.casda.services.dto.MessageDTO;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service to abstract VO TAP calls
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
@Component
public class VoToolsService
{
    private final String voToolsUrl;
    private final SecuredRestTemplate restTemplate;

    private static final Logger logger = LoggerFactory.getLogger(VoToolsService.class);

    /**
     * Constructor
     * 
     * @param voToolsUrl
     *            the VO Tools url
     * @param connectionTimeout
     *            the connection timeout value  
     */
    @Autowired
    public VoToolsService(@Value("${casda.vo.tools.url}") String voToolsUrl, 
    					  @Value("${connection.timeout.limit: " 
    					+ SecuredRestTemplate.DEFAULT_RESTTEMPLATE_CONNECT_TIMEOUT + "}") Integer connectionTimeout)
    {
        super();
        this.restTemplate = createRestTemplate(connectionTimeout);
        this.voToolsUrl = voToolsUrl;
    }

    /**
     * Calls VO Tools to reset the TAP metadata.
     */
    public void resetVoTapMetadata()
    {
        logger.info("Calling VO Tools to reset TAP Metadata");

        URI resetTapMetadataUri = getVoToolsUriBuilder().path("tap").path("reset").build();

        MessageDTO response = restTemplate.getForObject(resetTapMetadataUri, MessageDTO.class);

        logger.info("Finished resetting TAP Metadata. Response: {}", response);
    }

    /**
     * Calls VO Tools to reset the SCS metadata.
     */
    public void resetVoScsMetadata()
    {
        logger.info("Calling VO Tools to reset SCS Metadata");

        URI resetTapMetadataUri = getVoToolsUriBuilder().path("scs").path("reset").build();

        MessageDTO response = restTemplate.getForObject(resetTapMetadataUri, MessageDTO.class);

        logger.info("Finished resetting SCS Metadata. Response: {}", response.getMessage());
    }

    /**
     * @return the health of VO Tools as a deserialised JSON object.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getHealth()
    {
        URI healthUri = getVoToolsUriBuilder().path("health").build();

        try
        {
            Map<String, Object> response = restTemplate.getForObject(healthUri, Map.class);
            return response;
        }
        catch (HttpStatusCodeException ex)
        {
            /*
             * If the service is down then we might received a non-200 response which we try and handle as a normal
             * health JSON response but throw the original exception if we can't.
             */
            try
            {
                Map<String, Object> response =
                        (Map<String, Object>) new ObjectMapper().readValue(
                                new StringReader(ex.getResponseBodyAsString()), Map.class);
                return response;
            }
            catch (IOException ioe) // Covers JsonParseException, JsonMappingException, and generic IOException
            {
                throw ex;
            }
        }
    }

    /**
     * Gets a builder for VO Tools URI.
     * 
     * @return builder for VO Tools URI.
     */
    protected UriBuilder getVoToolsUriBuilder()
    {
        return UriBuilder.fromPath(voToolsUrl);
    }

    /**
     * Creates a new rest template, forcing json request and responses
     * @param connectionTimeout
     *            the connection timeout value  
     * @return the rest template
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected SecuredRestTemplate createRestTemplate(Integer connectionTimeout)
    {
        SecuredRestTemplate restTemplate = new SecuredRestTemplate(connectionTimeout);
        // Force JSON request and response
        restTemplate.setMessageConverters((List) Arrays.asList(new MappingJackson2HttpMessageConverter()));
        return restTemplate;
    }
}
