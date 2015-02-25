# Author: Joe Perez-Rogers; Adapted from Joshua Campbell, 2012
# Contact: jperezrogers@gmail.com
# Date: 2014-11-21
# Title: Shell script to run the biomarker discovery cross-validation process

#!/bin/bash
##!/bin/sh

# configuration of the biomarker pipeline cross-validation process

# set the number of cross-validation runs to initiate
#NCV=20
NCV=$1

# set the path of user-installed R packages (if none, leave as empty quotes "")
USERLIBS=""

# define the file prefix name
#PREFIX="JPL_NatMed_Gender_Classification"
PREFIX=$2

# define the project directory (this is where the bin & data files reside)
#PROJECTDIR="/usr/local/edrn/proteome/pipeline-tools/biomarker_discovery"
PROJECTDIR=$3

# define the output directory for the results files
OUTDIR=$PROJECTDIR

# set a random integer to be appended to the names of submitted jobs in case of multiple submissions of the same pipeline script
RVAR=$RANDOM

# make the log and results folders
mkdir -p $OUTDIR/results
mkdir -p $OUTDIR/results/logs

# make the cv dir to store outputs and make it the TMPDIR
mkdir -p $OUTDIR/results/cv
TMPDIR=$OUTDIR"/results/cv"

# submit the jobs
for i in `seq 1 $NCV`
do
  ID=$PREFIX"_fold_"$i"_"$RVAR
  LOG=$OUTDIR"/results/logs/"$ID".qlog"
  echo $LOG
  x=$(qsub -N $ID -d $TMPDIR -o $LOG -vJOB_ID=$ID,ARG1=$i,ARG2=$PREFIX,ARG3=$TMPDIR,ARG4=$PROJECTDIR,ARG5=$USERLIBS,TMPDIR=$TMPDIR $PROJECTDIR/scripts/Submit.qsub)
  echo $x
done

## submit the post-processing script
# PROJECTDIR is not used
PPLOG=$OUTDIR"/results/logs/"$PREFIX"_post_process.log"
qsub -N PostProcess -d $PROJECTDIR -o $PPLOG -W depend=afterok:$x -vARG1=$NCV,ARG2=$PREFIX,ARG3=$OUTDIR,ARG4=$PROJECTDIR $PROJECTDIR/scripts/PostProcess.qsub
