package gov.nasa.jpl.edrn.proteome.workflows.pge;

import gov.nasa.jpl.edrn.proteome.workflows.pge.met.PipelineMetKeys;

import java.io.File;
import java.util.Collection;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;
import org.apache.oodt.cas.workflow.structs.exceptions.WorkflowTaskInstanceException;

public class SolrIndexerTask implements WorkflowTaskInstance, PipelineMetKeys {

	private static final Logger LOG = Logger.getLogger(SolrIndexerTask.class.getName());
	
	public void run(Metadata met, WorkflowTaskConfiguration conf)
			throws WorkflowTaskInstanceException {
		
		for (String key : met.getAllKeys()) {
			LOG.info("==> key: ["+key+"]     value: ["+met.getMetadata(key)+"]");
		}
		
		try {
			File resultsDir = new File(met.getMetadata(PipelineMetKeys.RESULTS_DIR));
			
			if (resultsDir.isDirectory()) {
				Collection resultsDirFilesCollection = 
					FileUtils.listFiles(resultsDir, FileFileFilter.FILE, null);
				
				Object[] resultsDirFiles = resultsDirFilesCollection.toArray();

				 for (int iter=0; iter < resultsDirFiles.length; iter++) {
					 File resultsDirFile = (File) resultsDirFiles[iter];
					 LOG.info("Found results file: ["+resultsDirFile.getName()+"]");
				 } 					
				 
			} else {
				LOG.warning(PipelineMetKeys.RESULTS_DIR+ "["+resultsDir.getAbsolutePath()+"] is not a directory!");
			}
		} catch (NullPointerException e) {
			LOG.severe("Unable to obtain "+PipelineMetKeys.RESULTS_DIR+
					" met contents, metadata key ["+PipelineMetKeys.RESULTS_DIR+"] is invalid");
		}
		
		/*
		Collection productFilesCollection = 
			FileUtils.listFiles(labcasDatasetMetFile.getParentFile(), PROD_FILE, TrueFileFilter.INSTANCE);
		Object[] productFiles = productFilesCollection.toArray();

		 for (int iter=0; iter < productFiles.length; iter++) {
			 File productFile = (File) productFiles[iter];
			 try {
				 this.generateProdMet(labcasDatasetMet, productFile);
			 } catch (Exception e) {
                LOG.warning("Failed to process file : " + e.getMessage());
			 }
		 } 	
		 */
	}

}
