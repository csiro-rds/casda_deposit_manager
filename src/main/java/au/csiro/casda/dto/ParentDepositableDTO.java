package au.csiro.casda.dto;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;

import au.csiro.casda.datadeposit.DepositableArtefact;
import au.csiro.casda.entity.observation.Catalogue;
import au.csiro.casda.entity.observation.CatalogueType;
import au.csiro.casda.entity.observation.ImageCube;
import au.csiro.casda.entity.observation.Level7Collection;
import au.csiro.casda.entity.observation.MeasurementSet;

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
 * Data Transfer Object encapsulating information about a CASDA ParentDepositableArtefact.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public class ParentDepositableDTO extends DepositableDTO implements Serializable
{
    private static final long serialVersionUID = -4973658371572565617L;

    private DepositableArtefactDTO[] depositableArtefacts;
    private String imageCubePath;
    private String spectrumPath;
    private String momentMapPath;
    private String cubeletPath;
    private List<String> errors;

    /**
     * Empty constructor, for JSON deserialisation
     */
    public ParentDepositableDTO()
    {
        this.depositableArtefacts = new DepositableArtefactDTO[0];
    }

    /**
     * Creates a ParentDepositableArtefactDTO for the given Level7Collection and collectionDirectory.
     * 
     * @param level7Collection
     *            a Level7Collection
     * @param collectionDirectory
     *            a File
     */
    public ParentDepositableDTO(Level7Collection level7Collection, File collectionDirectory)
    {
        this();
        this.setDepositState(DepositStateDTO.BUILDING_DEPOSIT);
        this.setDepositableTypeDescription("Level 7 Collection");
        Map<String, DepositableArtefactDTO> depositableArtefacts = new HashMap<>();
        if (collectionDirectory != null && collectionDirectory.exists())
        {
            for (File file : collectionDirectory.listFiles())
            {
                if (!file.getName().endsWith(".checksum"))
                {
                    DepositableArtefactDTO depositableArtefactDto = new DepositableArtefactDTO();
                    depositableArtefactDto.setDepositState(DepositStateDTO.BUILDING_DEPOSIT);
                    depositableArtefactDto.setDepositableTypeDescription(getLevel7CatalogueDescription());
                    depositableArtefactDto.setFilename(file.getName());
                    depositableArtefactDto.setFilesizeInBytes(file.length());
                    File checksumFile = new File(file.getParentFile(), file.getName() + ".checksum");
                    if (checksumFile.exists())
                    {
                        try (Stream<String> lines = Files.lines(checksumFile.toPath()))
                        {
                            depositableArtefactDto.setChecksum(lines.collect(Collectors.joining("\n")));
                        }
                        catch (IOException e)
                        {
                            throw new IllegalStateException(e);
                        }
                    }
                    depositableArtefacts.put(file.getName(), depositableArtefactDto);
                }
            }
        }
        if (level7Collection != null)
        {
            for (DepositableArtefact depositableArtefact : level7Collection.getDepositableArtefacts())
            {
                DepositableArtefactDTO depositableArtefactDto;
                if (depositableArtefacts.get(depositableArtefact.getFilename()) != null)
                {
                    // Use details found from existing file
                    depositableArtefactDto = depositableArtefacts.get(depositableArtefact.getFilename());
                }
                else
                {
                    depositableArtefactDto = new DepositableArtefactDTO();
                    depositableArtefactDto.setFilename(depositableArtefact.getFilename());
                    depositableArtefactDto.setDepositableTypeDescription(depositableArtefact
                            .getDepositableArtefactTypeDescription());
                    depositableArtefacts.put(depositableArtefact.getFilename(), depositableArtefactDto);
                }
                // Update deposit state (will always be more up-to-date than any matching file-generated DTO)
                depositableArtefactDto.setDepositState(DepositStateDTO.valueForDepositable(depositableArtefact));
                // Use any stored database value as the correct size
                if (getFilesizeForDepositableArtefact(depositableArtefact) != null)
                {
                    depositableArtefactDto.setFilesizeInBytes(getFilesizeForDepositableArtefact(depositableArtefact));
                }
            }
            this.setDepositState(DepositStateDTO.valueForDepositable(level7Collection));
            imageCubePath = level7Collection.getImageCubePath();
            spectrumPath = level7Collection.getSpectrumPath();
            momentMapPath = level7Collection.getMomentMapPath();
            setCubeletPath(level7Collection.getCubeletPath());
            errors = level7Collection.getErrors();
        }
        this.depositableArtefacts =
                new ArrayList<DepositableArtefactDTO>(depositableArtefacts.values())
                        .toArray(new DepositableArtefactDTO[0]);
    }

    /**
     * Creates a ParentDepositableArtefactDTO for the given collectionDirectory.
     * 
     * @param collectionDirectory
     *            a File
     */
    public ParentDepositableDTO(File collectionDirectory)
    {
        this(null, collectionDirectory);
    }

    public DepositableArtefactDTO[] getDepositableArtefacts()
    {
        return depositableArtefacts;
    }

    public void setDepositableArtefacts(DepositableArtefactDTO[] depositableArtefacts)
    {
        this.depositableArtefacts = depositableArtefacts;
    }

    /**
     * @param filename
     *            a String
     * @return a DepositableArtefactDTO for a DepositableArtefact with the given filename in this object (or null if
     *         there is no match)
     */
    @JsonIgnore
    public DepositableArtefactDTO getDepositableArtefactForFilename(String filename)
    {
        for (DepositableArtefactDTO depositableArtefactDTO : this.depositableArtefacts)
        {
            if (depositableArtefactDTO.getFilename().equals(filename))
            {
                return depositableArtefactDTO;
            }
        }
        return null;
    }

    private String getLevel7CatalogueDescription()
    {
        return new Catalogue(CatalogueType.DERIVED_CATALOGUE).getDepositableArtefactTypeDescription();
    }

    private Long getFilesizeForDepositableArtefact(DepositableArtefact depositableArtefact)
    {
        // TODO: Refactor this when we do CASDA-4537
        if (depositableArtefact instanceof Catalogue)
        {
            return ((Catalogue) depositableArtefact).getFilesize();
        }
        else if (depositableArtefact instanceof ImageCube)
        {
            return ((ImageCube) depositableArtefact).getFilesize();
        }
        else if (depositableArtefact instanceof MeasurementSet)
        {
            return ((MeasurementSet) depositableArtefact).getFilesize();
        }
        else
        {
            return null;
        }
    }

    public String getImageCubePath()
    {
        return imageCubePath;
    }

    public void setImageCubePath(String imageCubePath)
    {
        this.imageCubePath = imageCubePath;
    }

    public String getSpectrumPath()
    {
        return spectrumPath;
    }

    public void setSpectrumPath(String spectrumPath)
    {
        this.spectrumPath = spectrumPath;
    }

    public String getMomentMapPath()
    {
        return momentMapPath;
    }

    public void setMomentMapPath(String momentMapPath)
    {
        this.momentMapPath = momentMapPath;
    }

	public String getCubeletPath()
	{
		return cubeletPath;
	}

	public void setCubeletPath(String cubeletPath)
	{
		this.cubeletPath = cubeletPath;
	}

	public List<String> getErrors()
	{
		return errors;
	}

	public void setErrors(List<String> errors) 
	{
		this.errors = errors;
	}
}
