'''
Python script to submit biomarker discovery jobs.
'''

import sys
import os

ncv = int( sys.argv[1] )
prefix = sys.argv[2]
projectDirectory = sys.argv[3]
print 'NCV= %s' % ncv
print 'Prefix= %s' % prefix
print 'Project Directory=%s' % projectDirectory

for i in range(1,ncv+1):
        command = "R --no-save < ./scripts/PipelineScript.R --args %s %s %s" % (i, prefix, projectDirectory)
        print 'Executing command: %s' % command
        os.system(command)
        filename = "%s_iter_%s.txt" % (prefix, i)
        os.rename("./%s" % filename, "results/cv/%s" % filename)

print 'done'

