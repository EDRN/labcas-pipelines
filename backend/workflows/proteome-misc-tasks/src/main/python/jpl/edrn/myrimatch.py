'''
Python script to submit 'myrimatch' workflow to multiple processing nodes.

@author: cinquini
'''

import urllib
import logging
import urllib2
import json
import subprocess
import sys
import time
import os

# edrn-frontend
SOLR_URL = 'http://edrn-frontend.jpl.nasa.gov:8081/solr/oodt-fm'
PROTEOME_WORKFLOW_HOME = '/usr/local/edrn/proteome/proteome-workflow-linux' 

# edrn-node0n
NUM_NODES = 1 # FIXME
PROTEOME_WORKFLOW_URLS = [ 'http://edrn-node%s:9001' % format(i,'02d') for i in range(1,NUM_NODES+1) ]

# other parameters
MAX_ROWS = 10
SLEEP_SECONDS = 60
DBFILE = './products.txt'

logging.basicConfig(level=logging.DEBUG)

def chooseNode():
    '''Chooses the first available node.'''
    
    # keep trying till an available node is returned
    while True:
    
        for workflowUrl in PROTEOME_WORKFLOW_URLS:
            
            idle = True
            command = "cd %s/bin;" % PROTEOME_WORKFLOW_HOME
            command += "./wmgr-client --operation --getWorkflowInsts --url %s" % workflowUrl
            logging.debug("Executing: %s" % command)
            p = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
            for line in p.stdout.readlines():
                logging.debug( line )
                if 'status=PGE EXEC' in line:
                    idle = False # job already running
            # choose this node
            if idle:
                return workflowUrl
    
        # no nodes available: wait some time before looping through the list again
        logging.debug("No nodes available, waiting %s seconds..." % SLEEP_SECONDS)
        time.sleep(SLEEP_SECONDS)

def querySolr():
    '''Queries Solr for all available products.'''
    
    productNames = []
    
    # keep querying Solr until all records are returned
    start = 0
    doit = True
    while doit:
        params = { 'q':'*:*', 'wt':'json', 'indent':'true', 'start':str(start), 'rows':MAX_ROWS }
        url = SOLR_URL+"/select?"+urllib.urlencode(params)
        
        # send request
        logging.debug("Solr request: %s" % url)
        fh = urllib2.urlopen( url )
        jdoc = fh.read().decode("UTF-8")
        response = json.loads(jdoc)
            
        # parse response, loop over products
        #logging.debug("Solr Response: %s" % response)
        numFound = int( response['response']['numFound'])
        for doc in response['response']['docs']:
            productNames.append( doc['CAS.ProductName'].replace('.raw','') )
            
        # next iteration of products
        start += MAX_ROWS
        if start >= numFound:
            doit = False

    return productNames

def submitJob(workflowUrl, productName):
    
    logging.info('Submitting product=%s --> node=%s' % (productName, workflowUrl))
                        
    # execute workflow manager invocation
    command = "cd %s/bin;" % PROTEOME_WORKFLOW_HOME
    command += "./wmgr-client --url %s --operation --sendEvent --eventName myrimatch --metaData --key ProductName %s" % (workflowUrl, productName)
                
    # FIXME: submit only one job
    #if productName=='A1_frx01':
    logging.debug("Executing: %s" % command)
    p = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    for line in p.stdout.readlines():
        logging.debug( line )
    retval = p.wait()
    logging.debug('Return value=%s' % retval)
    
def readDbFile():
    '''Reads the list of products already submitted.'''
    
    if os.path.exists(DBFILE):
        with open(DBFILE) as f:
            products = f.read().splitlines()
            
        return products
    
    else:
        return []

def writeDbFile(productName):
    '''Writes a new submitted product to the database file.'''
    
    with open(DBFILE, 'a') as f:
        f.write('%s\n' % productName)

if __name__ == '__main__':
    
    submittedProductNames = readDbFile()
    
    # query for all available products
    productNames = querySolr()
    
    # loop over products
    for productName in productNames:
        
        # do not submit again
        if not productName in submittedProductNames:
        
            # choose first available node
            workflowUrl = chooseNode()
            

            # submit to this node
            submitJob(workflowUrl, productName)
            
            # keep track of submission
            submittedProductNames.append(productName)
            writeDbFile(productName)
            