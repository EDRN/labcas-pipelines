REQUIREMENTS:
1. Set up your Win and Linux environment variables by installing and sourcing the bash env config file: 
   
   Modify and copy to your home directory on your own respective machines:
   LINUX: backend/linux/config/src/main/conf/.bashrc 
   WINDOWS: backend/win/config/src/main/conf/.bashrc 

   > source .bashrc 
2. LabCAS backend infrastructure
   a. A deployed LabCAS backend infrastructure on the LINUX machine, including filemgr and crawler
   b. The LABCAS_HOME environment variable pointing to the installation of LabCAS backend

BUILDING/DEPLOYMENT STEPS:
 1. (Optional) Edit top-level pom.xml and modify install.directory and user.directory
 2. LINUX: mvn install -Plinux
 3. WINDOWS: mvn install -Pwindows

For further, and more specific directions, please see pipeline deployment guide: http://edrn.me/7W 
