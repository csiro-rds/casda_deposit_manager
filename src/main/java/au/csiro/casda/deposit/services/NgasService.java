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


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import au.csiro.casda.security.SecuredRestTemplate;

/**
 * Service to abstract NGAS calls
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@Component
public class NgasService
{
    /**
     * Exception class representing a problem with calling the service. This exception will only be thrown when a
     * service call cannot be completed due to an internal processing problem (ie: inability to meet a service call's
     * 'post-conditions').
     * <p>
     * Copyright 2014, CSIRO Australia All rights reserved.
     */
    public static class ServiceCallException extends Exception
    {
        private static final long serialVersionUID = 1L;

        /**
         * Constructor
         * 
         * @param message
         *            an explanation of the exception context
         */
        public ServiceCallException(String message)
        {
            super(message);
        }

        /**
         * Constructor
         * 
         * @param message
         *            an explanation of the exception context
         * @param t
         *            the throwable to wrap
         */
        public ServiceCallException(String message, Throwable t)
        {
            super(message, t);
        }

        private static String getResponseDetails(ResponseEntity<String> responseEntity, Throwable t)
        {
            HttpStatus status = null;
            String body = null;
            if (responseEntity != null)
            {
                status = responseEntity.getStatusCode();
                body = responseEntity.getBody();
            }
            else if (t instanceof HttpStatusCodeException)
            {
                status = ((HttpStatusCodeException) t).getStatusCode();
                body = ((HttpStatusCodeException) t).getResponseBodyAsString();
            }
            if (status == null)
            {
                return "(No response from NGAS.)";
            }
            else
            {
                return String.format("NGAS Response:\nStatus Code: %s\nBody:\n%s", status.toString(),
                        StringUtils.isBlank(body) ? "(No body.)" : body);
            }
        }

        /**
         * Constructor
         * 
         * @param responseEntity
         *            the returned responseEntity (if available)
         * @param message
         *            an explanation of the exception context
         * @param t
         *            the cause of the exception
         */
        public ServiceCallException(ResponseEntity<String> responseEntity, String message, Throwable t)
        {
            this(message + " " + getResponseDetails(responseEntity, t), t);
        }

    }

    /**
     * Encapsulates the NGAS status response
     * <p>
     * Copyright 2014, CSIRO Australia All rights reserved.
     */
    public static class Status
    {
        private static final class UnmarshallException extends Exception
        {
            private static final long serialVersionUID = 1L;

            private static String getMessageForMarshalledStatus(String marshalledStatus)
            {
                return String
                        .format("Could not unmarshall '%s' into a valid NgasStatus XML document", marshalledStatus);
            }

            private UnmarshallException(String marshalledStatus)
            {
                super(getMessageForMarshalledStatus(marshalledStatus));
            }

            private UnmarshallException(String marshalledStatus, Exception e)
            {
                super(getMessageForMarshalledStatus(marshalledStatus), e);
            }
        }

        private static final String STATUS_XPATH = "/NgamsStatus/Status/@Status";
        private static final String CHECKSUM_XPATH = "/NgamsStatus/DiskStatus/FileStatus/@Checksum";
        private static final String FILENAME_XPATH = "/NgamsStatus/DiskStatus/FileStatus/@FileName";
        private static final String UNCOMPRESSED_FILESIZE_XPATH =
                "/NgamsStatus/DiskStatus/FileStatus/@UncompressedFileSize";
        private static final String MOUNTPOINT_XPATH = "/NgamsStatus/DiskStatus/@MountPoint";

        private Document document;

        /**
         * Creates a Status using the given XML document string
         * 
         * @param xmlDocument
         *            an XML document for the NGAS status response, as a String
         * @throws UnmarshallException
         *             if the xmlDocument could not be parsed as an NGAS status resonse
         */
        public Status(String xmlDocument) throws UnmarshallException
        {
            if (StringUtils.isBlank(xmlDocument))
            {
                throw new UnmarshallException(xmlDocument);
            }

            try
            {
                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                builderFactory.setValidating(false);
                builderFactory.setNamespaceAware(true);
                builderFactory.setFeature("http://xml.org/sax/features/validation", false);
                builderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                byte[] bytes = xmlDocument.getBytes("UTF-8");
                ByteArrayInputStream is = new ByteArrayInputStream(bytes);
                this.document = builder.parse(is);
            }
            catch (ParserConfigurationException | SAXException | IOException e)
            {
                throw new UnmarshallException(xmlDocument, e);
            }
        }

        /**
         * @return whether the overall status was a SUCCESS
         */
        public boolean wasSuccess()
        {
            return "SUCCESS".equals(evaluateXpathExpression(STATUS_XPATH));
        }

        /**
         * @return whether the overall status was a FAILURE
         */
        public boolean wasFailure()
        {
            return "FAILURE".equals(evaluateXpathExpression(STATUS_XPATH));
        }

        /**
         * @return the checksum for a file in NGAS
         */
        public String getChecksum()
        {
            return evaluateXpathExpression(CHECKSUM_XPATH);
        }

        /**
         * @return the fileName for a file in NGAS
         */
        public String getFileName()
        {
            return evaluateXpathExpression(FILENAME_XPATH);
        }

        /**
         * @return the uncompressed file size (in bytes) for a file in NGAS
         */
        public long getUncompressedFileSizeBytes()
        {
            return Long.parseLong(evaluateXpathExpression(UNCOMPRESSED_FILESIZE_XPATH));
        }
        
        /**
         * @return the mount point for a file in NGAS
         */
        public String getMountPoint()
        {
            return evaluateXpathExpression(MOUNTPOINT_XPATH);
        }

        private String evaluateXpathExpression(String expression)
        {
            try
            {
                XPathFactory xpathFactory = XPathFactory.newInstance();
                XPath xpath = xpathFactory.newXPath();

                XPathExpression xpathExpression = xpath.compile(expression);
                return xpathExpression.evaluate(this.document);
            }
            catch (XPathExpressionException e)
            {
                /*
                 * This is really an unexpected exception because either: * the expression was malformed and couldn't be
                 * compiled, or * the expression compiled but failed during evaluation The first is a programming bug
                 * and the second is completely unexpected.
                 */
                throw new RuntimeException(e);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString()
        {
            try
            {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                printDocument(this.document, out);
                return out.toString("UTF-8");
            }
            catch (IOException | TransformerException e)
            {
                throw new RuntimeException(e);
            }
        }

        /**
         * Utility method used to print-out a Status as XML
         * 
         * @param doc
         *            the underlying XML document
         * @param out
         *            an OutputStream
         * @throws IOException
         *             if there was a problem writing to the OutputStream
         * @throws TransformerException
         *             if there was a problem writing the XML
         */
        public static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException
        {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            transformer.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(out, "UTF-8")));
        }
    }

    private final String ngasUrl;
    private final SecuredRestTemplate restTemplate;

    /**
     * Constructor
     * 
     * @param restTemplate
     *            the rest template for calls to ngas
     * @param hostname
     *            the NGAS server hostname
     * @param port
     *            the NGAS server port
     */
    @Autowired
    public NgasService(SecuredRestTemplate restTemplate, @Value("${ngas.server.name}") String hostname,
            @Value("${ngas.server.port}") String port)
    {
        super();
        this.restTemplate = restTemplate;
        this.ngasUrl = String.format("http://%s:%s", hostname, port);
    }

    /**
     * Get the status of an NGAS file
     * 
     * @param fileId
     *            the NGAS identifier for the file
     * @return a Status
     * @throws ServiceCallException
     *             if the service call could not complete due to an internal error
     */
    public Status getStatus(String fileId) throws ServiceCallException
    {
        ResponseEntity<String> responseEntity = null;
        try
        {
            responseEntity = restTemplate.getForEntity(ngasUrl + "/STATUS?file_id={file_id}", String.class, fileId);
            /*
             * Assume NGAS always responds with an XML response. Regardless, any error will be raised as an exception
             * with the response.
             */
            return new Status(responseEntity.getBody());
        }
        catch (Exception e)
        {
            throw new ServiceCallException(responseEntity, String.format(
                    "Could not complete request to get file status, fileId: '%s'.", fileId), e);
        }
    }

    /**
     * Deposit a file in NGAS using the ARCHIVE command
     * 
     * @param fileId
     *            the NGAS identifier for the file
     * @param path
     *            the path to the file
     * @return a Status
     * @throws FileNotFoundException
     *             if the source file could not be found at the given path
     * @throws ServiceCallException
     *             if the service call could not complete due to an internal error
     */
    public Status archiveFile(String fileId, Path path) throws FileNotFoundException, ServiceCallException
    {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-type", "ngas/archive-request");

        HttpEntity<Resource> entity = new HttpEntity<Resource>(new FileSystemResource(path.toFile()), headers);
        SecuredRestTemplate archiveRestTemplate = new SecuredRestTemplate("", "", false);
        
        ResponseEntity<String> responseEntity = null;
        try
        {
            responseEntity =
                    archiveRestTemplate.exchange(ngasUrl + "/ARCHIVE?filename={file_id}", HttpMethod.POST, entity,
                            String.class, fileId);
            /*
             * Assume NGAS always responds with an XML response. Regardless, any error will be raised as an exception
             * with the response.
             */
            return new Status(responseEntity.getBody());
        }
        catch (Exception e)
        {
            throw new ServiceCallException(responseEntity, String.format(
                    "Could not complete request to archive file, fileId: '%s', path: '%s'.", fileId, path.toString()),
                    e);
        }
    }

    /**
     * Register a file with NGAS that lives in the given NGAS staging directory and with the given filename. Note that
     * the file can then be referenced using the filename as the file ID.
     * 
     * @param stagingDirectory
     *            the path on the NGAS server used to stage files for the REGISTER command
     * @param filename
     *            the name of the file in the stagingDirectory to register
     * @return a Status
     * @throws ServiceCallException
     *             if the service call could not complete due to an internal error
     */
    public Status registerFile(String stagingDirectory, String filename) throws ServiceCallException
    {
        ResponseEntity<String> responseEntity = null;
        try
        {
            responseEntity =
                    restTemplate.getForEntity(ngasUrl + "/REGISTER?path={path}", String.class, stagingDirectory + "/"
                            + filename);
            /*
             * Assume NGAS always responds with an XML response. Regardless, any error will be raised as an exception
             * with the response.
             */
            Status registerStatus = new Status(responseEntity.getBody());
            if (registerStatus.wasSuccess())
            {
                return getStatus(filename);
            }
            else
            {
                return registerStatus;
            }
        }
        catch (Exception e)
        {
            throw new ServiceCallException(responseEntity, String.format(
                    "Could not complete request to register file, stagingDirectory: '%s', filename: '%s'.",
                    stagingDirectory, filename), e);
        }

    }
}
