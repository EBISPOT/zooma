# ZOOMA

ZOOMA is an optimal ontology mapping application developed by the Functional Genomics Production team at the European Bioinformatics Institute.

This is the publicly available code repository for ZOOMA.  Currently, this only includes the core libraries for ZOOMA - there are no generic user interface implementations available, only the EBI internal version that is visible at the homepage listed below.  We're in the process of adding support for building customized versions of ZOOMA with your own data, and we will update these pages when this is available.

Developer documentation is available as part of the ZOOMA documentation at the link given below.  If you can't find what you need there, feel free to email the lead developer, Tony Burdett, on tburdett@ebi.ac.uk.

## Links

### ZOOMA homepage

http://www.ebi.ac.uk/spot/zooma

### ZOOMA documentation

http://www.ebi.ac.uk/spot/zooma/docs

### EBI

http://www.ebi.ac.uk

### Dependencies
#### Project Dependencies
ZOOMA depends on the lodestar project to produce it's SPARQL endpoint.  The source for this project is embedded as a 
submodule, served from the GitHUb repository https://github.com/EBISPOT/lodestar.  See this project for more details. 
When building from source, the latest version of this codebase will be incorporated into the build.

#### Software Dependencies
1. Java - Tested on Oracle JDK version 1.8
2. Maven - Tested using Maven 3.6
3. Virtuoso - Tested on the open source version of Virtuoso 7.2.5 which can be downloaded from 
http://vos.openlinksw.com/owiki/wiki/VOS/VOSDownload. 
4. Solr


## Building Zooma
Zooma requires `ojdbc6.jar` version 11.2.0.X for which there is no maven repository. Hence, one needs to download the file 
from Oracle and install it manually into a Maven repository. The steps to do this are:

1. Download `ojdbc6.jar` from http://www.oracle.com/technetwork/apps-tech/jdbc-112010-090769.html. The version number 
of the driver is given at the top of the page as 11.2.0.X. At the time of writing the actual value for *X* is 4. Thus
the full version number is 11.2.0.4.

2. Install the `ojdbc6.jar` file into a Maven repository. The command below installs the
 file into the local Maven repository(Assuming *X*=4):
`mvn  install:install-file -DgroupId=com.oracle -DartifactId=ojdbc6 -Dpackaging=jar -Dversion=11.2.0.4 
-Dfile=PATH_TO_THE_JAR_YOU_DOWNLOADED/ojdbc6.jar`

You should get a 'Build Success' message and the library should be installed under your mvn repository: 

 `~/.m2/repository/com/oracle/ojdbc6/11.2.0.4/ojdbc6-11.2.0.4.jar`

Once the `ojdbc6.jar` file has been installed into the Maven repository, Zooma can be built from its root directory be using:

`mvn clean package` 


## Configuring Zooma
Once Zooma has been built, a `zooma-builder.zip` can be found under `/zooma/zooma-builder-app/target`. This contains
the application for setting up zooma. Extract `zooma-builder.zip` and navigate to `/zooma-builder/bin/README`. This file 
contains important information on configuring Zooma and the order in which various scripts must be run.

1. Add the environment variables as described in `/zooma-builder/bin/README`.

2. To deploy the zooma web application to Tomcat, navigate `/zooma/zooma-ui/target` and copy the `zooma.war` 
to the `/webapps` directory of Tomcat. This will create the template config under `$ZOOMA_HOME`. Stop Tomcat.

3. Run the scripts under Execution in `/zooma-builder/bin/README`.

