package gov.nasa.jpl.edrn.proteome.workflows.misctasks;

import java.io.File;
import java.util.UUID;

import gov.nasa.jpl.edrn.proteome.workflows.misctasks.metadata.ConfigKeys;
import gov.nasa.jpl.edrn.proteome.workflows.misctasks.metadata.MetKeys;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;
import org.apache.oodt.cas.workflow.structs.exceptions.WorkflowTaskInstanceException;

public class SetupPipelineTask implements WorkflowTaskInstance, MetKeys, ConfigKeys {

	public void run(Metadata metadata, WorkflowTaskConfiguration config)
			throws WorkflowTaskInstanceException {

		// This will be our job ID overall
		UUID runID = UUID.randomUUID();
		String runIDString = runID.toString(); 
		
		// Create job directory
		String jobDir = config.getProperty(JOB_CONTAINER_DIR_CONFIG_PROP_KEY) + 
			File.separator + runIDString;
		new File(jobDir).mkdir();

		// Add to workflow met
		metadata.addMetadata(JOB_DIR_MET_KEY, jobDir);
		metadata.addMetadata(RUNID_MET_KEY, runIDString);		
		
	}

}
