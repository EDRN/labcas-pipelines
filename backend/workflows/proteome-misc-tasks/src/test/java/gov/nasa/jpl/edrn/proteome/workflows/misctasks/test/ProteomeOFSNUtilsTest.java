//Copyright (c) 2011, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.edrn.proteome.workflows.misctasks.test;

import gov.nasa.jpl.edrn.proteome.workflows.misctasks.DownloadInputFilesTask;
import gov.nasa.jpl.edrn.proteome.workflows.misctasks.ProteomeOFSNUtils;

import java.net.MalformedURLException;
import java.net.URL;

import junit.framework.TestCase;

public class ProteomeOFSNUtilsTest extends TestCase {

	public void testExtractRAWFilename() throws MalformedURLException {
		
		// Case 1
		URL ofsn1 = new URL("http://biomarker.jpl.nasa.gov:8080/grid/prod?q=OFSN=/iprg2009/tranche/sh_072808p_E_coli_ABRF_red_080729113858.RAW+AND+RT%3DRAW");
		String expResult1 = "sh_072808p_E_coli_ABRF_red_080729113858.RAW";
		String actResult1 = ProteomeOFSNUtils.extractRAWFilename(ofsn1);
		assertEquals(expResult1, actResult1);
		
		// Case 2
		URL ofsn2 = new URL("http://biomarker.jpl.nasa.gov:8080/grid/prod?q=OFSN=/iprg2009/fasta/Cntms.fasta+AND+RT%3DRAW");
		String expResult2 = "Cntms.fasta";
		String actResult2 = ProteomeOFSNUtils.extractRAWFilename(ofsn2);
		assertEquals(expResult2, actResult2);
		
		// Case 3
		URL ofsn3 = new URL("http://biomarker.jpl.nasa.gov:8080/grid/prod?q=OFSN=/iprg2009/myrimatch/MyriMatch.cfg+AND+RT%3DRAW");
		String expResult3 = "MyriMatch.cfg";
		String actResult3 = ProteomeOFSNUtils.extractRAWFilename(ofsn3);
		assertEquals(expResult3, actResult3);
		
		// Case 4
		URL ofsn4 = new URL("http://biomarker.jpl.nasa.gov:8080/grid/prod?q=OFSN=/iprg2009/fasta/uniprot-organism-ecoli.fasta+AND+RT%3DRAW");
		String expResult4 = "uniprot-organism-ecoli.fasta";
		String actResult4 = ProteomeOFSNUtils.extractRAWFilename(ofsn4);
		assertEquals(expResult4, actResult4);
				
		// Case 5
		URL ofsn5 = new URL("http://www.google.com");
		String expResult5 = "";
		String actResult5 = ProteomeOFSNUtils.extractRAWFilename(ofsn5);
		assertEquals(expResult5, actResult5);	
		
	}

}
