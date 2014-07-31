//Copyright (c) 2011, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.edrn.proteome.workflows.misctasks;

public class EmailNotifyTask {
	protected static String buildHTMLLink(String label, String href) {
		StringBuilder link = new StringBuilder();

		link.append("<a href=\"");
		link.append(href);
		link.append("\">");
		link.append(label);
		link.append("</a>");
		
		return link.toString();
	}
}
