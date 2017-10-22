package au.csiro.casda.entity;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import au.csiro.casda.datadeposit.AbstractDepositableArtefactTest;
import au.csiro.casda.datadeposit.ChildDepositableArtefact;
import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositState.Type;
import au.csiro.casda.datadeposit.DepositableArtefact;
import au.csiro.casda.entity.observation.Catalogue;
import au.csiro.casda.entity.observation.Cubelet;
import au.csiro.casda.entity.observation.EncapsulationFile;
import au.csiro.casda.entity.observation.EvaluationFile;
import au.csiro.casda.entity.observation.ImageCube;
import au.csiro.casda.entity.observation.MomentMap;
import au.csiro.casda.entity.observation.Observation;
import au.csiro.casda.entity.observation.ObservationMetadataFile;
import au.csiro.casda.entity.observation.Spectrum;
import au.csiro.casda.entity.observation.Thumbnail;

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
 * CasdaDepositableArtefactEntity Test
 * <p>
 * Copyright 2014, CSIRO Australia All rights reserved.
 */
public class CasdaDepositableArtefactEntityTest extends AbstractDepositableArtefactTest
{
	@Test
	public void encapsulatedFilesTransitionToDepositedWhenEncapsulatedFileDoesSo()
	{
		EncapsulationFile ef = createEncapsulationFile();
		ef.setDepositState(depositStateFactory.createState(Type.ARCHIVED, ef));
		
		for(ChildDepositableArtefact artefact : ef.getAllEncapsulatedArtefacts())
		{
			artefact.setDepositState(depositStateFactory.createState(Type.ENCAPSULATED, artefact));
		}
		
		ef.progressDeposit();
		
		for(ChildDepositableArtefact artefact : ef.getAllEncapsulatedArtefacts())
		{
			assertThat(artefact.isDeposited(), is(true));
		}
	}
	
	@Test
	public void encapsulatedFilesTransitionToDepositedCausesChildFilesToTransitionToEncapsulated()
	{
		EncapsulationFile ef = createEncapsulationFile();
		ef.setDepositState(depositStateFactory.createState(Type.PROCESSED, ef));
		
		for(ChildDepositableArtefact artefact : ef.getAllEncapsulatedArtefacts())
		{
			artefact.setDepositState(depositStateFactory.createState(Type.ENCAPSULATING, artefact));
		}
		
		ef.progressDeposit();
		
		for(ChildDepositableArtefact artefact : ef.getAllEncapsulatedArtefacts())
		{
			assertThat(artefact.isEncapsulated(), is(true));
		}
	}
	
	@Test
	public void encapsulatedFilesTransitionToProcessingWhenAllChildrenAreEncapsulating()
	{
		EncapsulationFile ef = createEncapsulationFile();
		ef.setDepositState(depositStateFactory.createState(Type.UNDEPOSITED, ef));
		
		//set all to encapsulating
		for(ChildDepositableArtefact artefact : ef.getAllEncapsulatedArtefacts())
		{
			artefact.setDepositState(depositStateFactory.createState(Type.ENCAPSULATING, ef));
		}	
		//set one file to processing so Encapsulation file cannot progress
		MomentMap mm = ef.getMomentMaps().get(1);
		mm.setDepositState(depositStateFactory.createState(Type.PROCESSING, mm));
		
		ef.progressDeposit();
		
		assertEquals(DepositState.Type.UNDEPOSITED, ef.getDepositStateType());
		
		//set to encapsulating so encapsulationFile can be progressed
		mm.setDepositState(depositStateFactory.createState(Type.ENCAPSULATING, mm));
		
		ef.progressDeposit();
		
		assertThat(ef.isProcessing(), is(true));
	}
	  
	@Test
	public void filesToBeEncapsulatedFollowDifferentPathFromOthers()
	{
		List<ChildDepositableArtefact> encapsulatedArtefacts= new ArrayList<ChildDepositableArtefact>();
		List<ChildDepositableArtefact> normalArtefacts= new ArrayList<ChildDepositableArtefact>();
        List<ChildDepositableArtefact> manualArtefacts= new ArrayList<ChildDepositableArtefact>();
        List<ChildDepositableArtefact> manualEncapsulatedArtefacts= new ArrayList<ChildDepositableArtefact>();
		
		ImageCube ic = new ImageCube();
		ic.setFilename("ic");
		ic.setParent(new Observation(1234));
		ic.setDepositState(depositStateFactory.createState(Type.PROCESSING, ic));
		manualArtefacts.add(ic);
		Thumbnail thumbExc = new Thumbnail();
		thumbExc.setFilename("thumb");
		thumbExc.setParent(new Observation(1234));
		thumbExc.setDepositState(depositStateFactory.createState(Type.PROCESSING, thumbExc));
		normalArtefacts.add(thumbExc);
		Catalogue cat = new Catalogue();
		cat.setFilename("cat");
		cat.setParent(new Observation(1234));
		cat.setDepositState(depositStateFactory.createState(Type.PROCESSING, cat));
		manualArtefacts.add(cat);
		EncapsulationFile encap = new EncapsulationFile();
		encap.setFilename("encap");
		encap.setParent(new Observation(1234));
		encap.setDepositState(depositStateFactory.createState(Type.PROCESSING, encap));
		manualArtefacts.add(encap);
		ObservationMetadataFile obsMetaData = new ObservationMetadataFile(new Observation(9999));
		obsMetaData.setParent(new Observation(1234));
		obsMetaData.setDepositState(depositStateFactory.createState(Type.PROCESSING, obsMetaData));
		normalArtefacts.add(obsMetaData);
		
		//test normal artefacts
		for(ChildDepositableArtefact artefact : normalArtefacts)
		{
			artefact.progressDeposit();
			assertThat(artefact.toString(), artefact.isProcessed(), is(true));
		}
		// Test and advance manual artefacts - these would be advanced by the batch import job
        for(ChildDepositableArtefact artefact : manualArtefacts)
        {
            artefact.progressDeposit();
            assertThat(artefact.toString(), artefact.isProcessing(), is(true));
            artefact.setDepositState(depositStateFactory.createState(Type.PROCESSED, artefact));
            assertThat(artefact.toString(), artefact.isProcessed(), is(true));
        }
		
		Spectrum spec = new Spectrum();
		spec.setFilename("spec1");
		spec.setParent(new Observation(1234));
		spec.setDepositState(depositStateFactory.createState(Type.PROCESSING, spec));
		spec.setImageCube(ic);
        manualEncapsulatedArtefacts.add(spec);
		MomentMap mm = new MomentMap();
		mm.setParent(new Observation(1234));
		mm.setFilename("mm1");
		mm.setDepositState(depositStateFactory.createState(Type.PROCESSING, mm));
        mm.setImageCube(ic);
		manualEncapsulatedArtefacts.add(mm);
		Thumbnail thumbInc = new Thumbnail();
		thumbInc.setParent(new Observation(1234));
		thumbInc.setFilename("thumb1");
		thumbInc.setEncapsulationFile(new EncapsulationFile());
		thumbInc.setDepositState(depositStateFactory.createState(Type.PROCESSING, thumbInc));
		encapsulatedArtefacts.add(thumbInc);//spectra & moment map thumbnails	
		EvaluationFile eval = new EvaluationFile();
		eval.setFilename("eval");
		eval.setFormat("pdf");
		eval.setParent(new Observation(1234));
		eval.setDepositState(depositStateFactory.createState(Type.PROCESSING, eval));
		EvaluationFile evalmetric = new EvaluationFile();
		evalmetric.setFilename("eval");
		evalmetric.setFormat("evaluation-metric");
		evalmetric.setParent(new Observation(1234));
		evalmetric.setDepositState(depositStateFactory.createState(Type.PROCESSING, evalmetric));
		encapsulatedArtefacts.add(evalmetric);	
		
		//test encapsulated artefacts
		for(ChildDepositableArtefact artefact : encapsulatedArtefacts)
		{
			artefact.progressDeposit();
			assertThat(artefact.toString(), artefact.isEncapsulating(), is(true));
		}
        // Test and advance manual encapsulated artefacts - these would be advanced by the batch import job
        for(ChildDepositableArtefact artefact : manualEncapsulatedArtefacts)
        {
            artefact.progressDeposit();
            assertThat(artefact.toString(), artefact.isProcessing(), is(true));
            artefact.setDepositState(depositStateFactory.createState(Type.ENCAPSULATING, artefact));
            assertThat(artefact.toString(), artefact.isEncapsulating(), is(true));
        }
	}
	
	@Test
	public void testEncapsulationProgressionChangingInResponseToEncapsulationFile()
	{
        Observation observation = new Observation(1234);
        observation.setDepositStateFactory(depositStateFactory);
        //start at priority depositing as encapulationfile and it children do nothing before now.
        observation.setDepositState(depositStateFactory.createState(DepositState.Type.UNDEPOSITED, observation));
        
        EncapsulationFile ef = new EncapsulationFile();
        ef.setFilename("filename.tar");
        ef.setDepositState(depositStateFactory.createState(DepositState.Type.UNDEPOSITED, ef));
        Spectrum spec = new Spectrum();
        spec.setFilename("spec.fits");
        spec.setDepositState(depositStateFactory.createState(DepositState.Type.UNDEPOSITED, spec));
        MomentMap mm = new MomentMap();
        mm.setFilename("mm.fits");
        mm.setDepositState(depositStateFactory.createState(DepositState.Type.UNDEPOSITED, mm));
        Cubelet cube = new Cubelet();
        cube.setFilename("cube.fits");
        cube.setDepositState(depositStateFactory.createState(DepositState.Type.UNDEPOSITED, cube));
        EvaluationFile eval = new EvaluationFile();
        eval.setFilename("eval1.fits");
        eval.setFormat("pdf");
        eval.setDepositState(depositStateFactory.createState(DepositState.Type.UNDEPOSITED, eval));
        EvaluationFile evalMetric = new EvaluationFile();
        evalMetric.setFilename("evalMetric.xml");
        evalMetric.setFormat("validation-metrics");
        evalMetric.setDepositState(depositStateFactory.createState(DepositState.Type.UNDEPOSITED, evalMetric));
        
        ef.addMomentMap(mm);
        ef.addCubelet(cube);
        ef.addSpectrum(spec);
        ef.addEvaluationFile(eval);
        ef.addEvaluationFile(evalMetric);
        observation.addEncapsulationFile(ef);
        observation.addSpectra(spec);
        observation.addMomentMap(mm);
        observation.addCubelet(cube);
        observation.addEvaluationFile(eval);
        observation.addEvaluationFile(evalMetric);
        
        //first run observation goes through to priority depositing
        observation.progressDeposit();
        assertThat(observation.isPriorityDepositing(), is(true));
        assertEquals(DepositState.Type.UNDEPOSITED, ef.getDepositStateType());
        assertEquals(DepositState.Type.UNDEPOSITED, spec.getDepositStateType());
        assertEquals(DepositState.Type.UNDEPOSITED, mm.getDepositStateType());
        assertEquals(DepositState.Type.UNDEPOSITED, cube.getDepositStateType());
        assertEquals(DepositState.Type.UNDEPOSITED, eval.getDepositStateType());
        assertEquals(DepositState.Type.UNDEPOSITED, evalMetric.getDepositStateType());
        
        //second run observation goes through to depositing (no image  cubes) 
        observation.progressDeposit();
        assertThat(observation.isDepositing(), is(true));
        assertEquals(DepositState.Type.UNDEPOSITED, ef.getDepositStateType());
        assertEquals(DepositState.Type.UNDEPOSITED, spec.getDepositStateType());
        assertEquals(DepositState.Type.UNDEPOSITED, mm.getDepositStateType());
        assertEquals(DepositState.Type.UNDEPOSITED, cube.getDepositStateType());
        assertEquals(DepositState.Type.UNDEPOSITED, eval.getDepositStateType());
        assertEquals(DepositState.Type.UNDEPOSITED, evalMetric.getDepositStateType());
        
        //third run moment map and spectrum transition to processing, encapsulation stays at undeposited
        observation.progressDeposit();
        assertThat(observation.isDepositing(), is(true));
        assertEquals(DepositState.Type.UNDEPOSITED, ef.getDepositStateType());
        assertThat(spec.isProcessing(), is(true));
        assertThat(mm.isProcessing(), is(true));
        assertThat(cube.isProcessing(), is(true));
        assertThat(eval.isProcessing(), is(true));
        assertThat(evalMetric.isProcessing(), is(true));
        
        // Manually advance the spectrum's status to reflect the job doing this
        spec.setDepositState(depositStateFactory.createState(DepositState.Type.ENCAPSULATING, spec));
        mm.setDepositState(depositStateFactory.createState(DepositState.Type.ENCAPSULATING, mm));
        cube.setDepositState(depositStateFactory.createState(DepositState.Type.ENCAPSULATING, cube));
        eval.setDepositState(depositStateFactory.createState(DepositState.Type.ENCAPSULATING, eval));
        evalMetric.setDepositState(depositStateFactory.createState(DepositState.Type.ENCAPSULATING, evalMetric));

        //fourth run moment map and spectrum have transitioned to encapsulating, so encapsulation file begins processing
        observation.progressDeposit();
        assertThat(observation.isDepositing(), is(true));
        assertEquals(DepositState.Type.PROCESSING, ef.getDepositStateType());
        assertThat(spec.getDepositStateType(), is(DepositState.Type.ENCAPSULATING));
        assertThat(spec.isEncapsulating(), is(true));
        assertThat(mm.isEncapsulating(), is(true));
        assertThat(cube.isEncapsulating(), is(true));
        assertThat(eval.isEncapsulating(), is(true));
        assertThat(evalMetric.isEncapsulating(), is(true));
        
        
        // Manually advance the encapsaultion's status to reflect the job doing this
        ef.setDepositState(depositStateFactory.createState(DepositState.Type.PROCESSED, ef));
        assertThat(observation.isDepositing(), is(true));
        assertThat(ef.isProcessed(), is(true));
        assertThat(spec.getDepositStateType(), is(DepositState.Type.ENCAPSULATING));
        assertThat(spec.isEncapsulating(), is(true));
        assertThat(mm.isEncapsulating(), is(true));
        assertThat(cube.isEncapsulating(), is(true));
        assertThat(eval.isEncapsulating(), is(true));
        assertThat(evalMetric.isEncapsulating(), is(true));

        //fifth run moment map and spectrum transition to encapsulated, encapsulation file transitions to processed
        observation.progressDeposit();
        assertThat(observation.isDepositing(), is(true));
        assertThat(ef.isStaging(), is(true));
        assertThat(spec.isEncapsulating(), is(false));
        assertThat(spec.getDepositStateType(), is(DepositState.Type.ENCAPSULATED));
        assertThat(spec.isEncapsulated(), is(true));
        assertThat(mm.isEncapsulated(), is(true));
        assertThat(cube.isEncapsulated(), is(true));
        assertThat(eval.isEncapsulated(), is(true));
        assertThat(evalMetric.isEncapsulated(), is(true));
        
        //sixth run moment map and spectrum remain at encapsulated, encapsulation file transitions to staged
        observation.progressDeposit();
        assertThat(observation.isDepositing(), is(true));
        assertThat(ef.isStaged(), is(true));
        assertThat(spec.isEncapsulated(), is(true));
        assertThat(mm.isEncapsulated(), is(true));
        assertThat(cube.isEncapsulated(), is(true));
        assertThat(eval.isEncapsulated(), is(true));
        assertThat(evalMetric.isEncapsulated(), is(true));
        
        //seventh run moment map and spectrum remain at encapsulated, encapsulation file transitions to registering
        observation.progressDeposit();
        assertThat(observation.isDepositing(), is(true));
        assertThat(ef.isRegistering(), is(true));
        assertThat(spec.isEncapsulated(), is(true));
        assertThat(mm.isEncapsulated(), is(true));
        assertThat(cube.isEncapsulated(), is(true));
        assertThat(eval.isEncapsulated(), is(true));
        assertThat(evalMetric.isEncapsulated(), is(true));
        
        //eighth run moment map and spectrum remain at encapsulated, encapsulation file transitions to registered
        observation.progressDeposit();
        assertThat(observation.isDepositing(), is(true));
        assertThat(ef.isRegistered(), is(true));
        assertThat(spec.isEncapsulated(), is(true));
        assertThat(mm.isEncapsulated(), is(true));
        assertThat(cube.isEncapsulated(), is(true));
        assertThat(eval.isEncapsulated(), is(true));
        assertThat(evalMetric.isEncapsulated(), is(true));
        
        //ninth run moment map and spectrum remain at encapsulated, encapsulation file transitions to archiving
        observation.progressDeposit();
        assertThat(observation.isDepositing(), is(true));
        assertThat(ef.isArchiving(), is(true));
        assertThat(spec.isEncapsulated(), is(true));
        assertThat(mm.isEncapsulated(), is(true));
        assertThat(cube.isEncapsulated(), is(true));
        assertThat(eval.isEncapsulated(), is(true));
        assertThat(evalMetric.isEncapsulated(), is(true));
        
        //tenth run moment map and spectrum remain at encapsulated, encapsulation file transitions to archived
        observation.progressDeposit();
        assertEquals(DepositState.Type.ARCHIVING, observation.getDepositStateType());
        assertThat(ef.isArchived(), is(true));
        assertThat(spec.isEncapsulated(), is(true));
        assertThat(mm.isEncapsulated(), is(true));
        assertThat(cube.isEncapsulated(), is(true));
        assertThat(eval.isEncapsulated(), is(true));
        assertThat(evalMetric.isEncapsulated(), is(true));
       
        //eleventh run moment map and spectrum transition to deposited, encapsulation file transitions to deposited
        observation.progressDeposit();
        assertEquals(DepositState.Type.ARCHIVING, observation.getDepositStateType());
        assertThat(ef.isDeposited(), is(true));
        assertThat(spec.isDeposited(), is(true));
        assertThat(mm.isDeposited(), is(true));
        assertThat(cube.isDeposited(), is(true));
        assertThat(eval.isDeposited(), is(true));
        assertThat(evalMetric.isDeposited(), is(true));
        
        //twelth run observation transitions to notifying
        observation.progressDeposit();
        assertThat(observation.isNotifying(), is(true));
        assertThat(ef.isDeposited(), is(true));
        assertThat(spec.isDeposited(), is(true));
        assertThat(mm.isDeposited(), is(true));
        assertThat(cube.isDeposited(), is(true));
        assertThat(eval.isDeposited(), is(true));
        assertThat(evalMetric.isDeposited(), is(true));
        
        //thirteenth run observation transitions to Deposited
        observation.progressDeposit();
        assertThat(observation.isDeposited(), is(true));
        assertThat(ef.isDeposited(), is(true));
        assertThat(spec.isDeposited(), is(true));
        assertThat(mm.isDeposited(), is(true));
        assertThat(cube.isDeposited(), is(true));
        assertThat(eval.isDeposited(), is(true));
        assertThat(evalMetric.isDeposited(), is(true));
	}
	
    /*
     * (non-Javadoc)
     * 
     * @see AbstractDepositableArtefactTest#createDepositableArtefact()
     */
    @Override
    protected DepositableArtefact createDepositableArtefact()
    {
        DummyObservationDepositableArtefact result =
                new DummyObservationDepositableArtefact(new Observation(1234), "filename.xml");
        result.setDepositStateFactory(depositStateFactory);
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see AbstractDepositableArtefactTest#createDepositableArtefactWithoutDepositStateFactory()
     */
    @Override
    protected DepositableArtefact createDepositableArtefactWithoutDepositStateFactory()
    {
        return new DummyObservationDepositableArtefact(new Observation(1235), "filename.xml");
    }

    /*
     * (non-Javadoc)
     * 
     * @see AbstractDepositableArtefactTest#createDepositableArtefactInState(DepositState.Type)
     */
    protected DepositableArtefact createDepositableArtefactInState(Type depositState, Type checkpointDepositStateType)
    {
        CasdaDepositableArtefactEntity depositableArtefact;
        depositableArtefact = new DummyObservationDepositableArtefact(new Observation(1234), "filename.xml");
        depositableArtefact.setDepositStateFactory(depositStateFactory);
        depositableArtefact.setDepositState(depositStateFactory.createState(depositState, depositableArtefact));
        depositableArtefact.setCheckpointStateType(checkpointDepositStateType);
        return depositableArtefact;
    }

    
    protected EncapsulationFile createEncapsulationFile()
    {
    	EncapsulationFile ef = new EncapsulationFile();
		ef.setFilename("filename.tar");
		ef.setParent(new Observation(1234));
		Cubelet cube1 = new Cubelet();
		Cubelet cube2 = new Cubelet();
		Cubelet cube3 = new Cubelet();
		MomentMap mm1 = new MomentMap();
		MomentMap mm2 = new MomentMap();
		MomentMap mm3 = new MomentMap();		
		Spectrum s1 = new Spectrum();
		Spectrum s2 = new Spectrum();
		Spectrum s3 = new Spectrum();		
		ef.addSpectrum(s1);
		ef.addSpectrum(s2);
		ef.addSpectrum(s3);
		ef.addMomentMap(mm1);
		ef.addMomentMap(mm2);
		ef.addMomentMap(mm3);
		ef.addCubelet(cube1);
		ef.addCubelet(cube2);
		ef.addCubelet(cube3);
		
		return ef;
    }
}
