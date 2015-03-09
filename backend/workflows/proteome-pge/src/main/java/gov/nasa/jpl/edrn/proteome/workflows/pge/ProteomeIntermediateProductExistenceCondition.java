package gov.nasa.jpl.edrn.proteome.workflows.pge;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.WorkflowConditionConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowConditionInstance;

/**
 * Workflow condition that checks for the existence of output products
 * from the previous task before starting the nexy task.
 * Files are looked for in the DATA_STAGING directory, and sym-linked to the job directory.
 * 
 * @author Luca Cinquini
 *
 */
public class ProteomeIntermediateProductExistenceCondition implements WorkflowConditionInstance {
	
	private static final Logger LOG = Logger.getLogger(ProteomeIntermediateProductExistenceCondition.class.getName());

	@Override
	public boolean evaluate(Metadata metadata, WorkflowConditionConfiguration config) {
				
		// output directory == DATA_STAGING
		String outputDirectory = config.getProperty("outputDirectory");
		String suffix = config.getProperty("suffix");
		LOG.info("Searching outputDirectory: "+outputDirectory + " for "+suffix+" files");
		String jobDir = metadata.getMetadata("JobDir");

		// loop over expected output files from Myrimatch task
		List<String> rawFileNames = metadata.getAllMetadata("RAWFileNames");
		for (String rawFileName : rawFileNames) {
			
			String outputFileName = rawFileName.replace(".raw",suffix); 
			File outputFile = new File(outputDirectory, outputFileName);
			LOG.info("Checking for outputFile: "+outputFile.getAbsolutePath());
			
			if (!outputFile.exists()) {
				return false;
			} else {
				try{
					// symlink this file
					File symlinkedFile = new File(jobDir, outputFileName);
					Runtime.getRuntime().exec("ln -s " + outputFile.getAbsolutePath() + " " + symlinkedFile.getAbsolutePath());
				} catch (IOException e) {
					LOG.warning(e.getMessage());
					return false;
				}	
			}
			
		}
		
		// all files exist
		LOG.info("All output files exist, ProteomeIntermediateProductExistenceCondition succeded");
		return true;
		
	}

}
