# ZOOMA

ZOOMA is an application for discovering optimal ontology mappings, developed by the Samples, Phenotypes and Ontologies Team at [EMBL-EBI](http://www.ebi.ac.uk). It can be used to automatically annotate "properties" (plain text, descriptive values about biological entities) with "semantic tags" (ontology classes).

* ZOOMA is currently live at the EBI here http://www.ebi.ac.uk/spot/zooma
* ZOOMA documentation can be found here http://www.ebi.ac.uk/spot/zooma/docs

## Deploying with Docker

The preferred method of deployment for ZOOMA is using Docker.   If you would like to deploy **the entire OntoTools stack** (OLS, OxO, and ZOOMA), check out the [OntoTools Docker Config](https://github.com/EBISPOT/ontotools-docker-config) repository. If you would like to deploy **ZOOMA only**, read on.

First, create the necessary volumes:

    docker volume create --name=zooma-config

Then, start ZOOMA:

    docker run -d --name zooma -p 8009:8080 -v zooma-config:/root/.zooma/config ebispot/zooma:latest

You should now be able to access ZOOMA at `http://localhost:8080`.

To configure ZOOMA, first stop the docker container:

    docker stop zooma

The configuration files will be located in the zooma-config volume on your filesystem. For Ubuntu, this will be located at `/var/lib/docker/volumes/zooma-config/_data`.

Once configured, start ZOOMA again:

    docker start zooma


## Building ZOOMA manually

### Dependencies
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

