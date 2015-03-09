//Copyright (c) 2011, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.edrn.proteome.workflows.misctasks.metadata;

public interface MetKeys {
    public static final String RUNID_MET_KEY = "RunID"; // temporary until a fix for passing met between tasks is found
    public static final String RAW_FILES_MET_KEY = "RAWFiles";    
    public static final String DB_FILE_MET_KEY = "DBFile";
    public static final String CNTMS_FILE_MET_KEY = "CntmsFile";
    public static final String MYRIMATCH_CONFIG_FILE_MET_KEY = "MyriMatchCfgFile";    
    public static final String IDPICKER_RESULT_FILES_MET_KEY = "IDPickerFiles";
    public static final String MYRIMATCH_RESULT_FILES_MET_KEY = "MyrimatchFiles";
    public static final String RESULTS_DIR_MET_KEY = "ResultsDir";
    public static final String RESULTS_DIR_OFSN_MET_KEY = "ResultsDirOFSN";
    public static final String IDPICKER_RESULT_OFSN_MEY_KEY = "IDPickerReportOFSN";
    public static final String ASSEMBLE_FILE_LIST_MET_KEY = "AssembleFileList";
    public static final String PROTEIN_FILE_MET_KEY = "ProteinFile";
    public static final String COPY_FILES_MET_KEY = "CopyFiles";
    
    public static final String JOB_DIR_MET_KEY = "JobDir";
    public static final String RAW_DATASET_NAME = "RawDatasetName";
    public static final String PRIMARY_ANALYSIS_DATASET_NAME = "PrimaryAnalysisDatasetName";
    
    public static final String DATASET_NAME="DatasetName";
    public static final String DATASET_DATE="DatasetDate";
    public static final String CPTAC_CLASSIFICATION="CPTACClassification";
    public static final String LEAD_PI="LeadPI";
    public static final String UPLOADED_BY="UploadedBy";
    public static final String ACCESS_GRANTED_TO="AccessGrantedTo";
    
    // pure workflow met
    public static final String RAW_FILES_NAMES_MET_KEY = "RAWFileNames";
    public static final String DB_FILE_NAME_MET_KEY = "DatabaseFile";
    public static final String CNTMS_FILE_NAME_MET_KEY = "ContaminantsFile";
    public static final String MYRIMATCH_FILE_NAME_MET_KEY = "MyrimatchConfigFile";
    public static final String FIRST_RAW_FILE_PRODUCT_ID_MET_KEY = "FirstRawFileProdID";
    public static final String RESULTS_PRODUCT_MET_KEY = "ResultsProduct";
    
}
