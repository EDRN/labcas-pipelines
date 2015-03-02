#!/bin/bash
# requires the following files in /usr/local/edrn/proteome/data/cystfluid :
#-rw-r--r-- 1 edrn staff       14 Mar  2 03:50 Assembly.txt
#-rw-r--r-- 1 edrn staff    31139 Mar  2 03:49 Cntms.fasta
#-rw-r--r-- 1 edrn staff      751 Mar  2 03:49 cystfluid.myrimatch.cfg
#-rw-r--r-- 1 edrn staff 53475917 Mar  2 03:49 protein.db.fasta
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
