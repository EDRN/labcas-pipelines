// Copyright 2013 California Institute of Technology. ALL RIGHTS
// RESERVED. U.S. Government Sponsorship acknowledged.
//
// $Id$

package gov.nasa.jpl.edrn.labcas.wmservice;

import gov.nasa.jpl.edrn.labcas.wmservice.metadata.WMServiceMetKeys;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.oodt.cas.metadata.Metadata;
import org.apache.oodt.cas.webcomponents.workflow.WorkflowMgrConn;
import org.apache.oodt.cas.workflow.structs.Workflow;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/")
public class WorkflowResource implements WMServiceMetKeys {

	private static Logger LOG = Logger.getLogger(WorkflowResource.class.getName());
	
	@Context private ServletContext context;
	@Context private UriInfo uriInfo;
	 
	@GET
	@Path("/events")
	@Produces("application/json")
	public String getEventList() {
		WorkflowMgrConn wm = new WorkflowMgrConn(
				context.getInitParameter(WMServiceMetKeys.WM_URL));
		
		JSONObject jsonObject = new JSONObject();
		try {
			List<String> eventNames = wm.safeGetRegisteredEvents();
			
			JSONArray eventsJSONArray = new JSONArray();
			for (String eventName : eventNames) {
				JSONObject eventJSONObj = new JSONObject();
				eventJSONObj.put(eventName, uriInfo.getAbsolutePath() + "/" + eventName);
				eventsJSONArray.put(eventJSONObj);
			}
			
			jsonObject.put("events", eventsJSONArray);		
		} catch (JSONException e) {
			LOG.severe(e.getMessage());
		}
		
		return jsonObject.toString();
	}
	
	@GET
	@Path("/events/{eventName}")
	@Produces("application/json")
	public String getEventInfo(@PathParam("eventName") String eventName) {
		if (eventName == null) {
			return Response.serverError().entity("eventName cannot be null").build().toString();
		} else if (eventName.trim().length() == 0) {
			return Response.serverError().entity("eventName cannot be empty").build().toString();
		}
		
		try {
		WorkflowMgrConn wm = new WorkflowMgrConn(
				context.getInitParameter(WMServiceMetKeys.WM_URL));
		
		List<Workflow> workflows = wm.safeGetWorkflowsByEvent(eventName);

		return "Workflow info for event: ["+eventName+"]: ["+workflows.toString()+"]";
		
		} catch (Exception e) {
			LOG.severe(e.getMessage());
			
			return "WorkflowManager could not be initalized or used to get event info";
		}
	}
	
	@POST
	@Path("/events/{eventName}/start")
	public Response startWorkflowEvent(@PathParam("eventName") String eventName,
			MultivaluedMap<String, String> metKeyVals) {
		if (eventName == null) {
			return Response.serverError().entity("eventName cannot be null").build();
		} else if (eventName.trim().length() == 0) {
			return Response.serverError().entity("eventName cannot be empty").build();
		}
		
		Metadata met = new Metadata();
		Iterator<String> keysIter = metKeyVals.keySet().iterator();
		while (keysIter.hasNext()) {
			String key = keysIter.next();
			List<String> vals = metKeyVals.get(key);
			
			met.addMetadata(key, vals);
			LOG.fine("Received KEY: ["+key+"], VAL: ["+vals.toString()+"]");
		}

		try {
			WorkflowMgrConn wm = new WorkflowMgrConn(
					context.getInitParameter(WMServiceMetKeys.WM_URL));

			wm.getWM().sendEvent(eventName, met);
			LOG.info("Launched workflow event ["+eventName+"] with metadata ["+met.getHashtable().toString()+"]");
			
			return Response.ok().build();
		} catch (Exception e) {
			LOG.severe(e.getMessage());
			
			return Response.serverError().entity("WorkflowManager could not be initalized or used to launch event").build();
		}
	
	}
}
