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


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * UI Controller for the Deposit Manager application.
 * 
 * Copyright 2014, CSIRO Australia All rights reserved.
 * 
 */
@Controller
public class DepositManagerUiController
{
   
    /**
     * Login page
     * 
     * @param model
     *            Model
     * @return login 
     *            String view name of login
     */
    @RequestMapping(value = "/login")
    public String login(Model model)
    {
        return "login";
    }
    
    /**
     * Home page
     * 
     * @param model
     *            Model
     * @return home 
     *            String view name of home page
     */
    @RequestMapping(value = "/")
    public String home(Model model)
    {
        return "home";
    }

}
