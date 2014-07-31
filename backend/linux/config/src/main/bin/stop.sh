#!/bin/bash

# Stop all LabCAS Linux Pipeline services
# 1. workflow manager
# 2. resource manager
# 3. resource manager linux batchstub
# 4. tomcat

cd $PROTEOME_WORKFLOW_HOME/bin
./wmgr stop
cd $PROTEOME_RESOURCE_HOME/bin
./resmgr stop
batch_stub_ps_output=(`ps -ef | grep oodt | grep $PIPELINE_LINUX_BATCHSTUB_PORT`)
kill -9 ${batch_stub_ps_output[1]}
cd $PROTEOME_HOME/tomcat/bin
./shutdown.sh

echo ""
echo "---------------------------------------"
echo "Stopped LabCAS Linux Pipeline services:"
echo "---------------------------------------"
echo "1. Workflow Manager"
echo "2. Resource Manager"
echo "3. Resource Manager Batch Stub on port $PIPELINE_LINUX_BATCHSTUB_PORT"
echo "4. Tomcat"