package gov.nasa.jpl.edrn.proteome.workflows.pge.met;

import java.io.File;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.metadata.extractors.FilenameTokenMetExtractor;
import org.apache.oodt.cas.pge.writers.PcsMetFileWriter;

public class FilenameMetExtractorWriter extends PcsMetFileWriter {

	@Override
	protected Metadata getSciPgeSpecificMetadata(File sciPgeCreatedDataFile,
			Metadata inputMetadata, Object... customArgs) throws Exception {

		inputMetadata.addMetadata("ProductType", inputMetadata.getMetadata("PrimaryAnalysisDatasetName"));
		
	    String metConfFilePath = String.valueOf(customArgs[0]);
	    FilenameTokenMetExtractor extractor = new FilenameTokenMetExtractor();
	    extractor.setConfigFile(metConfFilePath);
	    inputMetadata.addMetadata(extractor.extractMetadata(sciPgeCreatedDataFile));		
		
		return inputMetadata;
	}

}
