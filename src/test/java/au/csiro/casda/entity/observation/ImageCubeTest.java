package au.csiro.casda.entity.observation;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThanOrEqualTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

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
 * Test for the image cube class and its parent classes.
 * 
 * Copyright 2015, CSIRO Australia
 * All rights reserved.
 */
public class ImageCubeTest
{

    /**
     * Test method for {@link au.csiro.casda.entity.CasdaDepositableArtefactEntity#getUniqueIdentifier()}.
     */
    @Test
    public void testGetUniqueIdentifierShort()
    {
        ImageCube cube = new ImageCube();
        cube.setFilename("shortname.fits");
        cube.setId(2000L);
        Observation obs = new Observation(42);
        cube.setParent(obs);
        
        String uniqueId = cube.getUniqueIdentifier();
        assertThat(uniqueId.length(), lessThanOrEqualTo(64));
        assertThat(uniqueId, is("observations/42/image_cubes/shortname.fits"));
    }

    /**
     * Test method for {@link au.csiro.casda.entity.CasdaDepositableArtefactEntity#getUniqueIdentifier()}.
     */
    @Test
    public void testGetUniqueIdentifierExact()
    {
        ImageCube cube = new ImageCube();
        cube.setFilename("mediumname.part1.part2.part3aa.fits");
        cube.setId(2000L);
        Observation obs = new Observation(42);
        cube.setParent(obs);
        
        String uniqueId = cube.getUniqueIdentifier();
        assertThat(uniqueId.length(), lessThanOrEqualTo(64));
        assertThat(uniqueId, is("observations/42/image_cubes/mediumname.part1.part2.part3aa.fits"));
    }

    /**
     * Test method for {@link au.csiro.casda.entity.CasdaDepositableArtefactEntity#getUniqueIdentifier()}.
     */
    @Test
    public void testGetUniqueIdentifierLong()
    {
        ImageCube cube = new ImageCube();
        cube.setFilename("image.i.cdfs_full.linmos.taylor.0.restored.fits");
        cube.setId(2000L);
        Observation obs = new Observation(42);
        cube.setParent(obs);
        
        String uniqueId = cube.getUniqueIdentifier();
        assertThat(uniqueId.length(), lessThanOrEqualTo(64));
        assertThat(uniqueId, is("observations/42/image_cubes/2000.fits"));
    }

}
