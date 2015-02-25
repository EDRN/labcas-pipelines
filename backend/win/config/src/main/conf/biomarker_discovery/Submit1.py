'''
Python script to submit biomarker discovery jobs.
'''

import sys
import os

ncv = int( sys.argv[1] )
prefix = sys.argv[2]
projectDirectory = sys.argv[3]
proteomeHome = os.environ['PROTEOME_HOME']
print 'NCV= %s' % ncv
print 'Prefix= %s' % prefix
print 'Project Directory=%s' % projectDirectory
print 'PROTEOME_HOME=%s' % proteomeHome

# edrn-node0n
NUM_NODES = 18
PROTEOME_WORKFLOW_URLS = [ 'http://edrn-node%s:9001' % format(i,'02d') for i in range(1,NUM_NODES+1) ]


for i in range(1,ncv+1):
       
        # choose processing node URL through round-robin
        j = i-1
        if j >= len(PROTEOME_WORKFLOW_URLS):
	   j = 0
        wkfmgrUrl = PROTEOME_WORKFLOW_URLS[j]

        command = "cd %s/proteome-workflow-linux/bin; ./wmgr-client --url %s --operation --sendEvent --eventName biomarker-discovery-2 --metaData --key ncv %s" % (proteomeHome, wkfmgrUrl, i)
        print 'Executing command: %s' % command

        os.system(command)

print 'done'
