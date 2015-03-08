package gov.nasa.jpl.edrn.proteome.workflows.pge;

import java.util.List;
import java.util.logging.Logger;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.WorkflowConditionConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowConditionInstance;

public class ProteomeFilteringCondition implements WorkflowConditionInstance {
	
	private static final Logger LOG = Logger.getLogger(ProteomeFilteringCondition.class.getName());

	@Override
	public boolean evaluate(Metadata metadata, WorkflowConditionConfiguration config) {
		
		for (String key : metadata.getAllKeys()) {
			LOG.info("==> key: ["+key+"]     value: ["+metadata.getMetadata(key)+"]");
		}
		
		// retrieve job-level metadata
		List<String> rawFiles = metadata.getAllMetadata("RAWFiles");
		for (String rawFile : rawFiles) {
			LOG.info("Raw file="+rawFile);
		}
		List<String> rawFileNames = metadata.getAllMetadata("RAWFileNames");
		for (String rawFileName : rawFileNames) {
			LOG.info("Raw file name="+rawFileName);
		}
		
		// retrieve condition configuration
		String outputDirectory = config.getProperty("outputDirectory");
		LOG.info("outputDirectory="+outputDirectory);

		// loop over expected output files
//		Long now = System.currentTimeMillis();
//		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
//		for (int i=1; i<=ncv; i++) {
//			
//			String filepath = projectDirectory+"/results/cv/"+prefix+"_iter_"+i+".txt";
//			File file = new File(filepath);
//			Long fileLastModified = file.lastModified();
//			
//			LOG.info("Checking file: "+filepath+" last modified at:"+sdf.format(fileLastModified));
//			
//			// check existence
//			if (!file.exists()) return false;
//						
//		}
		
		// all files exist
		return false;
	}

}
