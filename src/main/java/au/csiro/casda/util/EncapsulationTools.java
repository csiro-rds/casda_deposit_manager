package au.csiro.casda.util;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import au.csiro.casda.Utils;
import au.csiro.casda.entity.CasdaDepositableArtefactEntity;
import au.csiro.casda.jobmanager.ProcessJob;
import au.csiro.casda.jobmanager.ProcessJobBuilder.ProcessJobFactory;
import au.csiro.casda.jobmanager.SimpleToolProcessJobBuilder;

/**
 * A set of tools for dealing with files in encapsulations.
 * <p>
 * Copyright 2017, CSIRO Australia. All rights reserved.
 */
@Component
public class EncapsulationTools
{

    private String[] extractEncapsulatedFileCommand;

    private ProcessJobFactory processJobFactory;

    /**
     * Create a new instance of EncapsulationTools
     * 
     * @param extractEncapsulatedFileCommand
     *            The command to be used to extract a file from an encapsulation.
     * @param processJobFactory
     *            The factory to be used to create jobs.
     */
    @Autowired
    public EncapsulationTools(@Value("${extract.encapsulated.file.command}") String extractEncapsulatedFileCommand,
            ProcessJobFactory processJobFactory)
    {
        this.processJobFactory = processJobFactory;
        this.extractEncapsulatedFileCommand = Utils.elStringToArray(extractEncapsulatedFileCommand);

    }

    /**
     * Build up a ProcessJob which will extract a spectrum or moment map from an encapsulation file. The file command is
     * expected to handle extraction of both the file and its checksum and renaming it to match the NGAS file id.
     * 
     * @param jobId
     *            the id for the job (running in slurm)
     * @param depositable
     *            The depositable object to be extracted
     * @param encapsPath
     *            The path to the encapsulation
     * @param destFolder
     *            The path in which to store the depositable object
     * @return The job details.
     */
    public ProcessJob buildExtractJob(String jobId, CasdaDepositableArtefactEntity depositable, Path encapsPath,
            Path destFolder)
    {
        List<String> commandParts = new ArrayList<>();
        commandParts.addAll(Arrays.asList(extractEncapsulatedFileCommand));
        String fileNameInArchive = depositable.getFilename();
        commandParts.add(fileNameInArchive);
        commandParts.add(depositable.getFileId());

        SimpleToolProcessJobBuilder processBuilder =
                new SimpleToolProcessJobBuilder(this.processJobFactory, commandParts.toArray(new String[] {}));

        processBuilder.setProcessParameter("tarFileName", encapsPath.toString());

        processBuilder.setWorkingDirectory(destFolder.toString());

        return processBuilder.createJob(jobId, "Encapsulation");
    }

}
