//Copyright (c) 2011, California Institute of Technology.
//ALL RIGHTS RESERVED. U.S. Government sponsorship acknowledged.
//
//$Id$

package gov.nasa.jpl.edrn.proteome.workflows.misctasks;

import gov.nasa.jpl.edrn.proteome.workflows.misctasks.metadata.MetKeys;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskConfiguration;
import org.apache.oodt.cas.workflow.structs.WorkflowTaskInstance;
import org.apache.oodt.cas.workflow.structs.exceptions.WorkflowTaskInstanceException;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

/**
 * OODT Workflow task which notifies a user via e-mail of a proteome job start
 * 
 * @author rverma
 *
 */
public class JobStartNotifyTask extends EmailNotifyTask implements WorkflowTaskInstance, MetKeys {

	private static final Logger LOG = Logger.getLogger(JobStartNotifyTask.class.getName());		
	
	public void run(Metadata metadata, WorkflowTaskConfiguration config)
			throws WorkflowTaskInstanceException {
	    Properties mailProps = new Properties();
	    mailProps.setProperty("mail.host", config.getProperty("mail.host"));

	    Session session = Session.getInstance(mailProps);
	    
	    // Add e-mail param information
	    Map emailParams = new HashMap();
	    
	    // run ID
	    emailParams.put(RUNID_MET_KEY, metadata.getMetadata(RUNID_MET_KEY));   
    	
	    // raw files
	    List rawFiles = metadata.getAllMetadata(RAW_FILE_NAMES_MET_KEY);
    	StringBuilder rawFilesStrBldr = new StringBuilder();
	    for (Iterator i = rawFiles.iterator(); i.hasNext();) {
	    	String rawFileName = (String) i.next();
	    	rawFilesStrBldr.append(rawFileName);
			rawFilesStrBldr.append("<br/>");    	
	    }    
	    emailParams.put(RAW_FILE_NAMES_MET_KEY, rawFilesStrBldr.toString());
	    
	    // db file
	    emailParams.put(DB_FILE_NAME_MET_KEY, 
	    		metadata.getMetadata(DB_FILE_NAME_MET_KEY));
	
		// contaminants file
	    emailParams.put(CNTMS_FILE_NAME_MET_KEY, 
	    		metadata.getMetadata(CNTMS_FILE_NAME_MET_KEY));

		
		// myrimatch config file
	    emailParams.put(MYRIMATCH_FILE_NAME_MET_KEY, 
	    		metadata.getMetadata(MYRIMATCH_FILE_NAME_MET_KEY));

		// assembly file (for idpAssembly)
	    emailParams.put(ASSEMBLE_FILE_LIST_MET_KEY,
	    		metadata.getMetadata(ASSEMBLE_FILE_LIST_MET_KEY));
	    		
	    // Load e-mail template file
	    VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());

        try {
			ve.init();
		} catch (Exception e2) {
			LOG.severe(e2.getMessage());
			e2.printStackTrace();
		}

        final String templatePath = "JobStartEmailTemplate.vm";
        InputStream input = this.getClass().getClassLoader().getResourceAsStream(templatePath);
        if (input == null) {
            LOG.severe("Could not find velocity template file: ["+templatePath+"]");
        }

        InputStreamReader reader = new InputStreamReader(input);

        VelocityContext context = new VelocityContext();
        context.put("emailParams", emailParams);
        
        Template template;
        StringWriter writer = new StringWriter();
		try {
			template = ve.getTemplate(templatePath, "UTF-8");
			
	        if (!ve.evaluate(context, writer, templatePath, reader)) {
	            LOG.severe("Failed to convert the velocity template into HTML.");
	        }	        

	        template.merge(context, writer);
     
		} catch (IOException e) {
			LOG.severe(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			LOG.severe(e.getMessage());
			e.printStackTrace();
		}

		// Send e-mail
	    Message msg = new MimeMessage(session);
	    try {
	      msg.setSubject("EDRN Proteome Job Notification: Started");
	      msg.setSentDate(new Date());
	      msg.setFrom(InternetAddress.parse(config.getProperty("mail.from"))[0]);
	      msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(
	          metadata.getMetadata("UserEmail"), false));
	      msg.setContent(writer.toString(), "text/html");
	      Transport.send(msg);

	    } catch (MessagingException e) {
	      throw new WorkflowTaskInstanceException(e.getMessage());
	    }
	}

}
