# Author: Joe Perez-Rogers; Adapted from Joshua Campbell, 2012
# Contact: jperezrogers@gmail.com
# Date: 2014-11-21
# Title: Biomarker Discovery Pipeline

# read in command line arguments 
# 1) the number of iterations run and 2) the file name prefix
arguments <- commandArgs(TRUE)

# set fold count and file prefix
fold.count <- as.numeric(arguments[1])
prefix <- arguments[2]
outdir <- arguments[3]

# read in each of the fold.count folds from the cv/ folder and concatenating them into a single object
mat <- c()
for (i in 1:fold.count) {
	if(file.exists(paste(outdir,"/results/cv/",prefix,"_iter_",i,".txt",sep=""))){
		infile <- read.table(paste(outdir,"/results/cv/",prefix,"_iter_",i,".txt",sep=""),head=T,sep="\t")
		mat <- rbind(mat,infile)
	}
}

# summarizing the fold.count files by model. Each model will have fold.count entries which need to be summarized.
# typically the first several columns are just parameters of the model which do not need to be summarized across
# folds. However, things like training set AUC or test set AUC or accuracy need to be averaged. Below is one hard-
# coded way to do that
matrix.summary <- c()
for (mod in names(table(mat$Index))) {
	ss <- subset(mat,Index==mod)
	train.na <- sum(is.na(ss$TrainAUC))
	test.na <- sum(is.na(ss$TestAUC))
	ms <- c(ss[1,1:5],colMeans(na.omit(ss[,6:ncol(ss)])))
	mr <- c(as.character(ms$Index),as.character(ms$FF),as.character(ms$FS),as.character(ms$BS),as.character(ms$CL),as.character(ms$TrainAUC),as.character(ms$TestAUC),train.na,test.na)
	matrix.summary <- rbind(matrix.summary,mr)
}
colnames(matrix.summary) <- c(colnames(mat),"TrainNA","TestNA")

# writing out the concatenated file as well as the summary file
write.table(mat,file=paste(outdir,"/results/concat/",prefix,"_concat.txt",sep=""),sep="\t",col.names=T,row.names=F,quote=F)
write.table(matrix.summary,file=paste(outdir,"/results/summary/",prefix,"_summary.txt",sep=""),sep="\t",col.names=T,row.names=F,quote=F)





