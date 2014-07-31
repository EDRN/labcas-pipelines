//Copyright (c) 2011, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.edrn.proteome.workflows.pge.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.List;

import org.apache.oodt.cas.workflow.util.ScriptFile;

/**
 * A Windows  compatible (cygwin) specific script file
 * 
 * @author rverma
 *
 */
public class CygwinScriptFile extends ScriptFile {

    /**
     * 
     */
    public CygwinScriptFile() {
        super();
    }

    public CygwinScriptFile(String shell) {
        super(shell);
    }

    public CygwinScriptFile(String shell, List cmds) {
        super(shell, cmds);
    }	
	
	@Override
    public void writeScriptFile(String filePath) throws Exception {
        PrintWriter pw = null;

        try {
            pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(
                    new File(filePath))));
            pw.print(toString()); // Changed println to print for Cygwin compatibility
        } catch (IOException e) {
            e.printStackTrace();
            throw new Exception("Error writing script file!: " + e.getMessage());
        } finally {
            try {
                pw.close();
                pw = null;
            } catch (Exception ignore) {
            }

        }

    }
	
}
