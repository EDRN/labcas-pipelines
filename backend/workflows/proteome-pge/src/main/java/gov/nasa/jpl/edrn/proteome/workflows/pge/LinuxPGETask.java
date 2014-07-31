//Copyright (c) 2011, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.edrn.proteome.workflows.pge;

import org.apache.oodt.cas.pge.PGETaskInstance;
import org.apache.oodt.cas.pge.metadata.PgeTaskMetadataKeys;

public class LinuxPGETask extends PGETaskInstance {
	  
	/* PGE task statuses */
	  public static final String STAGING_INPUT = "STAGING INPUT";
	  
	  public static final String CONF_FILE_BUILD = "BUILDING CONFIG FILE";
	  
	  public static final String RUNNING_PGE = "PGE EXEC";
	  
	  public static final String CRAWLING = "CRAWLING";

	  /* (non-Javadoc)
	   * @see org.apache.oodt.cas.pge.PGETaskInstance#updateStatus(java.lang.String)
	   */
	  @Override
	  protected void updateStatus(String status) {
	     String proteoStatus = this.convertStatus(status);
	     super.updateStatus(proteoStatus);
	  }

	  private String convertStatus(String casPgeStatus) {
	    if (casPgeStatus.equals(PgeTaskMetadataKeys.CONF_FILE_BUILD)) {
	        return CONF_FILE_BUILD;
	    } else if (casPgeStatus.equals(PgeTaskMetadataKeys.STAGING_INPUT)) {
	        return STAGING_INPUT;
	    } else if (casPgeStatus.equals(PgeTaskMetadataKeys.CRAWLING)) {
	        return CRAWLING;
	    } else if (casPgeStatus.equals(PgeTaskMetadataKeys.RUNNING_PGE)) {
	        return RUNNING_PGE;
	    } else
	        return casPgeStatus;
	}
	  
}
