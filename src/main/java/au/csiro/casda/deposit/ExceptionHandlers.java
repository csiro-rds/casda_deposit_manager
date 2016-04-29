package au.csiro.casda.deposit;

import javax.servlet.ServletException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import au.csiro.casda.BadRequestException;
import au.csiro.casda.ResourceNotFoundException;
import au.csiro.casda.ServerException;
import au.csiro.casda.deposit.exception.DepositProcessingException;
import au.csiro.casda.logging.CasdaLogMessageBuilderFactory;
import au.csiro.casda.logging.CasdaMessageBuilder;
import au.csiro.casda.logging.LogEvent;
import au.csiro.casda.services.dto.Message.MessageCode;
import au.csiro.casda.services.dto.MessageDTO;

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
 * Default exception handler
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
@ControllerAdvice
class ExceptionHandlers
{

    /**
     * Default view for errors will match associated page *.jsp
     */
    public static final String DEFAULT_ERROR_VIEW = "error";

    private static final Logger logger = LoggerFactory.getLogger(ExceptionHandlers.class);

    private HttpHeaders headers = new HttpHeaders();

    /**
     * Constructor.
     */
    public ExceptionHandlers()
    {
        // Initialise the content type for the response as JSON.
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    /**
     * Exception handler for ResourceNotFoundException that responds with an appropriate status code and details in a
     * JSON-serialised MessageDTO.
     * 
     * @param ex
     *            the exception thrown by the application
     * @param request
     *            the web request
     * @return a ResponseEntity
     */
    @ExceptionHandler({ ResourceNotFoundException.class })
    public ResponseEntity<MessageDTO> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request)
    {
        logger.debug("There was a {} processing request: {}", ex.getClass().getName(), request, ex);
        return new ResponseEntity<>(new MessageDTO(MessageDTO.MessageCode.FAILURE, ex.getMessage()), headers,
                HttpStatus.NOT_FOUND);
    }

    /**
     * Exception handler for BadRequestException that responds with an appropriate status code and details in a
     * JSON-serialised MessageDTO.
     * 
     * @param ex
     *            the exception thrown by the application
     * @param request
     *            the web request
     * @return a ResponseEntity
     */
    @ExceptionHandler({ BadRequestException.class })
    public ResponseEntity<MessageDTO> handleBadRequestException(BadRequestException ex, WebRequest request)
    {
        logger.debug("There was a {} processing request: {}", ex.getClass().getName(), request, ex);
        return new ResponseEntity<>(new MessageDTO(MessageDTO.MessageCode.FAILURE, ex.getMessage()), headers,
                HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles ServerExceptions thrown in this web app.
     * 
     * @param ex
     *            the ServerException thrown
     * @param request
     *            the request where the exception occurred
     * @return the ResponseEntity once the exception has been handled
     */
    @ExceptionHandler({ ServerException.class })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Object> handleServerException(RuntimeException ex, WebRequest request)
    {
        logger.error("There was a server error: {}", request, ex);
        return new ResponseEntity<>(new MessageDTO(MessageCode.FAILURE, ex.getMessage()), headers,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles IOExceptions and DepositProcessingExceptions thrown in this web app.
     * 
     * @param ex
     *            the exception thrown
     * @param request
     *            the request where the exception occurred
     * @return the ResponseEntity once the exception has been handled
     */
    @ExceptionHandler({ DepositProcessingException.class })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<Object> handleIOException(Exception ex, WebRequest request)
    {
        logger.error("There was an error processing this request: {}", request, ex);
        return new ResponseEntity<>(new MessageDTO(MessageCode.FAILURE, ex.getMessage()), headers,
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Exception handler for ServletException that responds with an appropriate status code and details in a
     * JSON-serialised MessageDTO.
     * 
     * @param ex
     *            the exception thrown by the application
     * @param request
     *            the web request
     * @return a ResponseEntity
     */
    @ExceptionHandler({ ServletException.class })
    public ResponseEntity<MessageDTO> handleServletException(ServletException ex, WebRequest request)
    {
        logger.debug("There was a {} processing request: {}", ex.getClass().getName(), request, ex);
        /*
         * Delegating to a ResponseEntityExceptionHandler because it does all the hard work of translating Spring's
         * internal exceptions into the right response status codes and messages.
         */
        ResponseEntityExceptionHandler handler = new ResponseEntityExceptionHandler()
        {
        };
        ResponseEntity<Object> defaultResponse = handler.handleException(ex, request);
        return new ResponseEntity<>(new MessageDTO(MessageDTO.MessageCode.FAILURE, ex.getMessage()), headers,
                defaultResponse.getStatusCode());
    }

    /**
     * Exception handler for Exception that responds with an appropriate status code and details in a JSON-serialised
     * MessageDTO.
     * 
     * @param ex
     *            the exception thrown by the application
     * @param request
     *            the web request
     * @return a ResponseEntity
     */
    @ExceptionHandler({ Exception.class })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Object handleRemainingExceptions(Exception ex, WebRequest request)
    {
        if (request.getHeader("accept").contains("application/json"))
        {
            logger.error("There was a {} processing request: {}", ex.getClass().getName(), request, ex);
            return new ResponseEntity<>(new MessageDTO(MessageDTO.MessageCode.FAILURE, ex.getMessage()), headers,
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        else
        {
            CasdaMessageBuilder<?> builder =
                    CasdaLogMessageBuilderFactory.getCasdaMessageBuilder(LogEvent.UNKNOWN_EVENT);
            builder.add(String.format("An unexpected exception occured trying to process request '%s'",
                    request.toString()));
            logger.error(builder.toString(), ex);
            // Otherwise setup and send the user to a default error-view.
            ModelAndView mav = new ModelAndView();
            mav.addObject("exception", ex);
            mav.addObject("url", request.toString());
            mav.setViewName(DEFAULT_ERROR_VIEW);
            return mav;
        }
    }
}
