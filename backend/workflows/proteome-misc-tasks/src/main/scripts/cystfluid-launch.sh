#!/bin/bash
./wmgr-client --url $PROTEOME_WORKFLOW_URL --operation --sendEvent --eventName TabblabProteomicsPipeline \
--metaData \
--key RawDatasetName 20140506-Pancreatic-Cyst-Fluid \
--key PrimaryAnalysisDatasetName 0140506-Pancreatic-Cyst-Fluid-Processed \
--key DBFile /usr/local/edrn/proteome/pipeline-tools/tabblab/protein.db.fasta \
--key CntmsFile /usr/local/edrn/proteome/pipeline-tools/tabblab/Cntms.fasta \
--key MyriMatchCfgFile /usr/local/edrn/proteome/pipeline-tools/tabblab/cystfluid.myrimatch.cfg \
--key AssembleFileList /usr/local/edrn/proteome/pipeline-tools/tabblab/Assembly.txt \
--key ProteinFile /usr/local/edrn/proteome/pipeline-tools/tabblab/protein.fa \
--key CopyFiles false \
--key UserEmail luca.cinquini@jpl.nasa.gov
#--key RAWFileNames A1_frx01.raw A1_frx02.raw A1_frx03.raw