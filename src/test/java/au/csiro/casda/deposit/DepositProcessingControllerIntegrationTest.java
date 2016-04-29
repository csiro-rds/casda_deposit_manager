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


import static org.junit.Assert.assertEquals;

import java.io.File;
import java.net.URI;
import java.net.URL;

import javax.ws.rs.core.UriBuilder;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import au.csiro.casda.security.SecuredRestTemplate;
import au.csiro.casda.services.dto.Message.MessageCode;
import au.csiro.casda.services.dto.MessageDTO;

/**
 * Integration tests for the Deposit Processing Controller
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public class DepositProcessingControllerIntegrationTest
{

    private static final String DEPOSIT_MANAGER_URL = "http://localhost:8080/casda_deposit_manager";
    
    @Ignore
    @Test
    public void integrationTestCreateCatalogueFile() throws Exception
    {
        String filename = "/Projects/Casda/casda_deposit_manager/src/test/resources/testFile/testFile.txt";
        
        UriBuilder uriBuilder = UriBuilder.fromPath(DEPOSIT_MANAGER_URL);
        uriBuilder.path("projects");
        uriBuilder.path("AS007");
        uriBuilder.path("level_7_collections");
        uriBuilder.path("15");
        uriBuilder.path("catalogues");

        URI uri = uriBuilder.build();

        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        URL fileURL = new File(filename).toURI().toURL();

        map.add("file", new UrlResource(fileURL));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> fileEntity =
                new HttpEntity<MultiValueMap<String, Object>>(map, headers);

        MessageDTO response = new SecuredRestTemplate().postForObject(uri, fileEntity, MessageDTO.class);
        // System.out.println(response.toString());
        assertEquals(MessageCode.SUCCESS, response.getMessageCode());
    }

    @Ignore
    @Test
    public void integrationTestCreateReadyFile() throws Exception
    {
        UriBuilder uriBuilder = UriBuilder.fromPath(DEPOSIT_MANAGER_URL);
        uriBuilder.path("projects");
        uriBuilder.path("AS007");
        uriBuilder.path("level_7_collections");
        uriBuilder.path("15");
        uriBuilder.path("ready");

        URI uri = uriBuilder.build();

        MessageDTO response = new SecuredRestTemplate().postForObject(uri, null, MessageDTO.class);
        // System.out.println(response.toString());
        assertEquals(MessageCode.SUCCESS, response.getMessageCode());
    }
}
