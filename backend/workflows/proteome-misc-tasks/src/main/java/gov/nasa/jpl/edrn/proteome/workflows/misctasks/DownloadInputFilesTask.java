//Copyright (c) 2011, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.edrn.proteome.workflows.misctasks;

// Proteome imports
import gov.nasa.jpl.edrn.proteome.workflows.misctasks.metadata.ConfigKeys;
import gov.nasa.jpl.edrn.proteome.workflows.misctasks.metadata.MetKeys;

// Java imports
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.Vector;
import java.util.logging.Logger;

// OODT imports
import org.apache.oodt.cas.filemgr.metadata.CoreMetKeys;
import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.Query;
import org.apache.oodt.cas.filemgr.structs.QueryCriteria;
import org.apache.oodt.cas.filemgr.structs.TermQueryCriteria;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.exceptions.QueryFormulationException;
import org.apache.oodt.cas.filemgr.structs.query.ComplexQuery;
import org.apache.oodt.cas.filemgr.structs.query.QueryResult;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import org.apache.oodt.cas.filemgr.util.SqlParser;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;
import org.apache.oodt.cas.workflow.structs.exceptions.WorkflowTaskInstanceException;
import org.apache.oodt.product.handlers.ofsn.util.OFSNUtils;


/**
 * OODT Workflow task which downloads remote proteome files to local destination
 * 
 * @author rverma
 *
 */
public class DownloadInputFilesTask implements WorkflowTaskInstance, MetKeys, ConfigKeys {

	private static final Logger LOG = Logger.getLogger(DownloadInputFilesTask.class.getName());	
	
	public void run(Metadata metadata, WorkflowTaskConfiguration config)
			throws WorkflowTaskInstanceException {
		
		// This will be our job ID overall
		String runIDString = metadata.getMetadata(RUNID_MET_KEY); 
		
		// Get files directory
		String jobDir = metadata.getMetadata(JOB_DIR_MET_KEY);
		
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
		
	    ProductType rawDatasetProductType = null;
	    try {
		    rawDatasetProductType = fmClient.getProductTypeByName(metadata.getMetadata(RAW_DATASET_NAME));	
		    LOG.info("Successfully obtained RAW dataset product type ["+rawDatasetProductType.getName()+"]");
	    } catch (Exception e) {
	    	LOG.severe("Unable to find dataset ["+metadata.getMetadata(RAW_DATASET_NAME)+"] within " + 
	    			"CAS FileManager");
	    	LOG.fine(e.toString());
	    }		
		
		// Obtain RAW files based on ProductId
		List rawFileProductIds = metadata.getAllMetadata(RAW_FILES_MET_KEY);
	    for (Iterator i = rawFileProductIds.iterator(); i.hasNext();) {
	    	String rawFileProductIdString = (String) i.next();
	    	LOG.info("Examining RAW ProductId ["+rawFileProductIdString+"]");
	    	
	    	Product rawProd = null;
			try {
				rawProd = fmClient.getProductById(rawFileProductIdString);
			} catch (CatalogException e) {
				LOG.severe("Unable to issue filemanager "+RAW_FILES_MET_KEY+" prod query for dataset [" + 
						rawDatasetProductType.getName()+"]");
				LOG.severe(e.toString());
				
				LOG.warning("Stopping "+DownloadInputFilesTask.class.getName());
				return;
			}
	    	
			Metadata rawProdMet = null;
			try {
				rawProdMet = fmClient.getMetadata(rawProd);
			} catch (CatalogException e) {
				LOG.severe("Unable to obtain metadata for "+RAW_FILES_MET_KEY+" file ["+rawFileProductIdString+"]");
				LOG.severe(e.toString());
				
				LOG.warning("Stopping "+DownloadInputFilesTask.class.getName());
				return;
			}
	    	
			String sourceFileLocation = rawProdMet.getMetadata("FileLocation");
			LOG.info(RAW_FILES_MET_KEY+" file ["+rawFileProductIdString+"] has FileLocation ["+sourceFileLocation+"]");
			String sourceFileName = rawProdMet.getMetadata("Filename");
			LOG.info(RAW_FILES_MET_KEY+" file ["+rawFileProductIdString+"] has Filename ["+sourceFileName+"]");
			String sourceFilePath = sourceFileLocation + File.separator + sourceFileName;

			LOG.info("Determined sourceFilePath ["+sourceFilePath+"] for raw file productId ["+rawFileProductIdString+"]");
			
	    	try {
				Runtime.getRuntime().exec("cp " + sourceFilePath + " " + jobDir);
				LOG.info("Successfully copied "+RAW_FILES_MET_KEY+" from sourceFilePath ["+sourceFilePath+"] to jobDir ["+jobDir+"]");
			} catch (IOException e) {
				LOG.warning("Unable to issue Runtime.exec to copy sourceFilePath ["+sourceFilePath+"] to jobDir ["+jobDir+"]");
				LOG.warning(e.getMessage());
				e.printStackTrace();
				
				LOG.warning("Stopping "+DownloadInputFilesTask.class.getName());
				return;
			}

	    	
	    	
	    	
	    	/*
	    	ComplexQuery complexQuery = new ComplexQuery();
	    	List<String> prodTypes = new Vector<String>();
	    	prodTypes.add(metadata.getMetadata(RAW_DATASET_NAME));
	    	complexQuery.setReducedProductTypeNames(prodTypes);
	    	try {
				complexQuery.addCriterion(SqlParser.parseSqlWhereClause("CAS.ProductId = '"+rawFileProductIdString+"'"));
			} catch (QueryFormulationException e1) {
				LOG.warning("Unable to create SQL query for obtaining RAW files");
				LOG.warning(e1.getMessage());
				e1.printStackTrace();
				
				LOG.warning("Stopping "+DownloadInputFilesTask.class.getName());
				return;
			}
	    	List<QueryResult> rawFiles = new Vector<QueryResult>();
	    	
			try {
				LOG.info("Issuing RAW file query ["+complexQuery.toString()+"]");
				rawFiles = fmClient.complexQuery(complexQuery);
				LOG.info("Successfully obtained RAW file ["+rawFiles.toString()+"] from ProductId ["+rawFileProductIdString+"]");
			} catch (CatalogException e) {
				LOG.severe("Unable to issue filemanager query for dataset [" + 
						rawDatasetProductType.getName()+"]");
				LOG.severe(e.toString());
				
				LOG.warning("Stopping "+DownloadInputFilesTask.class.getName());
				return;
			}
			LOG.info("Raw files from FM ["+rawFiles.toString()+"]");
			String sourceFileLocation = rawFiles.get(0).getMetadata().getMetadata("FileLocation");
			LOG.info("Raw file ["+rawFileProductIdString+"] has FileLocation ["+sourceFileLocation+"]");
			String sourceFileName = rawFiles.get(0).getMetadata().getMetadata("Filename");
			LOG.info("Raw file ["+rawFileProductIdString+"] has Filename ["+sourceFileName+"]");
			String sourceFilePath = sourceFileLocation + File.separator + sourceFileName;

			LOG.info("Determined sourceFilePath ["+sourceFilePath+"] for raw file productId ["+rawFileProductIdString+"]");
			
	    	try {
				Runtime.getRuntime().exec("cp " + sourceFilePath + " " + jobDir);
				LOG.info("Successfully copied RAW file from sourceFilePath ["+sourceFilePath+"] to jobDir ["+jobDir+"]");
			} catch (IOException e) {
				LOG.warning("Unable to issue Runtime.exec to copy sourceFilePath ["+sourceFilePath+"] to jobDir ["+jobDir+"]");
				LOG.warning(e.getMessage());
				e.printStackTrace();
				
				LOG.warning("Stopping "+DownloadInputFilesTask.class.getName());
				return;
			}
			*/
			
	    }
		
	    /*
	    // Download DBFile 
    	String dbFileProductIdString = metadata.getMetadata(DB_FILE_MET_KEY);
    	
    	ComplexQuery dbFilecomplexQuery = new ComplexQuery();
    	List<String> dbFileprodTypes = new Vector<String>();
    	dbFileprodTypes.add(metadata.getMetadata(RAW_DATASET_NAME));
    	dbFilecomplexQuery.setReducedProductTypeNames(dbFileprodTypes);
    	try {
			dbFilecomplexQuery.addCriterion(SqlParser.parseSqlWhereClause("CAS.ProductId = '"+dbFileProductIdString+"'"));
		} catch (QueryFormulationException e1) {
			LOG.warning("Unable to create SQL query for obtaining "+DB_FILE_MET_KEY+" files");
			LOG.warning(e1.getMessage());
			e1.printStackTrace();
			
			LOG.warning("Stopping "+DownloadInputFilesTask.class.getName());
			return;
		}
    	List<QueryResult> dbFileQueryResult = new Vector<QueryResult>();
    	
		try {
			LOG.info("Issuing "+DB_FILE_MET_KEY+"  file query ["+dbFilecomplexQuery.toString()+"]");
			dbFileQueryResult = fmClient.complexQuery(dbFilecomplexQuery);
			LOG.info("Successfully obtained "+DB_FILE_MET_KEY+"  file ["+dbFileQueryResult.toString()+"] from ProductId ["+dbFileProductIdString+"]");
		} catch (CatalogException e) {
			LOG.severe("Unable to issue filemanager query for dataset [" + 
					rawDatasetProductType.getName()+"]");
			LOG.severe(e.toString());
			
			LOG.warning("Stopping "+DownloadInputFilesTask.class.getName());
			return;
		}
		LOG.info(DB_FILE_MET_KEY+"  files from FM ["+dbFileQueryResult.toString()+"]");
		String dbFileSourceFileLocation = dbFileQueryResult.get(0).getMetadata().getMetadata("FileLocation");
		LOG.info(DB_FILE_MET_KEY+"  file ["+dbFileProductIdString+"] has FileLocation ["+dbFileSourceFileLocation+"]");
		String dbFileSourceFileName = dbFileQueryResult.get(0).getMetadata().getMetadata("Filename");
		LOG.info(DB_FILE_MET_KEY+"  file ["+dbFileProductIdString+"] has Filename ["+dbFileSourceFileName+"]");
		String dbFileSourceFilePath = dbFileSourceFileLocation + File.separator + dbFileSourceFileName;

		LOG.info("Determined sourceFilePath ["+dbFileSourceFilePath+"] for raw file productId ["+dbFileProductIdString+"]");
		
    	try {
			Runtime.getRuntime().exec("cp " + dbFileSourceFilePath + " " + jobDir);
			LOG.info("Successfully copied "+DB_FILE_MET_KEY+"  file from sourceFilePath ["+dbFileSourceFilePath+"] to jobDir ["+jobDir+"]");
		} catch (IOException e) {
			LOG.warning("Unable to issue Runtime.exec to copy sourceFilePath ["+dbFileSourceFilePath+"] to jobDir ["+jobDir+"]");
			LOG.warning(e.getMessage());
			e.printStackTrace();
			
			LOG.warning("Stopping "+DownloadInputFilesTask.class.getName());
			return;
		}   
	    
	    
		// Download CntmsFile
    	String cntmsFileOFSNString = metadata.getMetadata(CNTMS_FILE_OFSN_MET_KEY);
    	
    	ComplexQuery cntmsFileComplexQuery = new ComplexQuery();
    	List<String> cntmsFileProdTypes = new Vector<String>();
    	cntmsFileProdTypes.add(metadata.getMetadata(RAW_DATASET_NAME));
    	cntmsFileComplexQuery.setReducedProductTypeNames(cntmsFileProdTypes);
    	try {
			cntmsFileComplexQuery.addCriterion(SqlParser.parseSqlWhereClause("CAS.ProductId = '"+cntmsFileOFSNString+"'"));
		} catch (QueryFormulationException e1) {
			LOG.warning("Unable to create SQL query for obtaining "+DB_FILE_MET_KEY+" files");
			LOG.warning(e1.getMessage());
			e1.printStackTrace();
			
			LOG.warning("Stopping "+DownloadInputFilesTask.class.getName());
			return;
		}
    	List<QueryResult> cntmsFileQueryResult = new Vector<QueryResult>();
    	
		try {
			LOG.info("Issuing "+CNTMS_FILE_OFSN_MET_KEY+"  file query ["+cntmsFileComplexQuery.toString()+"]");
			cntmsFileQueryResult = fmClient.complexQuery(cntmsFileComplexQuery);
			LOG.info("Successfully obtained "+CNTMS_FILE_OFSN_MET_KEY+"  file ["+cntmsFileQueryResult.toString()+"] from ProductId ["+cntmsFileOFSNString+"]");
		} catch (CatalogException e) {
			LOG.severe("Unable to issue filemanager query for dataset [" + 
					rawDatasetProductType.getName()+"]");
			LOG.severe(e.toString());
			
			LOG.warning("Stopping "+DownloadInputFilesTask.class.getName());
			return;
		}
		LOG.info(CNTMS_FILE_OFSN_MET_KEY+"  files from FM ["+cntmsFileQueryResult.toString()+"]");
		String cntmsFileSourceFileLocation = cntmsFileQueryResult.get(0).getMetadata().getMetadata("FileLocation");
		LOG.info(CNTMS_FILE_OFSN_MET_KEY+"  file ["+cntmsFileOFSNString+"] has FileLocation ["+cntmsFileSourceFileLocation+"]");
		String cntmsFileSourceFileName = cntmsFileQueryResult.get(0).getMetadata().getMetadata("Filename");
		LOG.info(CNTMS_FILE_OFSN_MET_KEY+"  file ["+cntmsFileOFSNString+"] has Filename ["+cntmsFileSourceFileName+"]");
		String cntmsFileSourceFilePath = cntmsFileSourceFileLocation + File.separator + cntmsFileSourceFileName;

		LOG.info("Determined sourceFilePath ["+cntmsFileSourceFilePath+"] for raw file productId ["+cntmsFileOFSNString+"]");
		
    	try {
			Runtime.getRuntime().exec("cp " + cntmsFileSourceFilePath + " " + jobDir);
			LOG.info("Successfully copied "+CNTMS_FILE_OFSN_MET_KEY+"  file from sourceFilePath ["+cntmsFileSourceFilePath+"] to jobDir ["+jobDir+"]");
		} catch (IOException e) {
			LOG.warning("Unable to issue Runtime.exec to copy sourceFilePath ["+cntmsFileSourceFilePath+"] to jobDir ["+jobDir+"]");
			LOG.warning(e.getMessage());
			e.printStackTrace();
			
			LOG.warning("Stopping "+DownloadInputFilesTask.class.getName());
			return;
		}
		
	
		// Download MyriMatchConfigFile
    	String mmCfgFileOFSNString = metadata.getMetadata(MYRIMATCH_CONFIG_FILE_OFSN_MET_KEY);
    	
    	URL  mmCfgFileOFSN = null;
    	try {
    		 mmCfgFileOFSN = new URL( mmCfgFileOFSNString);
    	}  catch (MalformedURLException e) {
			LOG.severe("Malformed URL for MyriMatchCfgFile OFSN file: "+ mmCfgFileOFSNString);
			e.printStackTrace();
			return;
		} 
    	
    	String  mmCfgTargetFilePath = jobDir + File.separator + ProteomeOFSNUtils.extractRAWFilename(mmCfgFileOFSN);
    	File  mmCfgTargetFile = new File( mmCfgTargetFilePath);
    	try {
    		 mmCfgTargetFile.createNewFile();
		} catch (IOException e) {
			LOG.severe("Unable to create a local file for MyriMatchCfgFile OFSN ["+ mmCfgFileOFSNString+"]");
			e.printStackTrace();
			return;
		}
    	
		try {
			downloadOFSNFile(mmCfgFileOFSN,  mmCfgTargetFile);
		} catch (MalformedURLException e1) {
			LOG.severe(e1.getMessage());
			e1.printStackTrace();
		} catch (IOException e1) {
			LOG.severe(e1.getMessage());
			e1.printStackTrace();
		}	    				
		*/
		// Add RunID to met for later tasks to use
		metadata.addMetadata(RUNID_MET_KEY, runIDString);
		
	}
	
	/**
	 * Downloads a file described by an OFSN destination to a local file
	 * NOTE: the OFSN URL must be downloadable, ie. have return type of RAW
	 * 
	 * @param ofsn
	 * @param file
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	protected void downloadOFSNFile(URL ofsn, File file)  
		throws MalformedURLException, IOException {
		
		FileOutputStream byteArrayOutStream = new FileOutputStream(file);
		BufferedOutputStream buffOutStream = 
			new BufferedOutputStream(byteArrayOutStream, 1024);
		
		BufferedInputStream buffInStream = new BufferedInputStream(ofsn.openStream());
		
		byte[] buff = new byte[153600];
		int bytesRead;
		int totBytes = 0;
		LOG.info("Downloading OFSN file: "+ofsn.toString());
		
		while ( (bytesRead = buffInStream.read(buff)) > 0) {
			buffOutStream.write(buff, 0, bytesRead);
			buff = new byte[153600];
			totBytes += bytesRead;
		}
		
		LOG.fine("Downloaded ["+totBytes+"] bytes of OFSN file ["+ofsn.toString()+"]");
		LOG.info("Storing OFSN file ["+ofsn.toString()+"] at location ["+file.getAbsolutePath()+"]");
		
		buffOutStream.close();
		buffInStream.close();	
	}
	
}
