# zooma
ZOOMA - Optimal Ontology Mapping Application. http://www.ebi.ac.uk/spot/zooma.

# Quickstart


  Install Maven and JDK 8
  
  Install docker-compose https://docs.docker.com/compose/

  `mvn package`

  `docker-compose build`

  `docker-compose up`

 public zooma-mongo api: http://localhost:8080
 
 public zooma-solr api: http://localhost:8081

 internal RabbitMQ interface at http://localhost:15672/
 
 internal Solr interface at http://localhost:8983/
 
 To load data to the application in the container:
 - Edit the application.properties.example file in zooma-csv-loader
 - Rename it to application.properties
 - Run `mvn package` again
 - Run `java -jar zooma-csv-loader-<version>.jar` from the target directory of zooma-csv-loader while the container is running
