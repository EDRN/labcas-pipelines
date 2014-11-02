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

# edrn-frontend
SOLR_URL = 'http://edrn-frontend.jpl.nasa.gov:8081/solr/oodt-fm'
PROTEOME_WORKFLOW_HOME = '/usr/local/edrn/proteome/proteome-workflow-linux' 
MAX_ROWS = 10

# edrn-node0n
NUM_NODES = 18
PROTEOME_WORKFLOW_URLS = [ 'http://edrn-node%s:9001' % format(i,'02d') for i in range(1,NUM_NODES+1) ]

logging.basicConfig(level=logging.INFO)

if __name__ == '__main__':
        
    # keep querying Solr until all records are returned
    start = 0
    doit = True 
    inode = 0
    while doit:
        params = { 'q':'*:*', 'wt':'json', 'indent':'true', 'start':str(start), 'rows':MAX_ROWS }
        url = SOLR_URL+"/select?"+urllib.urlencode(params, doseq=True)
        
        # send request
        logging.debug("Solr request: %s" % url)
        fh = urllib2.urlopen( url )
        jdoc = fh.read().decode("UTF-8")
        response = json.loads(jdoc)
            
        # parse response, loop over products
        #logging.debug("Solr Response: %s" % response)
        numFound = int( response['response']['numFound'])
        for doc in response['response']['docs']:
            productName = doc['CAS.ProductName']
            productName = productName.replace('.raw','')
            logging.info('Submitting product=%s --> node=%s' % (productName, PROTEOME_WORKFLOW_URLS[inode]))
                        
            # execute workflow manager invocation
            command = "cd %s/bin;" % PROTEOME_WORKFLOW_HOME
            command += "./wmgr-client --url %s --operation --sendEvent --eventName myrimatch --metaData --key ProductName %s"\
                       % (PROTEOME_WORKFLOW_URLS[inode], productName)
                
            # FIXME: submit only one job
            if productName=='A1_frx01':
                logging.debug("Executing: %s" % command)
                p = subprocess.Popen(command, shell=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
                for line in p.stdout.readlines():
                    logging.debug( line )
                retval = p.wait()
                logging.debug('Return value=%s' % retval)
                
            # next processing node
            inode += 1
            if inode == NUM_NODES:
                inode = 0
            
        # next iteration of products
        start += MAX_ROWS
        if start >= numFound:
            doit = False
