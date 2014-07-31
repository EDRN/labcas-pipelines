package gov.nasa.jpl.edrn.proteome.workflows.pge;

import java.util.logging.Logger;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;
import org.apache.oodt.cas.workflow.structs.exceptions.WorkflowTaskInstanceException;

public class PipelineWinMetTest implements WorkflowTaskInstance {

	private static final Logger LOG = Logger.getLogger(PipelineWinMetTest.class.getName());	
	
	public void run(Metadata metadata, WorkflowTaskConfiguration config)
			throws WorkflowTaskInstanceException {
		LOG.info("START: PipelineWinMetTest");
		LOG.info("Metadata contents: ");
		for (String key : metadata.getAllKeys()) {
			LOG.info("key ["+key+"] has value ["+metadata.getMetadata(key)+"]");
		}
		LOG.info("about to add new metadata");
		metadata.addMetadata("PipelineWinMetTestKey", "PipelineWinMetTestVal");
		LOG.info("finished adding new metadata");
		LOG.info("Metadata contents: ");
		for (String key : metadata.getAllKeys()) {
			LOG.info("key ["+key+"] has value ["+metadata.getMetadata(key)+"]");
		}		
		LOG.info("END: PipelineWinMetTest");
	}

}
