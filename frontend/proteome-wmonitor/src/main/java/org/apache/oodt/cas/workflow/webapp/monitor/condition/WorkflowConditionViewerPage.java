/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.oodt.cas.workflow.webapp.monitor.condition;

import org.apache.oodt.cas.webcomponents.workflow.conditions.WorkflowConditionViewer;
import org.apache.oodt.cas.workflow.webapp.monitor.WMMonitorApp;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.protocol.http.WebResponse;

/**
 *
 * Describe your class here.
 *
 * @author mattmann
 * @version $Revision$
 *
 */
public class WorkflowConditionViewerPage extends WebPage {
  
   public WorkflowConditionViewerPage(PageParameters params){
     add(new Link("home_link"){
         /* (non-Javadoc)
         * @see org.apache.wicket.markup.html.link.Link#onClick()
         */
        @Override
        public void onClick() {
          setResponsePage(getApplication().getHomePage());
        }
     });
     add(new Label("cond_id", params.getString("id")));
     add(new WorkflowConditionViewer("cond_viewer", ((WMMonitorApp)getApplication()).getWorkflowUrl(), 
         params.getString("id")));
   }

   /**
    * Necessary to alleviate 'Page expired' caching issue.
    * See: http://www.richardnichols.net/2010/03/apache-wicket-force-page-reload-to-fix-ajax-back/
    */
   @Override
   protected void configureResponse() {
       super.configureResponse();
       WebResponse response = getWebRequestCycle().getWebResponse();
       response.setHeader("Cache-Control", 
             "no-cache, max-age=0,must-revalidate, no-store");
   } 
   
}
