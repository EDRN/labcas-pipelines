#!/bin/bash
./wmgr-client --url $PROTEOME_WORKFLOW_URL --operation --sendEvent --eventName TabblabProteomicsPipeline \
--metaData \
--key RawDatasetName 20140506-Pancreatic-Cyst-Fluid \
--key PrimaryAnalysisDatasetName 0140506-Pancreatic-Cyst-Fluid-Processed \
--key RAWFiles f3f8255c-e270-4303-a69d-1d01b0c98f50 \
--key DBFile /usr/local/edrn/proteome/data/cystfluid/protein.db.fasta \
--key CntmsFile /usr/local/edrn/proteome/data/cystfluid/Cntms.fasta \
--key MyriMatchCfgFile /usr/local/edrn/proteome/data/cystfluid/cystfluid.myrimatch.cfg \
--key AssembleFileList /usr/local/edrn/proteome/data/cystfluid/Assembly.txt \
--key ProductName A1_frx01 \
--key CopyFiles false --key UserEmail luca.cinquini@jpl.nasa.gov
