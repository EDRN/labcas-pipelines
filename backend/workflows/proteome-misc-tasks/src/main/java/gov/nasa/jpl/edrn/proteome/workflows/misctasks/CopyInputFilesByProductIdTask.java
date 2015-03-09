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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.apache.oodt.cas.filemgr.structs.Product;
import org.apache.oodt.cas.filemgr.structs.ProductType;
import org.apache.oodt.cas.filemgr.structs.exceptions.CatalogException;
import org.apache.oodt.cas.filemgr.structs.exceptions.ConnectionException;
import org.apache.oodt.cas.filemgr.structs.exceptions.RepositoryManagerException;
import org.apache.oodt.cas.filemgr.system.XmlRpcFileManagerClient;
import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;
import org.apache.oodt.cas.workflow.structs.exceptions.WorkflowTaskInstanceException;
// OODT imports


/**
 * OODT Workflow task which copies local proteome files to local destination by productId
 * 
 * @author rverma
 *
 */
public class CopyInputFilesByProductIdTask implements WorkflowTaskInstance, MetKeys, ConfigKeys {

	private static final Logger LOG = Logger.getLogger(CopyInputFilesByProductIdTask.class.getName());	
	
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
		
    	// Check if files should be copied or linked
    	Boolean copyFiles = true;
    	if (metadata.getMetadata(COPY_FILES_MET_KEY) != null) {
    		if (metadata.getMetadata(COPY_FILES_MET_KEY).length() > 0) {
    			copyFiles = Boolean.parseBoolean(metadata.getMetadata(COPY_FILES_MET_KEY));
    		}
    	}
		
		// Obtain RAW files based on ProductName or ProductType
    	List rawFileProductNames = null;
    	if (metadata.getMetadata(RAW_FILE_NAMES_MET_KEY)!=null) {
    		rawFileProductNames = metadata.getAllMetadata(RAW_FILE_NAMES_MET_KEY);
    	} else {
    		rawFileProductNames = this.queryProductNamesForProductType(PRODUCT_TYPE_NAME, fmClient);
    	}
	    for (Iterator i = rawFileProductNames.iterator(); i.hasNext();) {
	    	String rawFileProductNameString = (String) i.next();
	    	LOG.info("Examining RAW ProductName ["+rawFileProductNameString+"]");

	    	Metadata rawProdMet = this.queryMetadataForProduct(rawFileProductNameString, fmClient);
	    	String rawSourceFilePath = rawProdMet.getMetadata("FileLocation") + File.separator + 
	    		rawProdMet.getMetadata("Filename");
	    	String rawDestFilePath = jobDir + File.separator + rawProdMet.getMetadata("Filename");	
	    	copyProduct(rawSourceFilePath, rawDestFilePath, copyFiles);
	    	
	    	// save filename for e-mail purposes
	    	//metadata.addMetadata(RAW_FILES_NAMES_MET_KEY, rawProdMet.getMetadata("Filename"));
	    	
	    	// save first RAW file product ID for extracting common met
	    	if (!metadata.containsKey(FIRST_RAW_FILE_PRODUCT_NAME_MET_KEY)) {
	    		metadata.addMetadata(FIRST_RAW_FILE_PRODUCT_NAME_MET_KEY, rawFileProductNameString);
	    	}
	    }
		
	    
	    // Obtain DBFile
    	LOG.info("Copying "+DB_FILE_MET_KEY+" ["+metadata.getMetadata(DB_FILE_MET_KEY)+"]");
    	File sourceDBFile = new File(metadata.getMetadata(DB_FILE_MET_KEY));
    	File destDBFile = new File(jobDir + File.separator + sourceDBFile.getName());
    	copyProduct(sourceDBFile.getAbsolutePath(), destDBFile.getAbsolutePath(), copyFiles);
    	metadata.addMetadata(DB_FILE_NAME_MET_KEY, sourceDBFile.getName());
	    
	    
	    // Obtain CntmsFile
    	LOG.info("Copying "+CNTMS_FILE_MET_KEY+" ["+metadata.getMetadata(CNTMS_FILE_MET_KEY)+"]");
    	File sourceCntmsFile = new File(metadata.getMetadata(CNTMS_FILE_MET_KEY));
    	File destCntmsFile = new File(jobDir + File.separator + sourceCntmsFile.getName());
    	copyProduct(sourceCntmsFile.getAbsolutePath(), destCntmsFile.getAbsolutePath(), copyFiles);
		metadata.addMetadata(CNTMS_FILE_NAME_MET_KEY, sourceCntmsFile.getName());
	
	    // Obtain MyrimatchCfgFile
    	LOG.info("Copying "+MYRIMATCH_CONFIG_FILE_MET_KEY+" ["+metadata.getMetadata(MYRIMATCH_CONFIG_FILE_MET_KEY)+"]");
    	File sourceMyrimatchCfgFile = new File(metadata.getMetadata(MYRIMATCH_CONFIG_FILE_MET_KEY));
    	File destMyrimatchCfgFile = new File(jobDir + File.separator + sourceMyrimatchCfgFile.getName());
    	copyProduct(sourceMyrimatchCfgFile.getAbsolutePath(), destMyrimatchCfgFile.getAbsolutePath(), copyFiles);
    	metadata.addMetadata(MYRIMATCH_FILE_NAME_MET_KEY, sourceMyrimatchCfgFile.getName());
		
	    // Obtain Assemble File List
    	LOG.info("Copying "+ASSEMBLE_FILE_LIST_MET_KEY+" ["+metadata.getMetadata(ASSEMBLE_FILE_LIST_MET_KEY)+"]");
    	File sourceAssembleListFile = new File(metadata.getMetadata(ASSEMBLE_FILE_LIST_MET_KEY));
    	File destAssembleListFile = new File(jobDir + File.separator + sourceAssembleListFile.getName());
    	copyProduct(sourceAssembleListFile.getAbsolutePath(), destAssembleListFile.getAbsolutePath(), copyFiles);
    	//metadata.addMetadata(ASSEMBLE_FILE_LIST_MET_KEY, sourceAssembleListFile.getName());
    	
    	// Obtain Protein file
    	LOG.info("Copying "+PROTEIN_FILE_MET_KEY+" ["+metadata.getMetadata(PROTEIN_FILE_MET_KEY)+"]");
    	File sourceProteinFile = new File(metadata.getMetadata(PROTEIN_FILE_MET_KEY));
    	File destProteinFile = new File(jobDir + File.separator + sourceProteinFile.getName());
    	copyProduct(sourceProteinFile.getAbsolutePath(), destProteinFile.getAbsolutePath(), copyFiles);
    	metadata.addMetadata(PROTEIN_FILE_MET_KEY, sourceMyrimatchCfgFile.getName());

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
	
	protected Metadata queryMetadataForProduct(String productName, XmlRpcFileManagerClient fmClient) {
		Metadata prodMet = new Metadata();
		
		LOG.info("Examining ProductName ["+productName+"]");
    	
    	Product prod = null;
		try {
			prod = fmClient.getProductByName(productName);
		} catch (CatalogException e) {
			LOG.severe("Unable to issue filemanager prod query for ProductName [" + 
					productName+"]");
			LOG.severe(e.toString());
		
			return prodMet;
		}
    	
		try {
			prodMet = fmClient.getMetadata(prod);
		} catch (CatalogException e) {
			LOG.severe("Unable to obtain metadata for Product ["+prod+"]");
			LOG.severe(e.toString());
			
			return prodMet;
		}
    	
		return prodMet;
	}
	
	protected List queryProductNamesForProductType(String produtTypeName, XmlRpcFileManagerClient fmClient) {
		
		LOG.info("Examining ProductTypeName ["+produtTypeName+"]");
		
		List<String> productNames = new ArrayList<String>();
		try {
			
			ProductType pt = fmClient.getProductTypeByName(produtTypeName);
			List<Product> products = fmClient.getProductsByProductType(pt);
			for (Product p : products) {
				productNames.add(p.getProductName());
			}
			
		} catch(CatalogException e) {
			LOG.severe("Unable to obtain ProductType for ProductName ["+produtTypeName+"]");
			LOG.severe(e.toString());
		} catch(RepositoryManagerException e) {
			LOG.severe("Unable to obtain ProductType for ProductName ["+produtTypeName+"]");
			LOG.severe(e.toString());			
		}
		
		return productNames;
		
	}
	
	protected void copyProduct(String sourcePath, String destPath, Boolean copyFiles) {

    	try {
    		if (copyFiles) {
    			Runtime.getRuntime().exec("cp " + sourcePath + " " + destPath);
    			LOG.info("Successfully copied sourcePath ["+sourcePath+"] to destPath ["+destPath+"]");  			
    		} else {
    			Runtime.getRuntime().exec("ln -s " + sourcePath + " " + destPath);
    			LOG.info("Successfully linked sourcePath ["+sourcePath+"] to destPath ["+destPath+"]");  			
    		}
		} catch (IOException e) {
			LOG.warning("Unable to issue Runtime.exec to copy sourcePath ["+sourcePath+"] to destPath ["+destPath+"]");
			LOG.warning(e.getMessage());
			e.printStackTrace();
		}
		
	}
}
