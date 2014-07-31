# .bashrc

# Source global definitions
if [ -f /etc/bashrc ]; then
        . /etc/bashrc
fi

# Pipeline linux env vars
export JAVA_HOME=/usr/java/default
export PROTEOME_HOME=/usr/local/proteome
export PROTEOME_WORKFLOW_HOME=/usr/local/proteome/proteome-workflow-linux
export PROTEOME_CRAWLER_HOME=/usr/local/proteome/proteome-crawler-linux
export PROTEOME_RESOURCE_HOME=/usr/local/proteome/proteome-resource-linux
export PROTEOME_WORKFLOW_URL=http://biomarker:9001
export PROTEOME_RESOURCE_URL=http://biomarker:9002
export PROTEOME_FILEMGR_URL=http://biomarker:9000
export PROTEOME_TOMCAT_URL=http://biomarker
export PGE_ROOT=/usr/local/proteome/proteome-pge

# Pipeline cygwin env vars
export PGE_ROOT_CYGWIN=P:/proteome-pge
export PROTEOME_CRAWLER_HOME_CYGWINPATH=P:/proteome-crawler-linux

# Pipeline misc env vars
export PIPELINE_LINUX_HOST=http://biomarker
export PIPELINE_WIN_HOST=http://biomarker-win
export PIPELINE_LINUX_BATCHSTUB_PORT=2002
export PIPELINE_WIN_BATCHSTUB_PORT=2003

# User specific aliases and functions
alias ps_oodt="ps -ef | grep java | grep org.apache.oodt"
alias fmquery="java -Dorg.apache.oodt.cas.filemgr.properties=../etc/filemgr.properties -Djava.ext.dirs=../lib org.apache.oodt.cas.filemgr.tools.QueryTool --url http://biomarker:9000 --lucene -query "
alias fmdel="java -Dorg.apache.oodt.cas.filemgr.properties=../etc/filemgr.properties -Djava.ext.dirs=../lib org.apache.oodt.cas.filemgr.tools.DeleteProduct --fileManagerUrl http://biomarker:9000 --read"
