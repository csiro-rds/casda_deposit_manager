package au.csiro.casda.deposit;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import au.csiro.casda.datadeposit.ChildDepositableArtefact;
import au.csiro.casda.datadeposit.DepositState;
import au.csiro.casda.datadeposit.DepositStateFactory;
import au.csiro.casda.entity.CasdaDataProductEntity;
import au.csiro.casda.entity.CasdaDepositableEntity;
import au.csiro.casda.entity.observation.ChildDepositableArtefactComparator;
import au.csiro.casda.entity.observation.EvaluationFile;
import au.csiro.casda.entity.observation.FitsObject;
import au.csiro.casda.entity.observation.Project;
import au.csiro.casda.entity.observation.ImageDerivedProduct;
import au.csiro.casda.entity.observation.Thumbnail;
/**
 * parent for the 'parent depositables' deposit controllers to allow for shared keys and methods
 * 
 * Copyright 2017, CSIRO Australia All rights reserved.
 * 
 */
public abstract class ParentDepositableController 
{
    /**
     * MVC Model key for failed Observations.
     */
    protected static final String FAILED_PARENT_DEPOSITABLES_MODEL_KEY = "failedParentDepositables";
    /**
     * MVC Model key for depositing Observations.
     */
    protected static final String DEPOSITING_PARENT_DEPOSITABLES_MODEL_KEY = "activeParentDepositables";
    
    /**
     * MVC Model key for validating Level 7 collections.
     */
    protected static final String VALIDATING_PARENT_DEPOSITABLES_MODEL_KEY = "validatingParentDepositables";
    /**
     * MVC Model key for deposited Observations.
     */
    protected static final String DEPOSITED_PARENT_DEPOSITABLES_MODEL_KEY = "completedParentDepositables";
/**
     * MVC Model key for maximum age of recently completed observations
     */
    protected static final String DEPOSITED_PARENT_DEPOSITABLES_MAX_AGE_MODEL_KEY = "depositedParentDepositablesMaximumAge"; 
    /**
     * MVC Model key for failed observation depositables.
     */
    protected static final String FAILED_DEPOSITABLES_MODEL_KEY = "parentDepositableWithFailures";
    
    /**
     * MVC Model key for level 7 collection depositable artefacts.
     */
    protected static final String DEPOSITABLE_ARTEFACTS_MODEL_KEY = "depositableArtefacts";
    /** MVC Model key for observation artefact summaries. */
    protected static final String ARTEFACT_SUMMARIES_MODEL_KEY = "artefactSummaries";
    /** MVC Model key for observation artefact summaries. */
    protected static final String ARTIFACT_FAILED_MODEL_KEY = "artefactFailed";
    /** Constant for the requested URL */
    protected static final String DEPOSIT_STATUS_URL = "url";
    
    /** comparator for child depositables */
    protected static final ChildDepositableArtefactComparator DEPOSITABLE_ARTEFACT_COMPARATOR =
            new ChildDepositableArtefactComparator();

    @Autowired
    private DepositStateFactory depositStateFactory;
    
    @Autowired
    @Value("${job.manager.recent.age.hours}")
    private int maxAgeOfRecentCompletedJobs;
    
    /**
     * 
     * @param depositableArtefacts the depositable artefacts to iterate through
     * @param depositableSummariesByStatus depositables summaries collected by status in a map
     * @return true if any of these child artefacts have failed. making this a failed deposit.
     */
    protected boolean parseChildArtefacts(List<ChildDepositableArtefact> depositableArtefacts, 
    		Map<String, DepositableStatusSummary> depositableSummariesByStatus)
    {
    	boolean artefactFailed = false;
    	// Pull out non-failed moment maps, spectra and thumbnails, and summarise them
        for (Iterator<ChildDepositableArtefact> iterator = depositableArtefacts.iterator(); iterator.hasNext();)
        {
        	ChildDepositableArtefact artefact = iterator.next();
            if(artefact.isFailedDeposit())
            {
            	artefactFailed = true;
            }
            if ((artefact instanceof ImageDerivedProduct || artefact instanceof Thumbnail 
            		|| artefact instanceof EvaluationFile)
                    && ((CasdaDepositableEntity) artefact).getDepositStateType() != DepositState.Type.FAILED)
            {
                Project project = null; 
                if(artefact instanceof FitsObject)
                {
                	project = ((FitsObject) artefact).getProject();
                }
                else if(artefact instanceof EvaluationFile)
                {
                	project = ((EvaluationFile) artefact).getProject();
                }

                String projectCode = project != null ? project.getOpalCode() : "N/A";
                String key = DepositableStatusSummary.getSummaryKey(artefact.getDepositableArtefactTypeDescription(),
                        artefact.getDepositStateDescription(), projectCode);

                DepositableStatusSummary summary = depositableSummariesByStatus.get(key);
                if (summary == null)
                {
                    summary = new DepositableStatusSummary(artefact.getDepositableArtefactTypeDescription(),
                            artefact.getDepositStateDescription(), project,
                            artefact.getCheckpointStateType().toString(), !(artefact instanceof Thumbnail));
                    depositableSummariesByStatus.put(key, summary);
                }
                summary.setNumArtefacts(summary.getNumArtefacts() + 1);
                DateTime prevChangedDate = summary.getDepositStateChanged();
                if (prevChangedDate == null || prevChangedDate.compareTo(artefact.getDepositStateChanged()) < 0)
                {
                    summary.setDepositStateChanged(artefact.getDepositStateChanged());
                }
                if (artefact instanceof EvaluationFile)
                {
                    DateTime artefactReleasedDate = ((EvaluationFile) artefact).getReleasedDate();
                    if (artefactReleasedDate != null)
                    {
                        summary.setReleasedDate(artefactReleasedDate);
                    }
                }
                if (artefact instanceof CasdaDataProductEntity)
                {
                    DateTime prevReleasedDate = summary.getReleasedDate();
                    DateTime artefactReleasedDate = ((CasdaDataProductEntity) artefact).getReleasedDate();
                    if (artefactReleasedDate != null
                            && (prevReleasedDate == null || prevReleasedDate.compareTo(artefactReleasedDate) < 0))
                    {
                        summary.setReleasedDate(artefactReleasedDate);
                    }
                }
                iterator.remove();
            }
        }
        return artefactFailed;
    }
    
    protected DepositStateFactory getDepositStateFactory()
    {
    	return depositStateFactory;
    }
    
    protected int getMaxAgeOfRecentCompletedJobs()
    {
    	return maxAgeOfRecentCompletedJobs;
    }
}
