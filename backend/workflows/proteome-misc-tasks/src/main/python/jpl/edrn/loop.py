import os

# edrn-node0n
NUM_NODES = 18
nodes = [ 'edrn-node%s' % format(i,'02d') for i in range(1,NUM_NODES+1) ]
#FILEPATH = '/usr/local/edrn/proteome/proteome-pge/pge-configs/tabblab/myrimatch.setup.pgeconfig.xml'
FILEPATH = "/usr/local/edrn/proteome/proteome-pge/pge-configs/tabblab/proteome-dbsearch.tabblab.pgeconfig.xml"



# loop over nodes
for node in nodes:
  command = 'scp %s edrn@%s:%s' % (FILEPATH, node, FILEPATH)
  print command
  os.system( command )
