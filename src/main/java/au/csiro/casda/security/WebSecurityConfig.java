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


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

/**
 * 
 * The Spring Web Security Config class
 * 
 * Copyright 2015, CSIRO Australia
 * All rights reserved.
 *
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter
{
    private static final String ADMIN_ROLE ="CASDA_ADMIN";
    
    @Override
    protected void configure(HttpSecurity http) throws Exception
    {
        http.authorizeRequests()
            .antMatchers("/level_5_deposits/**", "/level_5_refreshes/**", "/level_7_deposits/**", "/jobs/**")
                .hasRole(ADMIN_ROLE)
                .and()
            .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
            .logout().invalidateHttpSession(true)
            .logoutRequestMatcher(new AntPathRequestMatcher("/logout")).logoutSuccessUrl("/login?logout")
                .permitAll();        
        http.formLogin().failureHandler(new AuthenticationFailureHandler("/login?error"));        
        http.csrf().disable(); 
    }
    
    /**
     * Security in-memory user configuration
     * 
     * @param auth
     *            the AuthenticationManagerBuilder
     * @param username
     *            username configured in the properties
     * @param password
     *            password configured in the properties
     * @throws Exception
     *             thrown when errors during init
     */
    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth,
            @Value("${ui.login.admin.username}") String username, @Value("${ui.login.admin.password}") String password)
            throws Exception
    {
        auth.inMemoryAuthentication().withUser(username).password(password).roles(ADMIN_ROLE);
    }
}
