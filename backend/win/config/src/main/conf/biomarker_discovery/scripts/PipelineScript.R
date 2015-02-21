# Author: Joe Perez-Rogers; Adapted from Joshua Campbell, 2012
# Contact: jperezrogers@gmail.com
# Date: 2014-11-21
# Title: Biomarker Discovery Pipeline

###################################################
# PIPELINE CONFIGURATION                          #
###################################################

# read in command line arguments fed in via shell script and qsub file
arguments <- commandArgs(TRUE)

# FIXME
write("prints to stdout", stdout())

# set the seed and iteration number for this particular run. We store this information so we can reproduce every iteration of the pipeline
# first, we check whether or not there were any arguments read into R. If there werent any arguments (i.e. we're debugging the script)
# we set the iteration number to a random integer and we set the output file prefix to 'debug'. If there are arguments read into R, we 
# set the iteration and output file prefix accordingly.
if (length(arguments)==0) {
  iteration <- sample(1:1000,1)
  prefix <- "debug"
  projectdir <- getwd()
  if(file.exists("~/R_LIBS")){
  	userlibs <- "~/R_LIBS"
  } else {
  	userlibs <- ""
  }
} else {
  iteration <- as.numeric(arguments[1])
  prefix <- arguments[2]
  projectdir <- arguments[3]
  userlibs <- arguments[4]
}
cat(paste("Iteration: ",iteration,"\n",sep=""))

# set paths to user-intalled r libraries and add that path to the .libPaths variable
# allowing R to search user.libs for libraries
.libPaths(c(.libPaths(),file.path(userlibs)));

# load R libraries that are required throughout this pipeline
library(affy)
library(limma)
library(MASS)
library(AUC)
library(gplots)
library(class)
library(e1071)
library(randomForest)
library(glmnet)

# load user-defined functions from bin
# these are a series of homegrown scripts that make processing the data easier
# the script 'sourceDirectory.R' allows the user to load all functions from a specified folder into memory for use in a given R session
print(projectdir)
print(prefix)
#q()
source(paste(projectdir,"/bin/SourceDirectory.R",sep=""))
funcs <- SourceDir(paste(projectdir,"/bin",sep=""),trace=FALSE)

# we set the seed which is a function of the iteration number. Because jobs are submitted to the sun grid engine queue so rapidly, the time
# stamp variable in the UNIX environment would often overlap between iterations of this pipeline, thus setting the same seed in multiple runs.
# to avoid this and ensure that each iteration of the pipeline was unique, we had to write a function to ensure that the seed was in fact
# different each time. Thus the random seed being computed as a function of the iteration ensures this is true. 
newSeed <- SetRandomSeed(x=iteration,print=TRUE)

# set output file name
outfile <- paste(prefix,"_iter_",iteration,".txt",sep="")
#print(outdir)
print(outfile)

###################################################
# PIPELINE INPUT DATA                             #
###################################################

# read in the data set. Most microarray data we use are stored in expression set objects in R (.rds). We use the command 'readRDS()' 
# to read in these particular files. Stored within an rds file are 3 matrices. 
# 1) a matrix of gene expression values which is accessed via 'exprs(fullDataSet)'
# 2) a phenotypic matrix containing clinical covariates which is accessed via 'pData(fullDataSet)'
# 3) a gene annotation matrix which contains mappings of genes to other known symbols such as ENSG or EMBL, accessed via 'fData(fullDataSet)'
fullDataSet <- readRDS(file=paste(projectdir,"/data/GSE4115_10female_10male.rds",sep=""))
fullExprs <- exprs(fullDataSet)
fullAnnot <- pData(fullDataSet)

# Here, we specificy the parameters that we want to evaluate. Each entry is a unique parameter for some piece of the pipeline.
# ff = feature filter = an unsupervised method to remove genes from our expression matrix
# fs = feature selection = a method to choose features that best differentiate between case and control classes
# bs = biomarker size = define the size of the biomarker we want to build
# cl = classification = an algorithm to define how the classes are separated and predict to which class new samples belong
parameterList <- list(
  ff=c("var","mean"),
  fs=c("lm-t","lm-abst","rf"),
  bs=c(25,50,100),
  cl=c("wv","nb","svm")
)

# create a matrix of all of the different model combinations that can be made from the parameterList above. Each row indicated a unique 
# configuration of the parameters listed above. The models are sorted so that computation is minimized and upstream methods do not need to
# be recalculated each time. For example, if we test three different classifiers but leave all other parameters constant, we do not need to
# recalculate the ff, fs, or bs steps.
index <- calculate.index(parameterList)
index <- index[order(index[,1], index[,2], index[,3], index[,4]),]

# starting a new results file and telling the program we need to print the header
need.header <- TRUE

###################################################
# Module 1: Discovery Set Split                   #
###################################################

# split our fullDatatSet into training/testing sets based on percentage in the test set
cvIndex <- SplitCV(fullDataSet,pct=0.30)
trainDataSet <- fullDataSet[,-cvIndex]
testDataSet <- fullDataSet[,cvIndex]

# defining the class variable for training and test sets
trainClass <- Class2Binary(trainDataSet$sex,case="gender: M",control="gender: F")
testClass <- Class2Binary(testDataSet$sex,case="gender: M",control="gender: F")

# run through all possible models
cat("Running through all possible models\n")

for (i in 1:nrow(index)) {
  
  # define model parameters
  ff.m <- parameterList$ff[index[i,1]] # feature filter model (ff.m)
  fs.m <- parameterList$fs[index[i,2]] # feature selection model (fs.m)
  bs.m <- parameterList$bs[index[i,3]] # biomarker size model (bs.m)
  cl.m <- parameterList$cl[index[i,4]] # classification model (cl.m)
  
  # create a list to store all important information about a given iteration
  bmod <- list()
  bmod[["seed"]] <- newSeed # the seed for this iteration
  bmod[["index"]] <- i
  bmod[["ff"]] <- ff.m
  bmod[["fs"]] <- fs.m
  bmod[["bs"]] <- bs.m
  bmod[["cl"]] <- cl.m
  
  # find the minimum changing parameter
  min.change <- 1
  if (i > 1) {
    parameter.previous <- index[i-1,]
    parameter.index <- index[i,]
    min.change <- min(which(parameter.index != parameter.previous))
  }
  
  ###################################################
  # Module 2: Feature Filtering (Unsupervised)      #
  ###################################################
  
  cat(paste("Feature filtering...",ff.m,"\n",sep=""))

  if (min.change <=1 ) {
        
    # variance
    if (ff.m=="var") {
      var.p <- 0.05
      v <- apply(exprs(trainDataSet),1,var)
      med.idx <- whichMedian(v)
      p <- apply(exprs(trainDataSet),1,function(x){
        var.test(x,exprs(trainDataSet)[med.idx,],alternative="greater")$p.value
      })
      ff.eset <- trainDataSet[p<var.p,]
      ff.genes <- row.names(exprs(ff.eset))
    }
    
    # mean
    if (ff.m=="mean") {
      var.p <- 0.05
      m <- apply(exprs(trainDataSet),1,mean)
      med.idx <- whichMedian(m)
      p <- apply(exprs(trainDataSet),1,function(x){
        var.test(x,exprs(trainDataSet)[med.idx,],alternative="greater")$p.value
      })
      ff.eset <- trainDataSet[p<var.p,]
      ff.genes <- row.names(exprs(ff.eset))
    }
    
  }
  
  ###################################################
  # Module 3: Feature Selection (Supervised)        #
  ###################################################
  
  cat(paste("Feature selection...",fs.m,"\n",sep=""))
  
  if (min.change <=2 ) {
        
    if (fs.m=="lm-t") {
      # linear model sorting genes by t-statistic
      model <- model.matrix(~1+trainClass, data=pData(ff.eset),na.action=na.exclude)
      fit <- lmFit(exprs(ff.eset),model,na.action=na.exclude)  
      fit.t <- fit$coef / fit$stdev.unscaled / fit$sigma
      fs.eset <- ff.eset[names(sort(fit.t[,2],decreasing=T)),]
      fs.genes <- row.names(exprs(fs.eset))
      
    } else if (fs.m=="lm-abst") {
      # linear model sorting genes by absolute t-statistic
      model <- model.matrix(~1+trainClass, data=pData(ff.eset),na.action=na.exclude)
      fit <- lmFit(exprs(ff.eset),model,na.action=na.exclude)  
      fit.t <- fit$coef / fit$stdev.unscaled / fit$sigma
      fs.eset <- ff.eset[names(sort(abs(fit.t[,2]),decreasing=T)),]
      fs.genes <- row.names(exprs(fs.eset))
      
    } else if (fs.m=="rf") {
      require(randomForest)
      # building a random forest with 5000 trees and ranking genes based on their importance score
      rf.out <- randomForest(t(exprs(ff.eset)), as.factor(trainClass), ntree=5000, importance=TRUE)
      fs.eset <- ff.eset[names(sort(rf.out$importance[,3], decreasing=T)),]
      fs.genes <- row.names(exprs(fs.eset))
      
    }
    
  }
  
  ###################################################
  # Module 4: Select Biomarker Size                 #
  ###################################################
  
  cat(paste("Biomarker size...",bs.m,"\n",sep=""))
  
  if (min.change <= 3) {
    
    if(fs.m=="lm-t"){
      bs.genes <- c(head(row.names(exprs(fs.eset)),round(bs.m/2)),tail(row.names(exprs(fs.eset)),round(bs.m/2)))
      bs.eset <- fs.eset[bs.genes,]
    } else {
      bs.genes <- head(row.names(exprs(fs.eset)),bs.m)
      bs.eset <- fs.eset[bs.genes,]
    }
    
  }
  
  ###################################################
  # Module 5: Build Classifiers (Supervised)        #
  ###################################################
  
  cat(paste("Building classifier...",cl.m,"\n",sep=""))
 
  if (min.change <= 4) {
    
    # clean up the workspace
    perform.train = NA
    perform.test = NA
    predict.train = NA
    predict.test = NA
    predictions.test = NA
    predictions.train = NA
    
    # define the test set
    test.eset <- testDataSet[row.names(exprs(bs.eset)),]
    
    if (cl.m== "wv") {
      
      predict.model <- wv.model(exprs(bs.eset),trainClass)
      
      predictions.train <- predict.wv(predict.model,exprs(bs.eset))
      predictions.train.scores <- predictions.train$scores
      predictions.train <- predictions.train$predictions
      
      predictions.test <- predict.wv(predict.model,exprs(test.eset))
      predictions.test.scores <- predictions.test$scores
      predictions.test <- predictions.test$predictions
      
    } else if (cl.m== "svm") { 
      
      # logistics
      f.levels <- levels(as.factor(trainClass))
      class.weights<-c((sum(trainClass==f.levels[1])/length(trainClass)),(sum(trainClass==f.levels[2])/length(trainClass)))
      names(class.weights) <- f.levels
      
      # building model
      tune.obj <- tune.svm(t(exprs(bs.eset)), trainClass, kernel="linear", cost = c(0.001,0.01,1,10,100), tunecontrol = tune.control(sampling = "cross",cross=5), cross=5,type="C",class.weights=class.weights)
      predict.model <- svm(t(exprs(bs.eset)), trainClass, kernel="linear", type="C", cost=tune.obj$best.model$cost,,class.weights=class.weights)
      
      # training set
      predictions.train <- predict(predict.model, t(exprs(bs.eset)), decision.values=TRUE)
      if(predict.model$labels[1] == 1) {
        predictions.train.scores <- -attr(predictions.train, "decision.values")
      } else {
        predictions.train.scores <- attr(predictions.train, "decision.values")
      }
      predictions.train <- as.numeric(as.character(predictions.train))
      
      # test set
      predictions.test <- predict(predict.model, t(exprs(test.eset)), decision.values=TRUE)
      if(predict.model$labels[1] == 1) {
        predictions.test.scores <- -attr(predictions.test, "decision.values")
      } else {
        predictions.test.scores <- attr(predictions.test, "decision.values")
      }
      predictions.test <- as.numeric(as.character(predictions.test))
      
    } else if (cl.m == "nb") {
      
      predict.model <- naiveBayes(t(exprs(bs.eset)), trainClass)
      
      predictions.train <- predict(predict.model, t(exprs(bs.eset)))
      predictions.train.scores <- predict(predict.model, t(exprs(bs.eset)), type="raw")[,2]
      predictions.train <- as.numeric(as.character(predictions.train))
      
      predictions.test <- predict(predict.model, t(exprs(test.eset)))
      predictions.test.scores <- predict(predict.model, t(exprs(test.eset)), type="raw")[,2]
      predictions.test <- as.numeric(as.character(predictions.test))
      
    }
    
  }
  
  ###################################################
  # Module 6: Summarization                         #
  ###################################################
  
  # evaluate the performance of the model
  perform.train <- performance(predictions.train, as.factor(trainClass), scores=predictions.train.scores)
  perform.test <- performance(predictions.test, as.factor(testClass), scores=predictions.test.scores)
  
  # adding prediction information to the output lists
  perform.train[["predictions"]] <- predictions.train
  perform.train[["scores"]] <- predictions.train.scores
  perform.test[["predictions"]] <- predictions.test
  perform.test[["scores"]] <- predictions.test.scores
  
  # adding prediction information and performance metrics to the bmod object
  bmod[["train"]] <- perform.train
  bmod[["test"]] <- perform.test
  
  # outputting the AUC for debugging purposes
  cat(paste("Training Set AUC: ",perform.train$AUC,"\n",sep=""))
  cat(paste("Test Set AUC: ",perform.test$AUC,"\n",sep=""))
  
  # writing the results of the iteration to a file
  if (need.header) {
    write.table(matrix(c("Index","FF","FS","BS","CL","TrainAUC","TestAUC"),nrow=1),file=outfile,sep="\t", quote=F, row.names=F, col.names=F, append=F)
    need.header <- FALSE
  }
  
  # files are written to the cwd which is $TMPDIR then are copied to the file system after they've been completed
  output.matrix <- matrix(c(bmod$index,bmod$ff,bmod$fs,bmod$bs,bmod$cl,bmod$train$AUC,bmod$test$AUC),nrow=1)
  write.table(output.matrix, file=outfile, sep="\t", quote=F, row.names=F, col.names=F, append=T)
  
  # put some space between model outputs
  cat("\n\n")
  
} #End cross-validation loop














