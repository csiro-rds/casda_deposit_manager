package au.csiro.casda.deposit.state;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.AclEntry;
import java.nio.file.attribute.AclEntryPermission;
import java.nio.file.attribute.AclEntryType;
import java.nio.file.attribute.AclFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Arrays;
import java.util.EnumSet;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.SystemUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositedDepositState;
import au.csiro.casda.datadeposit.FailedDepositState;
import au.csiro.casda.deposit.services.VoToolsService;
import au.csiro.casda.entity.observation.Catalogue;
import au.csiro.casda.entity.observation.CatalogueType;
import au.csiro.casda.entity.observation.Level7Collection;

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
 * Level7CleanupDepositState test cases.
 * <p>
 * Copyright 2015, CSIRO Australia. All rights reserved.
 */
public class Level7CleanUpDepositStateTest
{
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Mock
    private CasdaDepositStateFactory depositStateFactory;

    private Level7Collection level7Collection;

    private Level7CleanUpDepositState depositState;

    private File level7CollectionParentDirectory;

    @Mock
    private VoToolsService voToolsService;

    @Before
    public void setUp() throws IOException
    {
        MockitoAnnotations.initMocks(this);
        level7CollectionParentDirectory = tempFolder.newFolder("foo");
        level7Collection = new Level7Collection(RandomUtils.nextLong(1, Long.MAX_VALUE));
        Catalogue one = new Catalogue(CatalogueType.LEVEL7);
        one.setEntriesTableName("casda.table1");
        Catalogue two = new Catalogue(CatalogueType.LEVEL7);
        two.setEntriesTableName("casda.table1");
        level7Collection.addCatalogue(one);
        level7Collection.addCatalogue(two);
        depositState =
                new Level7CleanUpDepositState(depositStateFactory, level7Collection,
                        level7CollectionParentDirectory.getPath(), voToolsService);
    }

    @Test
    public void testProgressShouldFailWhenResettingVoMetadataFails()
    {
        RuntimeException exception = new RuntimeException("weee!");
        doThrow(exception).when(voToolsService).resetVoTapMetadata();
        doThrow(exception).when(voToolsService).resetVoScsMetadata();
        when(depositStateFactory.createState(DepositState.Type.FAILED, level7Collection)).thenReturn(
                new FailedDepositState(depositStateFactory, level7Collection));

        depositState.progress();
        assertThat(level7Collection.isFailedDeposit(), is(true));
        verify(depositStateFactory).createState(DepositState.Type.FAILED, level7Collection);
        verifyNoMoreInteractions(depositStateFactory);
    }
    
    @Test
    public void testProgressShouldSucceedWithMissingCollectionDirectory()
    {
        when(depositStateFactory.createState(DepositState.Type.DEPOSITED, level7Collection)).thenReturn(
                new DepositedDepositState(depositStateFactory, level7Collection));

        depositState.progress();
        assertThat(level7Collection.isDeposited(), is(true));
        verify(depositStateFactory).createState(DepositState.Type.DEPOSITED, level7Collection);
        verifyNoMoreInteractions(depositStateFactory);
    }

    @Test
    public void testProgressWithUnprocessableCollectionDirectory() throws IOException
    {
        level7CollectionParentDirectory.mkdirs();
        assertTrue(level7CollectionParentDirectory.isDirectory());

        File level7CollectionDirectory =
                new File(level7CollectionParentDirectory, Long.toString(level7Collection.getDapCollectionId()));
        level7CollectionDirectory.mkdir();
        assertTrue(level7CollectionDirectory.isDirectory());

        if (SystemUtils.IS_OS_WINDOWS)
        {
            AclFileAttributeView aclAttr =
                    Files.getFileAttributeView(level7CollectionDirectory.toPath(), AclFileAttributeView.class);
            /*
             * This feels like a bit of a hack to make the directory non-processable. I was trying to make it
             * non-deletable but on Windows that doesn't work. Setting the permissions as below ends up making the
             * directory unreadable which is good enough for the test case.
             */
            UserPrincipalLookupService upls =
                    level7CollectionDirectory.toPath().getFileSystem().getUserPrincipalLookupService();
            UserPrincipal user = upls.lookupPrincipalByName(System.getProperty("user.name"));
            AclEntry.Builder denyBuilder = AclEntry.newBuilder();
            denyBuilder.setPermissions((EnumSet.of(AclEntryPermission.DELETE)));
            denyBuilder.setType(AclEntryType.DENY);
            denyBuilder.setPrincipal(user);
            aclAttr.setAcl(Arrays.asList(denyBuilder.build()));
        }
        else
        {
            Files.setPosixFilePermissions(level7CollectionParentDirectory.toPath(),
                    EnumSet.complementOf(EnumSet.of(PosixFilePermission.OTHERS_WRITE, PosixFilePermission.GROUP_WRITE,
                            PosixFilePermission.OWNER_WRITE)));
        }
        Assert.assertFalse("Should not have been able to delete: " + level7CollectionDirectory.toString(),
                level7CollectionDirectory.delete());

        when(depositStateFactory.createState(DepositState.Type.FAILED, level7Collection)).thenReturn(
                new FailedDepositState(depositStateFactory, level7Collection));

        depositState.progress();
        assertThat(level7Collection.isFailedDeposit(), is(true));
        verify(depositStateFactory).createState(DepositState.Type.FAILED, level7Collection);
        verifyNoMoreInteractions(depositStateFactory);
    }

    @Test
    public void testProgress()
    {
        level7CollectionParentDirectory.mkdirs();
        assertTrue(level7CollectionParentDirectory.isDirectory());

        File level7CollectionDirectory =
                new File(level7CollectionParentDirectory, Long.toString(level7Collection.getDapCollectionId()));
        level7CollectionDirectory.mkdir();
        assertTrue(level7CollectionDirectory.isDirectory());

        when(depositStateFactory.createState(DepositState.Type.DEPOSITED, level7Collection)).thenReturn(
                new DepositedDepositState(depositStateFactory, level7Collection));

        depositState.progress();
        assertThat(level7Collection.isDeposited(), is(true));
        verify(depositStateFactory).createState(DepositState.Type.DEPOSITED, level7Collection);
        verifyNoMoreInteractions(depositStateFactory);
        verify(voToolsService).resetVoTapMetadata();
        verify(voToolsService).resetVoScsMetadata();
        assertThat(level7CollectionDirectory.isDirectory(), is(false));
    }
}
