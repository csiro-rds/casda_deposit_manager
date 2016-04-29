package au.csiro.casda.deposit.manager;

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


import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.transaction.Transactional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import au.csiro.casda.datadeposit.DepositStateChangeListener;
import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.deposit.jpa.Level7CollectionRepository;
import au.csiro.casda.deposit.jpa.ProjectRepository;
import au.csiro.casda.dto.ParentDepositableDTO;
import au.csiro.casda.entity.observation.Catalogue;
import au.csiro.casda.entity.observation.CatalogueType;
import au.csiro.casda.entity.observation.Level7Collection;
import au.csiro.casda.entity.observation.Project;
import au.csiro.casda.logging.CasdaLogMessageBuilderFactory;
import au.csiro.casda.logging.LogEvent;

/**
 * Deposit service for level 7 catalogues.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class Level7DepositService
{
    /**
     * Exception class used to indicate that a level 7 collection with the supplied dapCollectionId does not exist.
     * <p>
     * Copyright 2015, CSIRO Australia. All rights reserved.
     */
    public static class UnknownCollectionException extends Exception
    {
        private static final long serialVersionUID = 1L;

        private UnknownCollectionException(long dapCollectionId)
        {
            super(String.format("Could not find level 7 collection with id '%d'", dapCollectionId));
        }

    }

    /**
     * Exception class used to indicate that a supplied opalCode does not match the opalCode of a level 7 collection
     * matching a supplied dapCollectionId.
     * <p>
     * Copyright 2015, CSIRO Australia. All rights reserved.
     */
    public static class CollectionProjectCodeMismatchException extends Exception
    {
        private static final long serialVersionUID = 1L;

        private CollectionProjectCodeMismatchException(Level7Collection level7Collection, String opalCode)
        {
            super(String.format("Level 7 collection matching collection id '%d' had a "
                    + "different project code '%s' (expected '%s')", level7Collection.getId(), level7Collection
                    .getProject().getOpalCode(), opalCode));
        }

    }

    /**
     * Exception class used to indicate that a level 7 collection is in the wrong state to perform a requested action.
     * <p>
     * Copyright 2015, CSIRO Australia. All rights reserved.
     */
    public static class CollectionIllegalStateException extends Exception
    {
        private static final long serialVersionUID = 1L;

        private CollectionIllegalStateException(String message)
        {
            super(message);
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(Level7DepositService.class);

    private String level7CollectionsDirectory;

    private Level7CollectionRepository level7CollectionRepository;

    private ProjectRepository projectRepository;

    private Level7DepositProgressor level7DepositProgressor;

    private DepositStateFactory depositStateFactory;

    private DepositStateChangeListener depositStateChangeListener;

    /**
     * Constructor
     * 
     * @param level7CollectionsDirectory
     *            the level 7 collections directory
     * @param level7CollectionRepository
     *            level 7 collection repository
     * @param projectRepository
     *            project repository
     * @param level7DepositProgressor
     *            the progressor for level 7 collections
     * @param depositStateFactory
     *            a DepositStateFactory (used, during recovery, for configuring the DepositState on a Level7Collection
     *            object)
     * @param depositStateChangeListener
     *            a DepositStateChangeListener (used, during recovery, to allow external parties to monitor recovery
     *            state transitions)
     */
    @Autowired
    public Level7DepositService(@Value("${deposit.level7.collections.dir}") String level7CollectionsDirectory,
            Level7CollectionRepository level7CollectionRepository, ProjectRepository projectRepository,
            Level7DepositProgressor level7DepositProgressor, DepositStateFactory depositStateFactory,
            @Qualifier("CasdaLevel7DepositStateChangeListener") DepositStateChangeListener depositStateChangeListener)
    {
        this.level7CollectionsDirectory = level7CollectionsDirectory;
        this.level7DepositProgressor = level7DepositProgressor;
        this.level7CollectionRepository = level7CollectionRepository;
        this.projectRepository = projectRepository;
        this.depositStateFactory = depositStateFactory;
        this.depositStateChangeListener = depositStateChangeListener;
    }

    /**
     * Creates a new level 7 collection and its catalogues from the parameters passed to the method and the catalogue
     * files in the level 7 collection directory.
     * 
     * @param opalCode
     *            the project's opal code
     * @param dapCollectionId
     *            the data collection id (from Data Access Portal)
     * @return newly created Level7Collection
     * @throws CollectionIllegalStateException
     *             if the level 7 collection has no files, or if the level 7 collection deposit has already been
     *             initiated
     */
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = Exception.class)
    public Level7Collection initiateLevel7CollectionDeposit(String opalCode, long dapCollectionId)
            throws CollectionIllegalStateException
    {
        if (level7CollectionRepository.findByDapCollectionId(dapCollectionId) != null)
        {
            throw new CollectionIllegalStateException(String.format(
                    "Level 7 collection with id '%s' deposit has already been initiated", dapCollectionId));
        }
        if (!new File(level7CollectionsDirectory, Long.toString(dapCollectionId)).exists())
        {
            throw new CollectionIllegalStateException(String.format(
                    "Level 7 collection with id '%s' has no items to deposit", dapCollectionId));
        }

        Level7Collection level7Collection = new Level7Collection(dapCollectionId);
        Project project = projectRepository.findByOpalCode(opalCode);
        if (project == null)
        {
            project = new Project(opalCode);
        }
        level7Collection.setProject(project);

        // add a catalogue for all of the files in the folder
        try (DirectoryStream<Path> level7CatalogueFiles =
                Files.newDirectoryStream(Paths.get(level7CollectionsDirectory, Long.toString(dapCollectionId))))
        {
            level7CatalogueFiles.forEach(level7CatalogueFile -> {
                if (!level7CatalogueFile.getFileName().toString().endsWith(".checksum"))
                {
                    Catalogue catalogue = new Catalogue(CatalogueType.LEVEL7);
                    catalogue.setFormat("votable");
                    catalogue.setFilename(level7CatalogueFile.getFileName().toString());
                    level7Collection.addCatalogue(catalogue);
                }
            });
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        if (level7Collection.getCatalogues().isEmpty())
        {
            throw new CollectionIllegalStateException(String.format(
                    "Level 7 collection with id '%s' has no items to deposit", dapCollectionId));
        }
        return level7CollectionRepository.save(level7Collection);
    }

    /**
     * Progresses any non-DEPOSITED Level 7 Collections.
     */
    @Scheduled(fixedDelayString = "${deposit.workflow.progression.delay.millis}")
    public void progressCollections()
    {
        logger.debug("Progressing level 7 collections");
        List<Level7Collection> level7Collections = level7CollectionRepository.findDepositingLevel7Collections();
        if (CollectionUtils.isNotEmpty(level7Collections))
        {
            logger.debug("{}", "-------------------------------------------------------");
        }
        for (Level7Collection level7Collection : level7Collections)
        {
            logger.debug("{}", "Progressing collection " + level7Collection.getDapCollectionId());

            try
            {
                level7DepositProgressor.progressCollection(level7Collection.getDapCollectionId());
            }
            catch (ObjectOptimisticLockingFailureException e)
            {
                // CASDA's Data Deposit application might update the details in an artefact while we are progressing the
                // collection, which will cause an optimistic locking failure (CASDA-4440). We log the event, and
                // continue because this will be retried automatically the next time this scheduled method is run.
                logger.warn(
                        CasdaLogMessageBuilderFactory
                                .getCasdaMessageBuilder(LogEvent.UNKNOWN_EVENT)
                                .addCustomMessage(
                                        "Rolling back, level 7 collection modified by another process "
                                                + level7Collection.getDapCollectionId()).toString(), e);
            }

            Level7Collection changedCollection =
                    level7CollectionRepository.findByDapCollectionId(level7Collection.getDapCollectionId());

            logger.debug("{}", String.format("Level 7 Collection %d now %s", changedCollection.getDapCollectionId(),
                    changedCollection.getDepositStateType().toString()));
            logger.debug("{}", "-------------------------------------------------------");
        }
    }

    /**
     * Returns a summary of a level 7 collection.
     * 
     * @param opalCode
     *            the project's opal code
     * @param dapCollectionId
     *            the data collection id (from Data Access Portal)
     * @return a ParentDepositableArtefactDTO describing the level 7 collection
     * @throws CollectionProjectCodeMismatchException
     *             if the level 7 collection with the specified dap collection id has a different opal project code to
     *             the one specified
     */
    @Transactional(value = Transactional.TxType.REQUIRED)
    public ParentDepositableDTO getLevel7CollectionSummary(String opalCode, Long dapCollectionId)
            throws CollectionProjectCodeMismatchException
    {
        Level7Collection level7Collection = level7CollectionRepository.findByDapCollectionId(dapCollectionId);
        if (level7Collection != null)
        {
            if (!level7Collection.getProject().getOpalCode().equals(opalCode))
            {
                throw new CollectionProjectCodeMismatchException(level7Collection, opalCode);
            }
            else
            {
                return new ParentDepositableDTO(level7Collection, new File(level7CollectionsDirectory,
                        Long.toString(dapCollectionId)));
            }
        }
        else
        {
            File collectionDirectory = new File(level7CollectionsDirectory, Long.toString(dapCollectionId));
            if (collectionDirectory.exists())
            {
                return new ParentDepositableDTO(collectionDirectory);
            }
            else
            {
                return null;
            }
        }
    }

    /**
     * Saves the contents of the fileInputStream to a file with the supplied name to the appropriate level 7 collection
     * deposit directory for the give collection id.
     * 
     * @param dapCollectionId
     *            the data collection id (from Data Access Portal) - must be > 0
     * @param filename
     *            the name of the level 7 collection file (may not be blank)
     * @param fileInputStream
     *            an InputStream (the caller must close the stream themselves)
     * @return the path to the saved file
     */
    public String saveFileForLevel7CollectionDeposit(Long dapCollectionId, String filename, InputStream fileInputStream)
    {
        if (dapCollectionId == null)
        {
            throw new IllegalArgumentException("expected: dapCollectionId != null");
        }
        if (dapCollectionId <= 0)
        {
            throw new IllegalArgumentException("expected: dapCollectionId > 0");
        }
        if (StringUtils.isBlank(filename))
        {
            throw new IllegalArgumentException("expected: !StringUtils.isBlank(filename)");
        }
        if (fileInputStream == null)
        {
            throw new IllegalArgumentException("expected: fileInputStream != null");
        }
        File collectionDirectory = new File(level7CollectionsDirectory, Long.toString(dapCollectionId));
        if (!collectionDirectory.exists() && !collectionDirectory.mkdirs())
        {
            throw new RuntimeException("Cannot create directories: " + collectionDirectory);
        }

        // copy the file to the collection directory
        File catalogueFile = new File(collectionDirectory, filename);

        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(catalogueFile));)
        {
            IOUtils.copyLarge(fileInputStream, out);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        return catalogueFile.getAbsolutePath();
    }

    /**
     * Recovers a failed level 7 collection deposit.
     * 
     * @param opalCode
     *            the project's opal code
     * @param dapCollectionId
     *            the data collection id (from Data Access Portal)
     * @throws UnknownCollectionException
     *             if a level 7 collection with the specified dap collection id could not be found
     * @throws CollectionProjectCodeMismatchException
     *             if the level 7 collection with the specified dap collection id has a different opal project code to
     *             the one specified
     * @throws CollectionIllegalStateException
     *             if the level 7 collection has not failed
     */
    @Transactional(value = Transactional.TxType.REQUIRED, rollbackOn = Exception.class)
    public void recoverFailedLevel7CollectionDeposit(String opalCode, long dapCollectionId)
            throws UnknownCollectionException, CollectionProjectCodeMismatchException, CollectionIllegalStateException
    {
        Level7Collection level7Collection = level7CollectionRepository.findByDapCollectionId(dapCollectionId);
        if (level7Collection == null)
        {
            throw new UnknownCollectionException(dapCollectionId);
        }
        if (!level7Collection.getProject().getOpalCode().equals(opalCode))
        {
            throw new CollectionProjectCodeMismatchException(level7Collection, opalCode);
        }
        level7Collection.setDepositStateFactory(depositStateFactory);
        level7Collection.setDepositStateChangeListener(depositStateChangeListener);
        if (!level7Collection.isFailedDeposit())
        {
            throw new CollectionIllegalStateException(String.format(
                    "Expected deposit of collection with id '%d' to have failed (but is '%s')",
                    level7Collection.getId(), level7Collection.getDepositStateType().getDescription()));
        }
        level7Collection.recoverDeposit();
        level7CollectionRepository.save(level7Collection);
    }
}
