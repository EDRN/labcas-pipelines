package gov.nasa.jpl.edrn.proteome.workflows.pge;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.WorkflowConditionConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowConditionInstance;

public class BiomarkerDiscoveryPostProcessCondition implements WorkflowConditionInstance {
	
	private static final Logger LOG = Logger.getLogger(BiomarkerDiscoveryPostProcessCondition.class.getName());

	@Override
	public boolean evaluate(Metadata metadata, WorkflowConditionConfiguration config) {
		
		for (String key : metadata.getAllKeys()) {
			LOG.info("==> key: ["+key+"]     value: ["+metadata.getMetadata(key)+"]");
		}
		
		// retrieve job-level metadata
		String projectDirectory = metadata.getMetadata("projectDirectory");
		String prefix = metadata.getMetadata("prefix");
		int ncv = Integer.parseInt( metadata.getMetadata("ncv") );
		
		// retrieve condition configuration
		int numSecs = Integer.parseInt( config.getProperty("numSecs") );
		
		// loop over expected output files
		Long now = System.currentTimeMillis();
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		for (int i=1; i<=ncv; i++) {
			
			String filepath = projectDirectory+"/results/cv/"+prefix+"_iter_"+i+".txt";
			File file = new File(filepath);
			Long fileLastModified = file.lastModified();
			
			LOG.info("Checking file: "+filepath+" last modified at:"+sdf.format(fileLastModified));
			
			// check existence
			if (!file.exists()) return false;
			
			// check last modification time
			if (fileLastModified > (now - numSecs*1000)) return false;
			
		}
		
		// all files exist
		return true;
	}

}
