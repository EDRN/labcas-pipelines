package gov.nasa.jpl.edrn.proteome.workflows.pge;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.WorkflowConditionConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowConditionInstance;

/**
 * Workflow condition that checks for the existence of output from the Myrimatch task
 * before staring the idpQonvert task.
 * Specifically, all needed files A1_frx02-MM.pepXML, A1_frx01-MM.pepXML
 * must exist in the DATA_STAGING directory.
 * 
 * @author Luca Cinquini
 *
 */
public class ProteomeFilteringCondition implements WorkflowConditionInstance {
	
	private static final Logger LOG = Logger.getLogger(ProteomeFilteringCondition.class.getName());

	@Override
	public boolean evaluate(Metadata metadata, WorkflowConditionConfiguration config) {
				
		// output directory == DATA_STAGING
		String outputDirectory = config.getProperty("outputDirectory");
		LOG.info("Searcging outputDirectory="+outputDirectory + " for .pepXML files");

		// loop over expected outpput files from Myrimatch task
		List<String> rawFileNames = metadata.getAllMetadata("RAWFileNames");
		for (String rawFileName : rawFileNames) {
			String outputFileName = outputDirectory + rawFileName.replace(".raw", "-MM.pepXML"); 
			File outputFile = new File(outputDirectory, outputFileName);
			LOG.info("Checking for outputFile"+outputFile.getAbsolutePath());
			if (!outputFile.exists()) return false;
		}
		
		// all files exist
		return true;
		
	}

}
