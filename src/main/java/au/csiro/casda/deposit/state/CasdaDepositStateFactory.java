package au.csiro.casda.deposit.state;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import au.csiro.casda.Utils;
import au.csiro.casda.datadeposit.ArchivedDepositState;
import au.csiro.casda.datadeposit.ChildDepositableArtefact;
import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.datadeposit.Depositable;
import au.csiro.casda.datadeposit.DepositedDepositState;
import au.csiro.casda.datadeposit.FailedDepositState;
import au.csiro.casda.datadeposit.ParentDepositableArtefactFailedDepositState;
import au.csiro.casda.datadeposit.ParentDepositableArtefactUndepositedDepositState;
import au.csiro.casda.datadeposit.ProcessedDepositState;
import au.csiro.casda.datadeposit.ProcessingDepositState;
import au.csiro.casda.datadeposit.RegisteredDepositState;
import au.csiro.casda.datadeposit.StagedDepositState;
import au.csiro.casda.datadeposit.UndepositedDepositState;
import au.csiro.casda.deposit.CasdaToolProcessJobBuilderFactory;
import au.csiro.casda.deposit.SingleJobMonitorFactory;
import au.csiro.casda.deposit.services.NgasService;
import au.csiro.casda.deposit.services.VoToolsService;
import au.csiro.casda.entity.observation.Catalogue;
import au.csiro.casda.entity.observation.ImageCube;
import au.csiro.casda.entity.observation.Level7Collection;
import au.csiro.casda.entity.observation.Observation;
import au.csiro.casda.jobmanager.CasdaToolProcessJobBuilder;
import au.csiro.casda.jobmanager.JobManager;
import au.csiro.casda.jobmanager.ProcessJobBuilder;
import au.csiro.casda.jobmanager.ProcessJobBuilder.ProcessJobFactory;
import au.csiro.casda.jobmanager.SimpleToolProcessJobBuilder;

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
 * Specialisation of DepositStateFactory that instantiates CASDA-specific (and Depositable-specific) instances of
 * DepositState that perform the required state actions for data deposit.
 * <p>
 * Copyright 2014, CSIRO Australia. All rights reserved.
 */
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
@Component
public class CasdaDepositStateFactory implements DepositStateFactory
{
    /**
     * Types of process jobs.
     * <p>
     * Copyright 2015, CSIRO Australia. All rights reserved.
     */
    public static enum ProcessJobType
    {
        /**
         * Represents a simple process job
         */
        SIMPLE,

        /**
         * Represents a CASDA tool process job
         */
        CASDA_TOOL
    };

    private NgasService ngasService;

    private JobManager jobManager;

    private String depositObservationParentDirectory;

    private String level7CollectionParentDirectory;

    private String stageCommand;

    private ProcessJobType stageCommandType;

    private String stageCommandArgs;

    private String registerCommand;

    private ProcessJobType registerCommandType;

    private String registerCommandArgs;

    private Map<String, String> ngasArtefactVolumeMap;

    private final String archiveStatusCommandAndArgs;

    private final String archivePutCommandAndArgs;

    private CasdaToolProcessJobBuilderFactory casdaToolBuilderFactory;

    private SingleJobMonitorFactory singleJobMonitorFactory;

    private VoToolsService voToolsService;

    private ProcessJobFactory processJobFactory;

    /**
     * Constructor
     * 
     * @param ngasService
     *            the service to interact with NGAS
     * @param jobManager
     *            a JobManager that can be used to manage the running of any processes
     * @param builderFactory
     *            factory to create CasdaToolProcessJobBuilders
     * @param processJobFactory
     *            a ProcessJobFactory used to instantiate a specific concrete subclass of ProcessJob
     * @param singleJobMonitorFactory
     *            the SingleJobMonitor factory
     * @param voToolsService
     *            the vo tools service
     * @param depositObservationParentDirectory
     *            the root directory of all deposited observations
     * @param level7CollectionParentDirectory
     *            the root directory of all level 7 collections
     * @param stageCommand
     *            the stage command used to stage depositable artefacts to the NGAS staging directory
     * @param stageCommandType
     *            the 'type' of the stage command used to stage depositable artefacts (must be a ProcessBuilderType)
     * @param stageCommandArgs
     *            the stage command args used to stage depositable artefacts to the NGAS staging directory
     * @param registerCommand
     *            the register command used to register depositable artefacts in the NGAS staging directory with NGAS
     * @param registerCommandType
     *            the 'type' of the copy command used to register depositable artefacts (must be a ProcessBuilderType)
     * @param registerCommandArgs
     *            the copy command args used to register depositable artefacts in the NGAS staging directory with NGAS
     * @param archiveStatusCommandAndArgs
     *            the command and args to use check an artefact status on the DMF
     * @param archivePutCommandAndArgs
     *            the command and args to use to force the DMF to dual state an artefact file.
     * @param marshalledNgasArtefactVolumeMap
     *            a name-value pair list, in Spring EL format, representing a map from DepositableArtefact 'types' to
     *            NGAS volume names (used to control which volume an artefact is copied to and registered on).
     */
    @Autowired
    public CasdaDepositStateFactory(NgasService ngasService, JobManager jobManager,
            CasdaToolProcessJobBuilderFactory builderFactory, ProcessJobFactory processJobFactory,
            SingleJobMonitorFactory singleJobMonitorFactory, VoToolsService voToolsService,
            @Value("${deposit.observation.parent.directory}") String depositObservationParentDirectory,
            @Value("${deposit.level7.collections.dir}") String level7CollectionParentDirectory,
            @Value("${artefact.stage.command}") String stageCommand,
            @Value("${artefact.stage.command.type}") String stageCommandType,
            @Value("${artefact.stage.command.args}") String stageCommandArgs,
            @Value("${artefact.register.command}") String registerCommand,
            @Value("${artefact.register.command.type}") String registerCommandType,
            @Value("${artefact.register.command.args}") String registerCommandArgs,
            @Value("${artefact.archive.status.command.and.args}") String archiveStatusCommandAndArgs,
            @Value("${artefact.archive.put.command.and.args}") String archivePutCommandAndArgs,
            @Value("${ngas.artefact.volume.map}") String marshalledNgasArtefactVolumeMap)
    {
        this.ngasService = ngasService;
        this.jobManager = jobManager;
        this.casdaToolBuilderFactory = builderFactory;
        this.processJobFactory = processJobFactory;
        this.singleJobMonitorFactory = singleJobMonitorFactory;
        this.voToolsService = voToolsService;
        this.depositObservationParentDirectory = depositObservationParentDirectory;
        this.level7CollectionParentDirectory = level7CollectionParentDirectory;
        this.stageCommand = stageCommand;
        this.stageCommandType = ProcessJobType.valueOf(stageCommandType);
        this.stageCommandArgs = stageCommandArgs;
        this.registerCommand = registerCommand;
        this.registerCommandType = ProcessJobType.valueOf(registerCommandType);
        this.registerCommandArgs = registerCommandArgs;
        this.archiveStatusCommandAndArgs = archiveStatusCommandAndArgs;
        this.archivePutCommandAndArgs = archivePutCommandAndArgs;
        this.ngasArtefactVolumeMap = Utils.elStringToMap(marshalledNgasArtefactVolumeMap);
    }

    /**
     * {@inheritDoc}
     */
    public DepositState createState(DepositState.Type type, Depositable depositable)
    {
        if (depositable instanceof Observation)
        {
            return createStateForObservation(type, (Observation) depositable);
        }
        else if (depositable instanceof Level7Collection)
        {
            return createStateForLevel7Collection(type, (Level7Collection) depositable);
        }
        else if (depositable instanceof ChildDepositableArtefact)
        {
            return createStateForDataProduct(type, (ChildDepositableArtefact) depositable);
        }
        else
        {
            throw new IllegalStateException(String.format("Illegal state type '%s' for depositable '%s'", type,
                    depositable.getClass().getSimpleName()).toString());
        }

    }

    /**
     * Create a suitable deposit state implementation object for a file based data product.
     * 
     * @param type
     *            the type of the state
     * @param depositable
     *            the Observation being deposited
     * @return The DepositState instance
     */
    protected DepositState createStateForDataProduct(DepositState.Type type, ChildDepositableArtefact depositable)
    {
        switch (type)
        {
        case UNDEPOSITED:
            return new UndepositedDepositState(this, depositable);
        case PROCESSED:
            return new ProcessedDepositState(this, depositable);
        case STAGED:
            return new StagedDepositState(this, depositable);
        case REGISTERED:
            return new RegisteredDepositState(this, depositable);
        case ARCHIVED:
            return new ArchivedDepositState(this, depositable);
        case DEPOSITED:
            return new DepositedDepositState(this, depositable);
        case FAILED:
            return new FailedDepositState(this, depositable);
        case STAGING:
            /*
             * Rather than always use a CasdaToolProcessJobBuilder for the ProcessJobBuilder, this state supports using
             * a SimpleToolProcessJobBuilder to allow for a local development 'no-op' configuration.
             * 
             * Ideally such a no-op would be configured in the Casda command-line tool itself, and for the staging
             * process that could be done, but when it comes to the registering process that is much more difficult (see
             * REGISTERING for more details).
             */
            return new CasdaStagingDepositState(this, (ChildDepositableArtefact) depositable,
                    this.ngasArtefactVolumeMap,
                    createProcessJobBuilderForProcessJobType(this.stageCommandType, this.stageCommand,
                            this.stageCommandArgs),
                    this.jobManager, depositObservationParentDirectory, level7CollectionParentDirectory);

        case REGISTERING:
            /*
             * Rather than always use a CasdaToolProcessJobBuilder for the ProcessJobBuilder, this state supports using
             * a SimpleToolProcessJobBuilder to allow for a local development 'no-op' configuration.
             * 
             * Ideally such a no-op would be configured in the Casda command-line tool itself, but the registering
             * command-line tool needs to talk to an NGAS service and replacing that with some sort of stub that returns
             * sensible status results (including correct checksums) would be complicated.
             */
            return new CasdaRegisteringDepositState(this, (ChildDepositableArtefact) depositable,
                    this.ngasArtefactVolumeMap,
                    createProcessJobBuilderForProcessJobType(this.registerCommandType, this.registerCommand,
                            this.registerCommandArgs),
                    this.jobManager, depositObservationParentDirectory, level7CollectionParentDirectory);

        case ARCHIVING:
            ProcessJobBuilder archiveStatusBuilder = createProcessJobBuilderForProcessJobType(ProcessJobType.SIMPLE,
                    this.archiveStatusCommandAndArgs, null);
            ProcessJobBuilder archivePutBuilder = createProcessJobBuilderForProcessJobType(ProcessJobType.SIMPLE,
                    this.archivePutCommandAndArgs, null);
            if (((ChildDepositableArtefact) depositable).getParent() instanceof Observation)
            {
                return new ObservationArchivingDepositState(this, (ChildDepositableArtefact) depositable,
                        this.ngasArtefactVolumeMap, ngasService, archiveStatusBuilder, archivePutBuilder,
                        singleJobMonitorFactory);
            }
            else
            {
                return new Level7ArchivingDepositState(this, (ChildDepositableArtefact) depositable,
                        this.ngasArtefactVolumeMap, ngasService, archiveStatusBuilder, archivePutBuilder,
                        singleJobMonitorFactory);
            }

        case PROCESSING:
            if (depositable instanceof ImageCube)
            {
                return new CasdaImageCubeProcessingDepositState(this, (ImageCube) depositable,
                        this.depositObservationParentDirectory, casdaToolBuilderFactory.createBuilder(),
                        this.jobManager);
            }
            else if (depositable instanceof Catalogue)
            {
                if (depositable.getParent() instanceof Observation)
                {
                    return new CasdaCatalogueProcessingDepositState(this, (Catalogue) depositable,
                            this.depositObservationParentDirectory, casdaToolBuilderFactory.createBuilder(),
                            this.jobManager);
                }
                else
                {
                    return new CasdaCatalogueProcessingDepositState(this, (Catalogue) depositable,
                            this.level7CollectionParentDirectory, casdaToolBuilderFactory.createBuilder(),
                            this.jobManager);
                }
            }
            else if (depositable instanceof Level7Collection)
            {
                return new CasdaFileProcessingDepositState(this, depositable, this.level7CollectionParentDirectory);
            }
            else
            {
                return new ProcessingDepositState(this, depositable);
            }

        default:
            throw new IllegalStateException(String.format("Illegal state type '%s' for depositable '%s'", type,
                    depositable.getClass().getSimpleName()).toString());
        }
    }

    /**
     * Create a suitable deposit state implementation object for the current state type of the Observation.
     * 
     * @param type
     *            the type of the state
     * @param depositable
     *            the Observation being deposited
     * @return The DepositState instance
     */
    private DepositState createStateForObservation(DepositState.Type type, Observation depositable)
    {
        switch (type)
        {
        case NOTIFYING:
            return new CasdaObservationNotifyingDepositState(this, (Observation) depositable,
                    this.depositObservationParentDirectory, casdaToolBuilderFactory.createBuilder(), this.jobManager);

        case UNDEPOSITED:
            return new ParentDepositableArtefactUndepositedDepositState(this, depositable);
        case DEPOSITING:
            return new ObservationDepositingDepositState(this, depositable);
        case DEPOSITED:
            return new DepositedDepositState(this, depositable);
        case FAILED:
            return new ParentDepositableArtefactFailedDepositState(this, depositable);
        default:
            throw new IllegalStateException(String.format("Illegal state type '%s' for depositable '%s'", type,
                    depositable.getClass().getSimpleName()).toString());
        }
    }

    /**
     * Create a suitable deposit state implementation object for the current state type of the Level 7 Collection
     * 
     * @param type
     *            the type of the state
     * @param depositable
     *            the Level 7 Collection being deposited
     * @return The DepositState instance
     */
    private DepositState createStateForLevel7Collection(DepositState.Type type, Level7Collection depositable)
    {
        switch (type)
        {
        case UNDEPOSITED:
            return new ParentDepositableArtefactUndepositedDepositState(this, depositable);
        case DEPOSITING:
            return new Level7DepositingDepositState(this, depositable);
        case CLEANUP:
            return new Level7CleanUpDepositState(this, (Level7Collection) depositable,
                    this.level7CollectionParentDirectory, this.voToolsService);
        case DEPOSITED:
            return new DepositedDepositState(this, depositable);
        case FAILED:
            return new ParentDepositableArtefactFailedDepositState(this, depositable);
        default:
            throw new IllegalStateException(String.format("Illegal state type '%s' for depositable '%s'", type,
                    depositable.getClass().getSimpleName()).toString());
        }

    }

    /**
     * Creates a ProcessJobBuilder for a given job type and command.
     * 
     * @param commandType
     *            used to determine which type of builder to create - SIMPLE creates a SimpleToolProcessJobBuilder,
     *            CASDA_TOOL creates a CasdaToolProcessJobBuilder
     * @param command
     *            the command string
     * @param commandArgs
     *            the command and args
     * @return the ProcessJobBuilder
     */
    public ProcessJobBuilder createProcessJobBuilderForProcessJobType(ProcessJobType commandType, String command,
            String commandArgs)
    {
        ProcessJobBuilder processJobBuilder = null;
        switch (commandType)
        {
        case SIMPLE:
            SimpleToolProcessJobBuilder simpleToolProcessJobBuilder =
                    new SimpleToolProcessJobBuilder(this.processJobFactory, Utils.elStringToArray(command));
            processJobBuilder = simpleToolProcessJobBuilder;
            break;
        case CASDA_TOOL:
            CasdaToolProcessJobBuilder casdaToolProcessJobBuilder = casdaToolBuilderFactory.createBuilder();
            casdaToolProcessJobBuilder.setCommand(command);
            casdaToolProcessJobBuilder.addCommandArguments(Utils.elStringToArray(commandArgs));
            processJobBuilder = casdaToolProcessJobBuilder;
            break;
        default:
            throw new RuntimeException(String.format("Unknown command type: '%s' (expected one of: %s", commandType,
                    StringUtils.join(ProcessJobType.values(), ", ")));
        }
        return processJobBuilder;
    }

}
