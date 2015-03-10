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
SOLR_QUERY = 'CAS.ProductTypeName:20140506-Pancreatic-Cyst-Fluid'
PROTEOME_WORKFLOW_HOME = '/usr/local/edrn/proteome/proteome-workflow-linux' 

# edrn-node0n
NUM_NODES = 8
PROTEOME_WORKFLOW_URLS = [ 'http://edrn-node%s:9001' % format(i,'02d') for i in range(1,NUM_NODES+1) ]

# other parameters
MAX_ROWS = 100
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
                if 'status=PGE EXEC' in line or 'status=RSUBMIT' in line or 'status=QUEUED' in line:
                    idle = False # job already running
            # choose this node
            if idle:
                return workflowUrl
    
        # no nodes available: wait some time before looping through the list again
        logging.debug("No nodes available, waiting %s seconds..." % SLEEP_SECONDS)
        time.sleep(SLEEP_SECONDS)

def querySolr():
    '''Queries Solr for all available products.'''
    
    productNamesDict = {}
    
    # keep querying Solr until all records are returned
    start = 0
    doit = True
    while doit:
        params = { 'q':SOLR_QUERY, 'wt':'json', 'indent':'true', 'start':str(start), 'rows':MAX_ROWS }
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
            '''
            <str name="id">2d1610a4-0ca4-429d-9754-c61279d1262b</str>
            <str name="CAS.ProductId">2d1610a4-0ca4-429d-9754-c61279d1262b</str>
            <str name="CAS.ProductTypeName">20140506-Pancreatic-Cyst-Fluid</str>
            <date name="CAS.ProductReceivedTime">2014-10-29T11:44:58Z</date>
            <str name="CAS.ProductTransferStatus">RECEIVED</str>
            <str name="CAS.ProductName">A1_frx09.raw</str>
            <str name="CAS.ProductTypeId">
            urn:edrn:labcas:dtabb:dtabb|20140506-pancreatic-cyst-fluid
            </str>
            '''
            productNamesDict[doc['CAS.ProductId']] = doc['CAS.ProductName'].replace('.raw','')
            
        # next iteration of products
        start += MAX_ROWS
        if start >= numFound:
            doit = False

    return productNamesDict

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
    
    # List of products already submitted for processing
    submittedProductNames = readDbFile()
    print 'Product names already submitted: %s' % submittedProductNames
    
    # query for all available products
    productNamesDict = querySolr()
    
    # list of requested products, or all products if none is specified
    requestedProductNames = []
    if len(sys.argv) == 2:
           requestedProductNames = [x.strip().replace('.raw','') for x in sys.argv[1].split(",")]
    else:
        requestedProductNames = productNamesDict.values()
    print 'Requested product names: %s' % requestedProductNames

    
    # loop over products
    for productId, productName in productNamesDict.items():
      
      # must either specify one or more products,
      # or specify no products to process all
      if productName in requestedProductNames:
        
        # do not process any product more than once
        if not productName in submittedProductNames:
        # submit only one producr
        #if productName == 'A1_frx01':
        
            # choose first available node
            workflowUrl = chooseNode()
            
            # submit to this node
            print 'Submitting product id=%s name=%s to node=%s' % (productId, productName, workflowUrl)
            submitJob(workflowUrl, productName)
            
            # keep track of submission
            submittedProductNames.append(productName)
            writeDbFile(productName)
            
        else:
            print 'Product name=%s already submitted for processing (see file products.txt)' % productName