package gov.nasa.jpl.edrn.proteome.workflows.misctasks;

import gov.nasa.jpl.edrn.proteome.workflows.misctasks.metadata.ConfigKeys;
import gov.nasa.jpl.edrn.proteome.workflows.misctasks.metadata.MetKeys;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.logging.Logger;

import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;
import org.apache.oodt.cas.workflow.structs.exceptions.WorkflowTaskInstanceException;
import org.apache.oodt.commons.date.DateUtils;
import org.springframework.util.StringUtils;

public class AddDatasetMetadata implements WorkflowTaskInstance, MetKeys, ConfigKeys {

	private static final Logger LOG = Logger.getLogger(AddDatasetMetadata.class.getName());
	
	private static final String CPTAC_CLASSIFICATION_VALUE="Processed";
	
	public void run(Metadata met, WorkflowTaskConfiguration config)
			throws WorkflowTaskInstanceException {

		// Set up connection to FM
		XmlRpcFileManagerClient fmClient = null;
		String fmURLValue = config.getProperty(FILE_MANAGER_URL);
		try {
			URL fmURL = new URL(fmURLValue);
			fmClient = new XmlRpcFileManagerClient(fmURL);
			LOG.info("Successfully connected to CAS FileManager at URL ["+fmURL+"]");
		} catch (MalformedURLException e) {
			LOG.severe("CAS FileManager URL ["+fmURLValue+"] is invalid");
			LOG.severe(e.toString());
		} catch (ConnectionException e) {
			LOG.severe("Unable to connect to CAS FileManager at URL ["+fmURLValue+"]");
			LOG.severe(e.toString());
		}
		
		List rawFileProductNames = met.getAllMetadata(RAW_FILE_NAMES_MET_KEY);
		if (!rawFileProductNames.isEmpty()) {
			
	    	String rawFileProductNameString = (String) rawFileProductNames.get(0); // any prod is OK
	    	LOG.info("Examining RAW ProductName ["+rawFileProductNameString+"]");
	    	
	    	Product rawProd = null;
			try {
				rawProd = fmClient.getProductByName(rawFileProductNameString);
			} catch (CatalogException e) {
				LOG.severe("Unable to issue filemanager "+RAW_FILES_MET_KEY+" prod query");
				LOG.severe(e.toString());
				
				LOG.warning("Stopping "+AddDatasetMetadata.class.getName());
				return;
			}			
			
			Metadata rawProdMet = null;
			try {
				rawProdMet = fmClient.getMetadata(rawProd);
			} catch (CatalogException e) {
				LOG.severe("Unable to obtain metadata for "+RAW_FILES_MET_KEY+" file ["+rawFileProductNameString+"]");
				LOG.severe(e.toString());
				
				LOG.warning("Stopping "+AddDatasetMetadata.class.getName());
				return;
			}
			
			// add necessary met to workflow (check for nulls or empty strings)
			if (StringUtils.hasText(rawProdMet.getMetadata(MetKeys.DATASET_NAME)))
					met.addMetadata(MetKeys.DATASET_NAME, rawProdMet.getMetadata(MetKeys.DATASET_NAME) + "-" + CPTAC_CLASSIFICATION_VALUE); // need to customize dataset name
			try {
				met.addMetadata(MetKeys.DATASET_DATE, DateUtils.toString(DateUtils.getCurrentUtcTime()));
			} catch (Exception e) {
				LOG.warning("Unable to add met key ["+MetKeys.DATASET_DATE+"]");
				LOG.warning(e.getMessage());
			}
			met.addMetadata(MetKeys.CPTAC_CLASSIFICATION, CPTAC_CLASSIFICATION_VALUE);
			if (StringUtils.hasText(rawProdMet.getMetadata(MetKeys.LEAD_PI)))
				met.addMetadata(MetKeys.LEAD_PI, rawProdMet.getAllMetadata(MetKeys.LEAD_PI));
			if (StringUtils.hasText(rawProdMet.getMetadata(MetKeys.UPLOADED_BY)))
				met.addMetadata(MetKeys.UPLOADED_BY, rawProdMet.getMetadata(MetKeys.UPLOADED_BY));
			if (StringUtils.hasText(rawProdMet.getMetadata(MetKeys.ACCESS_GRANTED_TO)))
				met.addMetadata(MetKeys.ACCESS_GRANTED_TO, rawProdMet.getAllMetadata(MetKeys.ACCESS_GRANTED_TO));
		}
		
		for (String key : met.getAllKeys()) {
			LOG.info("==> key: ["+key+"]     value: ["+met.getMetadata(key)+"]");
		}
	}

}
