#!/bin/bash

# Start all LabCAS Windows Pipeline services
# 1. resource manager win batchstub

cd $PROTEOME_RESOURCE_WIN/bin
./batch_stub $PIPELINE_WIN_BATCHSTUB_PORT

echo ""
echo "-----------------------------------------"
echo "Started LabCAS Windows Pipeline services:"
echo "-----------------------------------------"
echo "1. Resource Manager batchstub"