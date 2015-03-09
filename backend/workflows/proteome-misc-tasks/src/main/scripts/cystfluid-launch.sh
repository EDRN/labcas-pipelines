#!/bin/bash
./wmgr-client --url $PROTEOME_WORKFLOW_URL --operation --sendEvent --eventName TabblabProteomicsPipeline \
--metaData \
--key RawDatasetName 20140506-Pancreatic-Cyst-Fluid \
--key PrimaryAnalysisDatasetName 0140506-Pancreatic-Cyst-Fluid-Processed \
--key RAWFiles f3f8255c-e270-4303-a69d-1d01b0c98f50 e3e8eebb-e885-4cd5-b79e-5ebdf32eeafa cb5f925f-5d19-4bcd-971a-6c40875bcbaf \
--key DBFile /usr/local/edrn/proteome/pipeline-tools/tabblab/protein.db.fasta \
--key CntmsFile /usr/local/edrn/proteome/pipeline-tools/tabblab/Cntms.fasta \
--key MyriMatchCfgFile /usr/local/edrn/proteome/pipeline-tools/tabblab/cystfluid.myrimatch.cfg \
--key AssembleFileList /usr/local/edrn/proteome/pipeline-tools/tabblab/Assembly.txt \
--key ProteinFile /usr/local/edrn/proteome/pipeline-tools/tabblab/protein.fa \
--key CopyFiles false --key UserEmail luca.cinquini@jpl.nasa.gov