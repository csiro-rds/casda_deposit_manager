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
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.Charsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;


/**
 * 
 * RestTemplate to access secured rest services (SSL enabled)
 * 
 * <p>
 * Copyright 2015, CSIRO Australia All rights reserved.
 *
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SecuredRestTemplate extends RestTemplate
{
    /**
     * Default rest connection timeout in milliseconds currently 2 min
     */
    public static final int DEFAULT_RESTTEMPLATE_CONNECT_TIMEOUT = 120000;
    
    private HttpComponentsClientHttpRequestFactory requestFactory;
    
    /**
     * Default Constructor
     * @param connectionTimeout the time limit for rest connections
     */
    @Autowired
    public SecuredRestTemplate(@Value("${connection.timeout.limit: " 
    		+ DEFAULT_RESTTEMPLATE_CONNECT_TIMEOUT + "}") Integer connectionTimeout)
    {
        this("", "", true);
        int restConnectionTimeout;
        try
        {
            restConnectionTimeout = connectionTimeout != null && connectionTimeout > -1 
            		? connectionTimeout:DEFAULT_RESTTEMPLATE_CONNECT_TIMEOUT;
        }
        catch(Exception e)
        {
            restConnectionTimeout = DEFAULT_RESTTEMPLATE_CONNECT_TIMEOUT;
        }

        requestFactory.setConnectTimeout(restConnectionTimeout);
    }

    /**
     * Constructor
     * 
     * @param userName
     *            The username to be used for basic authentication.
     * @param password
     *            The password to be used for basic authentication.
     * @param buffer
     *            The BufferRequestBody switch to turn on/off.
     * 
     */
    public SecuredRestTemplate(String userName, String password, boolean buffer)
    {
        requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setBufferRequestBody(buffer);

        // Hostname verification is turned off in NoopHostnameVerifier so this can work on all our environments
        CloseableHttpClient httpClient = HttpClients.custom().setSSLHostnameVerifier(new NoopHostnameVerifier())
                .build();
        requestFactory.setHttpClient(httpClient);

        //If basic auth credentials are not provided don't intercept to add Authorization header
        if (StringUtils.isBlank(userName) || StringUtils.isBlank(password))
        {
            setRequestFactory(requestFactory);
        }
        else
        {
            List<ClientHttpRequestInterceptor> interceptors = Collections
                    .<ClientHttpRequestInterceptor> singletonList(new BasicAuthorizationInterceptor(userName, password));
            setRequestFactory(new InterceptingClientHttpRequestFactory(requestFactory, interceptors));
        } 
    }
    
    /**
     * 
     * Intercepter class that intercepts outgoing rest calls and add Authorization header
     * 
     * <p>
     * Copyright 2015, CSIRO Australia All rights reserved.
     *
     */
    private static class BasicAuthorizationInterceptor implements ClientHttpRequestInterceptor
    {
        private final String username;
        private final String password;

        public BasicAuthorizationInterceptor(String username, String password)
        {
            this.username = username;
            this.password = password;
        }

        @Override
        public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
                throws IOException
        {
            byte[] token = Base64.encode((this.username + ":" + this.password).getBytes(Charsets.UTF_8));           
            request.getHeaders().add("Authorization", "Basic " + new String(token, Charsets.UTF_8));
            return execution.execute(request, body);
        }
    }
}
