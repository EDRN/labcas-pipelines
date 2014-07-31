#!/bin/bash

# Start all LabCAS Linux Pipeline services
# 1. workflow manager
# 2. resource manager
# 3. resource manager linux batchstub
# 4. tomcat

cd $PROTEOME_WORKFLOW_HOME/bin
./wmgr start
cd $PROTEOME_RESOURCE_HOME/bin
./resmgr start
./batch_stub $PIPELINE_LINUX_BATCHSTUB_PORT
cd $PROTEOME_HOME/tomcat/bin
./startup.sh

echo ""
echo "---------------------------------------"
echo "Started LabCAS Linux Pipeline services:"
echo "---------------------------------------"
echo "1. Workflow Manager"
echo "2. Resource Manager"
echo "3. Resource Manager batchstub"
echo "4. Tomcat"