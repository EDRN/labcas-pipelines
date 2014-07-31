//Copyright (c) 2011, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.edrn.proteome.workflows.pge;

import java.io.File;
import java.util.logging.Level;

import gov.nasa.jpl.edrn.proteome.workflows.pge.util.CygwinScriptFile;

import org.apache.oodt.cas.pge.PGETaskInstance;
import org.apache.oodt.cas.pge.metadata.PgeTaskMetadataKeys;
import org.apache.oodt.cas.workflow.util.ScriptFile;

/**
 * A PGE Task for executing the windows-specific portions of the proteome pipeline
 * 
 * @author rverma
 *
 */
public class WinPGETask extends PGETaskInstance {
	  
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

	  /**
	   * An overriding buildPgeRunScript method to call a windows specific script file
	   */
	  @Override
	  protected ScriptFile buildPgeRunScript() {
		  CygwinScriptFile sf = new CygwinScriptFile(this.pgeConfig.getShellType());
	      sf.setCommands(this.pgeConfig.getExeCmds());
	   
	      return sf;
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
