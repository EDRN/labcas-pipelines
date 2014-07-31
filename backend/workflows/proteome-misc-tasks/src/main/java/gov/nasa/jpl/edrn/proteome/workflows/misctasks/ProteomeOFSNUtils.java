//Copyright (c) 2011, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.edrn.proteome.workflows.misctasks;

import java.net.URL;

	public final class ProteomeOFSNUtils {
		
		public static final String RAW_RETURN_TYPE_EXTENSION = "+AND+RT%3DRAW";
		
		public static String extractRAWFilename(URL ofsn) {
			if (ofsn == null) 
				return "";
			
			String urlFileComponent = ofsn.getFile();
			int lastSlashIndex = urlFileComponent.lastIndexOf("/");
			int firstPlusIndex = urlFileComponent.indexOf(RAW_RETURN_TYPE_EXTENSION);
			
			// if ofsn is correctly formatted, parse out and return filename, otherwise return empty string
			if ( (lastSlashIndex > 0 && firstPlusIndex > 0) && (lastSlashIndex < firstPlusIndex) ) {
				return urlFileComponent.substring(lastSlashIndex + 1, firstPlusIndex);
			} else
				return "";
			
		}
}
